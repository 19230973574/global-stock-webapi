package com.globalstock.webapi.service;

import com.globalstock.webapi.common.BusinessException;
import com.globalstock.webapi.infra.KlineDataTaskInfra;
import com.globalstock.webapi.infra.MarketDataInfra;
import com.globalstock.webapi.manager.AuditManager;
import com.globalstock.webapi.model.document.KlineDataTaskDocument;
import com.globalstock.webapi.model.dto.KlineDeleteResultDTO;
import com.globalstock.webapi.model.dto.KlineFetchTaskDTO;
import com.globalstock.webapi.model.dto.KlineFetchTaskPageDTO;
import com.globalstock.webapi.model.dto.KlineRebuildResultDTO;
import com.globalstock.webapi.model.request.KlineDataRequests;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminKlineDataService {

    private static final int MAX_CODES = 200;
    private static final int MAX_ACTIVE_TASKS = 3;
    private static final int MAX_REBUILD_BATCH_TASKS = 10;
    private static final int MAX_DATE_RANGE_YEARS = 3;
    private static final int DELETE_CONFIRM_THRESHOLD = 5000;
    private static final int REBUILD_PHRASE_THRESHOLD = 1000;
    private static final String REBUILD_CONFIRM_PHRASE = "REBUILD";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final AuthService authService;
    private final KlineDataTaskInfra klineDataTaskInfra;
    private final MarketDataInfra marketDataInfra;
    private final AuditManager auditManager;
    private final AdminDataMgmtCache adminDataMgmtCache;

    public AdminKlineDataService(AuthService authService,
                                 KlineDataTaskInfra klineDataTaskInfra,
                                 MarketDataInfra marketDataInfra,
                                 AuditManager auditManager,
                                 AdminDataMgmtCache adminDataMgmtCache) {
        this.authService = authService;
        this.klineDataTaskInfra = klineDataTaskInfra;
        this.marketDataInfra = marketDataInfra;
        this.auditManager = auditManager;
        this.adminDataMgmtCache = adminDataMgmtCache;
    }

    public KlineFetchTaskDTO createFetchTask(KlineDataRequests.CreateFetchTaskRequest request) {
        authService.requireAdminRole();
        String operator = authService.currentOperatorEmail();

        String market = normalizeMarket(request.market());
        String period = normalizePeriod(request.period());
        List<String> codes = normalizeCodes(request.codes());
        String startDate = normalizeDate(request.startDate(), "开始日期");
        String endDate = normalizeDate(request.endDate(), "结束日期");
        validateDateRange(startDate, endDate);

        if (klineDataTaskInfra.countActiveTasks() >= MAX_ACTIVE_TASKS) {
            throw new BusinessException("当前排队/执行中的任务过多，请稍后再试");
        }

        long now = System.currentTimeMillis();
        KlineDataTaskDocument document = new KlineDataTaskDocument();
        document.setId("task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        document.setType("FETCH");
        document.setStatus("PENDING");
        document.setMarket(market);
        document.setPeriod(period);
        document.setCodes(codes);
        document.setStartDate(startDate);
        document.setEndDate(endDate);

        KlineDataTaskDocument.KlineTaskProgress progress = new KlineDataTaskDocument.KlineTaskProgress();
        progress.setTotalCodes(codes.size());
        progress.setDoneCodes(0);
        progress.setSavedBars(0);
        progress.setFailedCodes(0);
        document.setProgress(progress);

        KlineDataTaskDocument.KlineTaskResult result = new KlineDataTaskDocument.KlineTaskResult();
        result.setSavedBars(0);
        result.setFailedCodes(new ArrayList<>());
        document.setResult(result);

        document.setCreatedBy(operator);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        KlineDataTaskDocument saved = klineDataTaskInfra.save(document);
        adminDataMgmtCache.invalidateKlineDataCaches();
        auditManager.log("kline.fetch_task.created", operator, operator, "kline_task", saved.getId(),
                "codes=" + codes.size() + ", range=" + startDate + "~" + endDate);
        return toDto(saved);
    }

    public KlineFetchTaskPageDTO listFetchTasks(String status, Integer page, Integer pageSize) {
        authService.requireAdminRole();
        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);
        List<KlineFetchTaskDTO> items = klineDataTaskInfra.findPage(status, currentPage, currentPageSize).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        KlineFetchTaskPageDTO result = new KlineFetchTaskPageDTO();
        result.setPage(currentPage);
        result.setPageSize(currentPageSize);
        result.setTotal(klineDataTaskInfra.count(status));
        result.setItems(items);
        return result;
    }

    public KlineFetchTaskDTO getFetchTask(String taskId) {
        authService.requireAdminRole();
        KlineDataTaskDocument document = klineDataTaskInfra.findById(taskId);
        if (document == null) {
            throw new BusinessException("任务不存在");
        }
        return toDto(document);
    }

    public KlineFetchTaskDTO cancelFetchTask(String taskId) {
        authService.requireAdminRole();
        String operator = authService.currentOperatorEmail();
        if (!klineDataTaskInfra.cancelPending(taskId)) {
            throw new BusinessException("只能取消排队中的任务");
        }
        auditManager.log("kline.fetch_task.cancelled", operator, operator, "kline_task", taskId, null);
        return getFetchTask(taskId);
    }

    public KlineDeleteResultDTO deleteKlineData(KlineDataRequests.DeleteKlineRequest request) {
        authService.requireAdminRole();
        String operator = authService.currentOperatorEmail();

        String market = normalizeMarket(request.market());
        String period = normalizePeriod(request.period());
        List<String> codes = normalizeCodesOptional(request.codes());
        DateRange dateRange = resolveDeleteDates(request);

        if (codes == null && dateRange.dates() == null) {
            throw new BusinessException("请指定股票代码，或选择具体交易日");
        }

        long previewCount = marketDataInfra.countHistoryBarsForDelete(
                market, period, codes, dateRange.startDate(), dateRange.endDate(), dateRange.dates());
        boolean confirm = Boolean.TRUE.equals(request.confirm());

        KlineDeleteResultDTO result = new KlineDeleteResultDTO();
        result.setCodes(codes == null ? List.of() : codes);
        result.setStartDate(dateRange.startDate());
        result.setEndDate(dateRange.endDate());
        result.setPreviewCount(previewCount);

        if (!confirm) {
            result.setPreview(true);
            result.setDeletedCount(0);
            return result;
        }

        if (previewCount > DELETE_CONFIRM_THRESHOLD) {
            throw new BusinessException("待删除条数超过 " + DELETE_CONFIRM_THRESHOLD + "，请确认范围后重试");
        }

        long deleted = marketDataInfra.deleteHistoryBars(
                market, period, codes, dateRange.startDate(), dateRange.endDate(), dateRange.dates());
        result.setPreview(false);
        result.setDeletedCount(deleted);

        auditManager.log("kline.data.deleted", operator, operator, "kline_history", market,
                "codes=" + (codes == null ? "ALL" : codes.size()) + ", deleted=" + deleted + ", range="
                        + (dateRange.dates() != null ? dateRange.dates() : dateRange.startDate() + "~" + dateRange.endDate()));
        adminDataMgmtCache.invalidateKlineDataCaches();
        return result;
    }

    public KlineRebuildResultDTO rebuildKlineData(KlineDataRequests.RebuildKlineRequest request) {
        authService.requireAdminRole();
        String operator = authService.currentOperatorEmail();

        String market = normalizeMarket(request.market());
        String period = normalizePeriod(request.period());
        String startDate = normalizeDate(request.startDate(), "开始日期");
        String endDate = normalizeDate(request.endDate(), "结束日期");
        validateDateRange(startDate, endDate);

        boolean allCodes = Boolean.TRUE.equals(request.allCodes());
        List<String> fetchCodes = resolveRebuildFetchCodes(market, request.codes(), allCodes);
        List<String> deleteCodes = allCodes ? null : fetchCodes;

        long previewDeleteCount = marketDataInfra.countHistoryBarsForDelete(
                market, period, deleteCodes, startDate, endDate, null);
        int taskCount = (int) Math.ceil((double) fetchCodes.size() / MAX_CODES);
        boolean confirm = Boolean.TRUE.equals(request.confirm());

        KlineRebuildResultDTO result = new KlineRebuildResultDTO();
        result.setAllCodes(allCodes);
        result.setCodeCount(fetchCodes.size());
        result.setStartDate(startDate);
        result.setEndDate(endDate);
        result.setTaskCount(taskCount);
        result.setCodesPreview(fetchCodes.stream().limit(8).toList());
        result.setPreviewDeleteCount(previewDeleteCount);

        if (!confirm) {
            result.setPreview(true);
            result.setDeletedCount(0);
            result.setTaskIds(List.of());
            return result;
        }

        if (previewDeleteCount > REBUILD_PHRASE_THRESHOLD || allCodes) {
            if (request.confirmPhrase() == null
                    || !REBUILD_CONFIRM_PHRASE.equals(request.confirmPhrase().trim())) {
                throw new BusinessException("大范围重建请在 confirmPhrase 中输入 " + REBUILD_CONFIRM_PHRASE);
            }
        }

        if (previewDeleteCount > DELETE_CONFIRM_THRESHOLD) {
            throw new BusinessException("待删除条数超过 " + DELETE_CONFIRM_THRESHOLD + "，请缩小日期或代码范围");
        }

        if (klineDataTaskInfra.countActiveTasks() + taskCount > MAX_ACTIVE_TASKS + MAX_REBUILD_BATCH_TASKS) {
            throw new BusinessException("当前活跃任务过多，请稍后再试或减少代码批次");
        }

        long deleted = marketDataInfra.deleteHistoryBars(
                market, period, deleteCodes, startDate, endDate, null);

        List<String> taskIds = new ArrayList<>();
        for (int i = 0; i < fetchCodes.size(); i += MAX_CODES) {
            List<String> batch = fetchCodes.subList(i, Math.min(i + MAX_CODES, fetchCodes.size()));
            KlineDataTaskDocument saved = saveFetchTaskDocument(
                    market, period, batch, startDate, endDate, operator, "REBUILD");
            taskIds.add(saved.getId());
        }

        result.setPreview(false);
        result.setDeletedCount(deleted);
        result.setTaskIds(taskIds);

        auditManager.log("kline.data.rebuild", operator, operator, "kline_history", market,
                "deleted=" + deleted + ", tasks=" + taskIds.size() + ", codes=" + fetchCodes.size()
                        + ", range=" + startDate + "~" + endDate + ", allCodes=" + allCodes);
        adminDataMgmtCache.invalidateKlineDataCaches();
        return result;
    }

    private List<String> resolveRebuildFetchCodes(String market, List<String> codes, boolean allCodes) {
        if (allCodes) {
            List<String> all = marketDataInfra.findStockInfos(market).stream()
                    .map(stock -> stock.getCode())
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> code.trim().toUpperCase(Locale.ROOT))
                    .distinct()
                    .sorted()
                    .toList();
            if (all.isEmpty()) {
                throw new BusinessException("股票池为空，请先在 us_stock_info 维护代码");
            }
            return new ArrayList<>(all);
        }
        return normalizeCodes(codes);
    }

    private KlineDataTaskDocument saveFetchTaskDocument(String market, String period, List<String> codes,
                                                        String startDate, String endDate,
                                                        String operator, String taskType) {
        long now = System.currentTimeMillis();
        KlineDataTaskDocument document = new KlineDataTaskDocument();
        document.setId("task_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        document.setType(taskType);
        document.setStatus("PENDING");
        document.setMarket(market);
        document.setPeriod(period);
        document.setCodes(codes);
        document.setStartDate(startDate);
        document.setEndDate(endDate);

        KlineDataTaskDocument.KlineTaskProgress progress = new KlineDataTaskDocument.KlineTaskProgress();
        progress.setTotalCodes(codes.size());
        progress.setDoneCodes(0);
        progress.setSavedBars(0);
        progress.setFailedCodes(0);
        document.setProgress(progress);

        KlineDataTaskDocument.KlineTaskResult taskResult = new KlineDataTaskDocument.KlineTaskResult();
        taskResult.setSavedBars(0);
        taskResult.setFailedCodes(new ArrayList<>());
        document.setResult(taskResult);

        document.setCreatedBy(operator);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        return klineDataTaskInfra.save(document);
    }

    private KlineFetchTaskDTO toDto(KlineDataTaskDocument document) {
        KlineFetchTaskDTO dto = new KlineFetchTaskDTO();
        dto.setId(document.getId());
        dto.setType(document.getType());
        dto.setStatus(document.getStatus());
        dto.setMarket(document.getMarket());
        dto.setPeriod(document.getPeriod());
        dto.setCodes(document.getCodes());
        dto.setStartDate(document.getStartDate());
        dto.setEndDate(document.getEndDate());
        dto.setCreatedBy(document.getCreatedBy());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setStartedAt(document.getStartedAt());
        dto.setFinishedAt(document.getFinishedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setErrorMessage(document.getErrorMessage());

        if (document.getProgress() != null) {
            KlineFetchTaskDTO.KlineTaskProgressDTO progress = new KlineFetchTaskDTO.KlineTaskProgressDTO();
            progress.setTotalCodes(document.getProgress().getTotalCodes());
            progress.setDoneCodes(document.getProgress().getDoneCodes());
            progress.setCurrentCode(document.getProgress().getCurrentCode());
            progress.setSavedBars(document.getProgress().getSavedBars());
            progress.setFailedCodes(document.getProgress().getFailedCodes());
            dto.setProgress(progress);
        }

        if (document.getResult() != null) {
            KlineFetchTaskDTO.KlineTaskResultDTO result = new KlineFetchTaskDTO.KlineTaskResultDTO();
            result.setSavedBars(document.getResult().getSavedBars());
            result.setFailedCodes(document.getResult().getFailedCodes());
            result.setErrorSummary(document.getResult().getErrorSummary());
            dto.setResult(result);
        }
        return dto;
    }

    private record DateRange(String startDate, String endDate, List<String> dates) {
    }

    private DateRange resolveDeleteDates(KlineDataRequests.DeleteKlineRequest request) {
        if (request.dates() != null && !request.dates().isEmpty()) {
            List<String> dates = request.dates().stream()
                    .map(date -> normalizeDate(date, "日期"))
                    .distinct()
                    .sorted()
                    .toList();
            return new DateRange(null, null, dates);
        }
        String startDate = normalizeDate(request.startDate(), "开始日期");
        String endDate = normalizeDate(request.endDate(), "结束日期");
        validateDateRange(startDate, endDate);
        return new DateRange(startDate, endDate, null);
    }

    private List<String> normalizeCodesOptional(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return null;
        }
        return normalizeCodes(codes);
    }

    private List<String> normalizeCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new BusinessException("请至少指定一个股票代码");
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String raw : codes) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            for (String part : raw.split("[,\\s\\n]+")) {
                if (part.isBlank()) {
                    continue;
                }
                String code = part.trim().toUpperCase(Locale.ROOT);
                if (code.endsWith(".US")) {
                    code = code.substring(0, code.length() - 3);
                }
                normalized.add(code);
            }
        }
        if (normalized.isEmpty()) {
            throw new BusinessException("请至少指定一个有效股票代码");
        }
        if (normalized.size() > MAX_CODES) {
            throw new BusinessException("单次最多支持 " + MAX_CODES + " 个代码");
        }
        return new ArrayList<>(normalized);
    }

    private String normalizeMarket(String market) {
        if (market == null || market.isBlank()) {
            return "US";
        }
        return market.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePeriod(String period) {
        if (period == null || period.isBlank()) {
            return "1d";
        }
        String normalized = period.trim().toLowerCase(Locale.ROOT);
        if (!"1d".equals(normalized)) {
            throw new BusinessException("当前仅支持日 K（1d）");
        }
        return normalized;
    }

    private String normalizeDate(String date, String label) {
        if (date == null || date.isBlank()) {
            throw new BusinessException(label + "不能为空");
        }
        try {
            return LocalDate.parse(date.trim(), DATE_FORMAT).format(DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(label + "格式应为 YYYY-MM-DD");
        }
    }

    private void validateDateRange(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DATE_FORMAT);
        LocalDate end = LocalDate.parse(endDate, DATE_FORMAT);
        if (start.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (ChronoUnit.YEARS.between(start, end) > MAX_DATE_RANGE_YEARS) {
            throw new BusinessException("日期区间不能超过 " + MAX_DATE_RANGE_YEARS + " 年");
        }
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 20;
        }
        return Math.min(pageSize, 100);
    }
}

package com.globalstock.webapi.controller;

import com.globalstock.webapi.common.ApiResponse;
import com.globalstock.webapi.model.dto.AuditLogDTO;
import com.globalstock.webapi.model.dto.DashboardStatsDTO;
import com.globalstock.webapi.model.dto.PurchaseOrderDTO;
import com.globalstock.webapi.model.dto.KlineBarDTO;
import com.globalstock.webapi.model.dto.QuantDataOverviewDTO;
import com.globalstock.webapi.model.dto.QuantScreenDTO;
import com.globalstock.webapi.model.dto.QuantScreenPageDTO;
import com.globalstock.webapi.model.dto.UserAccountDTO;
import com.globalstock.webapi.model.dto.KlineDeleteResultDTO;
import com.globalstock.webapi.model.dto.KlineFetchTaskDTO;
import com.globalstock.webapi.model.dto.KlineFetchTaskPageDTO;
import com.globalstock.webapi.model.dto.KlineHistoryBarPageDTO;
import com.globalstock.webapi.model.dto.KlineRebuildResultDTO;
import com.globalstock.webapi.model.dto.KlineTradeDatePageDTO;
import com.globalstock.webapi.model.dto.UsDataMgmtOverviewDTO;
import com.globalstock.webapi.model.dto.UsStockPageDTO;
import com.globalstock.webapi.model.request.AdminRequests;
import com.globalstock.webapi.model.request.KlineDataRequests;
import com.globalstock.webapi.model.request.AuthRequests;
import com.globalstock.webapi.model.request.CommerceRequests;
import com.globalstock.webapi.service.AdminDataMgmtService;
import com.globalstock.webapi.service.AdminKlineDataService;
import com.globalstock.webapi.service.AdminQuantService;
import com.globalstock.webapi.service.AdminService;
import com.globalstock.webapi.service.CommerceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/v1")
public class AdminController {

    private final AdminService adminService;
    private final CommerceService commerceService;
    private final AdminQuantService adminQuantService;
    private final AdminKlineDataService adminKlineDataService;
    private final AdminDataMgmtService adminDataMgmtService;

    public AdminController(AdminService adminService,
                           CommerceService commerceService,
                           AdminQuantService adminQuantService,
                           AdminKlineDataService adminKlineDataService,
                           AdminDataMgmtService adminDataMgmtService) {
        this.adminService = adminService;
        this.commerceService = commerceService;
        this.adminQuantService = adminQuantService;
        this.adminKlineDataService = adminKlineDataService;
        this.adminDataMgmtService = adminDataMgmtService;
    }

    @GetMapping("/dashboard/stats")
    public ApiResponse<DashboardStatsDTO> dashboardStats() {
        return ApiResponse.success(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ApiResponse<List<UserAccountDTO>> listUsers() {
        return ApiResponse.success(adminService.listUsers());
    }

    @PostMapping("/users")
    public ApiResponse<UserAccountDTO> createUser(@RequestBody AuthRequests.CreateUserRequest request) {
        return ApiResponse.success(adminService.createUser(request));
    }

    @PatchMapping("/users/{email}/role")
    public ApiResponse<UserAccountDTO> updateUserRole(@PathVariable String email,
                                                      @RequestBody AuthRequests.UpdateRoleRequest request) {
        return ApiResponse.success(adminService.updateUserRole(email, request));
    }

    @PatchMapping("/users/{email}/status")
    public ApiResponse<UserAccountDTO> updateUserStatus(@PathVariable String email,
                                                        @RequestBody AdminRequests.UpdateStatusRequest request) {
        return ApiResponse.success(adminService.updateUserStatus(email, request));
    }

    @GetMapping("/orders")
    public ApiResponse<List<PurchaseOrderDTO>> listOrders(
            @RequestParam(defaultValue = "pending_review") String status,
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.success(commerceService.listOrdersForReview(status, limit));
    }

    @PostMapping("/orders/{id}/approve")
    public ApiResponse<PurchaseOrderDTO> approveOrder(@PathVariable String id) {
        return ApiResponse.success(commerceService.approveOrder(id));
    }

    @PostMapping("/orders/{id}/reject")
    public ApiResponse<PurchaseOrderDTO> rejectOrder(@PathVariable String id,
                                                     @RequestBody CommerceRequests.RejectOrderRequest request) {
        return ApiResponse.success(commerceService.rejectOrder(id, request));
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AuditLogDTO>> listAuditLogs(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(adminService.listAuditLogs(type, limit));
    }

    @GetMapping("/quant/us/overview")
    public ApiResponse<QuantDataOverviewDTO> quantUsOverview() {
        return ApiResponse.success(adminQuantService.getUsOverview());
    }

    @GetMapping("/quant/us/screen")
    public ApiResponse<QuantScreenDTO> quantUsScreen(
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Double minMarketCap,
            @RequestParam(required = false) Double maxMarketCap) {
        return ApiResponse.success(adminQuantService.screenUsMarket(days, limit, code, minMarketCap, maxMarketCap));
    }

    @GetMapping("/quant/us/screen/page")
    public ApiResponse<QuantScreenPageDTO> quantUsScreenPage(
            @RequestParam(defaultValue = "new_high") String signalType,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) String period,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Double minMarketCap,
            @RequestParam(required = false) Double maxMarketCap,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        return ApiResponse.success(adminQuantService.screenUsMarketPage(
                signalType, days, period, page, pageSize, code, minMarketCap, maxMarketCap,
                sortBy, sortOrder));
    }

    @GetMapping("/quant/us/kline")
    public ApiResponse<List<KlineBarDTO>> quantUsKline(
            @RequestParam String code,
            @RequestParam(required = false) Integer limit) {
        return ApiResponse.success(adminQuantService.getUsHistoryBars(code, limit));
    }

    @GetMapping("/data-mgmt/us/overview")
    public ApiResponse<UsDataMgmtOverviewDTO> dataMgmtUsOverview(
            @RequestParam(required = false, defaultValue = "false") Boolean refresh) {
        return ApiResponse.success(adminDataMgmtService.getUsOverview(Boolean.TRUE.equals(refresh)));
    }

    @GetMapping("/data-mgmt/us/stocks")
    public ApiResponse<UsStockPageDTO> dataMgmtUsStocks(
            @RequestParam(required = false) String code,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "false") Boolean refresh) {
        return ApiResponse.success(adminDataMgmtService.listUsStocks(
                code, page, pageSize, Boolean.TRUE.equals(refresh)));
    }

    @GetMapping("/data-mgmt/us/kline/bars")
    public ApiResponse<KlineHistoryBarPageDTO> dataMgmtUsKlineBars(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "false") Boolean refresh) {
        return ApiResponse.success(adminDataMgmtService.listKlineHistoryBars(
                code, date, startDate, endDate, page, pageSize, Boolean.TRUE.equals(refresh)));
    }

    @GetMapping("/data-mgmt/us/kline/dates")
    public ApiResponse<KlineTradeDatePageDTO> dataMgmtUsKlineDates(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false, defaultValue = "false") Boolean refresh) {
        return ApiResponse.success(adminDataMgmtService.listKlineTradeDates(
                startDate, endDate, page, pageSize, Boolean.TRUE.equals(refresh)));
    }

    @PostMapping("/data-mgmt/us/kline/fetch-tasks")
    public ApiResponse<KlineFetchTaskDTO> createKlineFetchTask(
            @RequestBody KlineDataRequests.CreateFetchTaskRequest request) {
        return ApiResponse.success(adminKlineDataService.createFetchTask(request));
    }

    @GetMapping("/data-mgmt/us/kline/fetch-tasks")
    public ApiResponse<KlineFetchTaskPageDTO> listKlineFetchTasks(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return ApiResponse.success(adminKlineDataService.listFetchTasks(status, page, pageSize));
    }

    @GetMapping("/data-mgmt/us/kline/fetch-tasks/{taskId}")
    public ApiResponse<KlineFetchTaskDTO> getKlineFetchTask(@PathVariable String taskId) {
        return ApiResponse.success(adminKlineDataService.getFetchTask(taskId));
    }

    @PostMapping("/data-mgmt/us/kline/fetch-tasks/{taskId}/cancel")
    public ApiResponse<KlineFetchTaskDTO> cancelKlineFetchTask(@PathVariable String taskId) {
        return ApiResponse.success(adminKlineDataService.cancelFetchTask(taskId));
    }

    @PostMapping("/data-mgmt/us/kline/rebuild")
    public ApiResponse<KlineRebuildResultDTO> rebuildKlineData(
            @RequestBody KlineDataRequests.RebuildKlineRequest request) {
        return ApiResponse.success(adminKlineDataService.rebuildKlineData(request));
    }

    @PostMapping("/data-mgmt/us/kline/delete")
    public ApiResponse<KlineDeleteResultDTO> deleteKlineData(
            @RequestBody KlineDataRequests.DeleteKlineRequest request) {
        return ApiResponse.success(adminKlineDataService.deleteKlineData(request));
    }
}

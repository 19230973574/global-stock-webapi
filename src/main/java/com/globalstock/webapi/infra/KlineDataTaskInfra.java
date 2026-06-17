package com.globalstock.webapi.infra;

import com.globalstock.webapi.common.SystemException;
import com.globalstock.webapi.mapper.KlineDataTaskMapper;
import com.globalstock.webapi.model.document.KlineDataTaskDocument;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KlineDataTaskInfra {

    private final KlineDataTaskMapper klineDataTaskMapper;

    public KlineDataTaskInfra(KlineDataTaskMapper klineDataTaskMapper) {
        this.klineDataTaskMapper = klineDataTaskMapper;
    }

    public KlineDataTaskDocument save(KlineDataTaskDocument document) {
        try {
            return klineDataTaskMapper.save(document);
        } catch (Exception exception) {
            throw new SystemException("保存 K 线任务失败", exception);
        }
    }

    public KlineDataTaskDocument findById(String id) {
        try {
            return klineDataTaskMapper.findById(id);
        } catch (Exception exception) {
            throw new SystemException("查询 K 线任务失败", exception);
        }
    }

    public List<KlineDataTaskDocument> findPage(String status, int page, int pageSize) {
        try {
            return klineDataTaskMapper.findPage(status, page, pageSize);
        } catch (Exception exception) {
            throw new SystemException("查询 K 线任务列表失败", exception);
        }
    }

    public long count(String status) {
        try {
            return klineDataTaskMapper.count(status);
        } catch (Exception exception) {
            throw new SystemException("统计 K 线任务失败", exception);
        }
    }

    public long countActiveTasks() {
        try {
            return klineDataTaskMapper.countActiveTasks();
        } catch (Exception exception) {
            throw new SystemException("统计活跃 K 线任务失败", exception);
        }
    }

    public List<KlineDataTaskDocument> findActiveTasks() {
        try {
            return klineDataTaskMapper.findActiveTasks();
        } catch (Exception exception) {
            throw new SystemException("查询活跃 K 线任务失败", exception);
        }
    }

    public boolean cancelPending(String id) {
        try {
            return klineDataTaskMapper.cancelPending(id);
        } catch (Exception exception) {
            throw new SystemException("取消 K 线任务失败", exception);
        }
    }
}

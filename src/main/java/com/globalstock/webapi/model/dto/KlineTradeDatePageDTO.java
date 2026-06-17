package com.globalstock.webapi.model.dto;

import java.util.List;

public class KlineTradeDatePageDTO {

    private int page;
    private int pageSize;
    private long total;
    private List<KlineTradeDateItemDTO> items;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<KlineTradeDateItemDTO> getItems() {
        return items;
    }

    public void setItems(List<KlineTradeDateItemDTO> items) {
        this.items = items;
    }
}

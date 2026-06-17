package com.globalstock.webapi.model.dto;

import java.util.List;

public class KlineHistoryBarPageDTO {

    private int page;
    private int pageSize;
    private long total;
    private List<KlineBarDTO> items;

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

    public List<KlineBarDTO> getItems() {
        return items;
    }

    public void setItems(List<KlineBarDTO> items) {
        this.items = items;
    }
}

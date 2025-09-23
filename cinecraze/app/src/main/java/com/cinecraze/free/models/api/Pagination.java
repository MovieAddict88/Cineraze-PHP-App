package com.cinecraze.free.models.api;

import com.google.gson.annotations.SerializedName;

public class Pagination {
    @SerializedName("total_items")
    private int totalItems;

    @SerializedName("page")
    private int page;

    @SerializedName("limit")
    private int limit;

    @SerializedName("total_pages")
    private int totalPages;

    public int getTotalItems() {
        return totalItems;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public int getTotalPages() {
        return totalPages;
    }
}

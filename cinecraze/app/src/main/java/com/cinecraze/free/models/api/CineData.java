package com.cinecraze.free.models.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CineData {
    @SerializedName("pagination")
    private Pagination pagination;

    @SerializedName("categories")
    private List<Category> categories;

    public Pagination getPagination() {
        return pagination;
    }

    public List<Category> getCategories() {
        return categories;
    }
}

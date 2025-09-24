package com.cinecraze.free.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

package com.cinecraze.free.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse {

    @SerializedName("pagination")
    private Pagination pagination;

    @SerializedName("categories")
    private List<Category> categories;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }
}

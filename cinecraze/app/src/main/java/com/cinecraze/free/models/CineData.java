package com.cinecraze.free.models;

import java.util.List;

public class CineData {
    private Pagination pagination;
    private List<Category> categories;

    public Pagination getPagination() {
        return pagination;
    }

    public List<Category> getCategories() {
        return categories;
    }
}

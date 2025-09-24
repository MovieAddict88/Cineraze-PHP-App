package com.cinecraze.free.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cinecraze.free.R;

public class HomeFragment extends BaseFragment {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    protected void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        carouselViewPager = view.findViewById(R.id.carousel_viewpager);
        fabViewMode = view.findViewById(R.id.fab_view_mode);
        
        btnYearFilter = view.findViewById(R.id.btn_year_filter);
    }

    @Override
    protected String getCategory() {
        return ""; // Empty string for all categories (Home shows everything)
    }
    

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }
}
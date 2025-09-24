package com.cinecraze.free.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.cinecraze.free.CarouselAdapter;
import com.cinecraze.free.FilterSpinner;
import com.cinecraze.free.FragmentMainActivity;
import com.cinecraze.free.MovieAdapter;
import com.cinecraze.free.R;
import com.cinecraze.free.models.Entry;
import com.cinecraze.free.repository.DataRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFragment extends Fragment {

    protected RecyclerView recyclerView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected MovieAdapter movieAdapter;
    protected List<Entry> currentPageEntries = new ArrayList<>();
    protected ViewPager2 carouselViewPager;
    protected CarouselAdapter carouselAdapter;
    protected FloatingActionButton fabViewMode;
    
    // Filter UI elements
    protected MaterialButton btnYearFilter;
    protected FilterSpinner yearSpinner;
    
    // Filter variables
    protected String currentYearFilter = null;
    protected boolean isLoading = false;

    protected boolean isGridView = true;
    protected DataRepository dataRepository;
    
    // Pagination variables
    protected int currentPage = 0;
    protected int pageSize = 20;
    protected boolean hasMorePages = false;
    protected int totalCount = 0;
    protected String currentCategory = "";
    protected String currentSearchQuery = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentCategory = getCategory();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataRepository = new DataRepository(getContext());
        initializeViews(view);
        setupRecyclerView();
        setupCarousel();
        setupViewSwitch();
        setupPagination();
        setupFilters();
        setupSwipeRefresh();
        loadInitialData();
    }

    protected abstract void initializeViews(View view);
    protected abstract String getCategory();
    protected abstract int getLayoutId();

    protected void setupRecyclerView() {
        if (recyclerView != null) {
            movieAdapter = new MovieAdapter(getContext(), currentPageEntries, isGridView);
            updateViewMode();
            
            // Add scroll listener for lazy loading and to hide/show bottom navigation
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                private int lastDy = 0;

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if (!isLoading && hasMorePages) {
                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                    && firstVisibleItemPosition >= 0
                                    && totalItemCount >= pageSize) {
                                currentPage++;
                                loadPageData();
                            }
                        }
                    }

                    // Hide/show bottom navigation
                    if (getActivity() instanceof FragmentMainActivity) {
                        FragmentMainActivity activity = (FragmentMainActivity) getActivity();
                        if (dy > 0 && lastDy <= 0) {
                            // Scrolling down
                            activity.hideBottomNavigation();
                        } else if (dy < 0 && lastDy > 0) {
                            // Scrolling up
                            activity.showBottomNavigation();
                        }
                    }
                    lastDy = dy;
                }
            });
        }
    }

    protected void setupCarousel() {
        if (carouselViewPager != null) {
            carouselAdapter = new CarouselAdapter(getContext(), new ArrayList<>());
            carouselViewPager.setAdapter(carouselAdapter);

            // Auto-scroll handler
            final Handler handler = new Handler(Looper.getMainLooper());
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int currentItem = carouselViewPager.getCurrentItem();
                    int totalItems = carouselAdapter.getItemCount();
                    if (totalItems > 0) {
                        carouselViewPager.setCurrentItem((currentItem + 1) % totalItems, true);
                    }
                    handler.postDelayed(this, 3000); // 3 seconds delay
                }
            };
            handler.postDelayed(runnable, 3000);
        }
    }

    protected void updateViewMode() {
        if (recyclerView != null && movieAdapter != null) {
            if (isGridView) {
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                if (fabViewMode != null) fabViewMode.setImageResource(R.drawable.ic_grid_view);
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                if (fabViewMode != null) fabViewMode.setImageResource(R.drawable.ic_list_view);
            }
            movieAdapter.setGridView(isGridView);
            recyclerView.setAdapter(movieAdapter);
        }
    }

    protected void setupViewSwitch() {
        if (fabViewMode != null) {
            fabViewMode.setOnClickListener(v -> {
                isGridView = !isGridView;
                updateViewMode();
            });
        }
    }

    protected void setupFilters() {
        if (btnYearFilter != null) {
            // Initialize filter spinners only if they haven't been created yet
            if (yearSpinner == null) {
                yearSpinner = new FilterSpinner(getContext(), "Year", new ArrayList<>(), currentYearFilter);

                // Common filter selection listener
                FilterSpinner.OnFilterSelectedListener filterListener = (filterType, filterValue) -> {
                    if (filterType.equals("Year")) {
                        currentYearFilter = filterValue;
                        btnYearFilter.setText(filterValue != null ? filterValue : "Year");
                    }

                    // Reset pagination and apply filters
                    currentPage = 0;
                    currentSearchQuery = ""; // Clear search when filtering
                    loadPageData();
                };

                yearSpinner.setOnFilterSelectedListener(filterListener);

                // Set up button click listeners to show spinners
                btnYearFilter.setOnClickListener(v -> {
                    populateFilterSpinners();
                    dismissAllSpinners();
                    yearSpinner.show(btnYearFilter);
                });
            }
        }
    }
    
    protected void populateFilterSpinners() {
        if (dataRepository == null) return;
        
        // Get unique values from repository and populate spinners
        List<String> years = dataRepository.getUniqueYears();
        
        if (yearSpinner != null) {
            yearSpinner.updateFilterValues(years);
        }
    }
    
    protected void dismissAllSpinners() {
        if (yearSpinner != null && yearSpinner.isShowing()) {
            yearSpinner.dismiss();
        }
    }

    protected void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                currentPage = 0;
                currentSearchQuery = "";
                // Force fetch latest data from API, then reload the current page from cache
                if (dataRepository != null) {
                    dataRepository.fetchContent(1, pageSize, getCategory(), "newest", new DataRepository.ContentCallback() {
                        @Override
                        public void onSuccess(com.cinecraze.free.models.ApiResponse apiResponse) {
                            // After cache is updated, reload paginated data
                            loadPageData();
                        }

                        @Override
                        public void onError(String error) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (swipeRefreshLayout != null) {
                                        swipeRefreshLayout.setRefreshing(false);
                                    }
                                    Toast.makeText(getContext(), "Refresh failed: " + error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
                } else {
                    // Fallback: just reload current page
                    loadPageData();
                }
            });
        }
    }

    protected void loadInitialData() {
        currentPage = 0;
        
        // Ensure data is available before loading
        dataRepository.ensureDataAvailable(new DataRepository.DataCallback() {
            @Override
            public void onSuccess(List<Entry> entries) {
                loadPageData();
                populateFilterSpinners(); // Populate filter spinners after data is loaded
            }
            
            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to initialize: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    protected void loadPageData() {
        if (getActivity() == null) return;
        
        isLoading = true;
        
        // Use DataRepository's paginated methods for better performance
        if (!currentSearchQuery.isEmpty()) {
            loadSearchPageData();
        } else if (!currentCategory.isEmpty()) {
            loadCategoryPageData();
        } else {
            loadAllPageData();
        }
    }
    
    protected void loadAllPageData() {
        dataRepository.getPaginatedData(currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                updatePageData(entries, hasMorePages, totalCount);
            }
            
            @Override
            public void onError(String error) {
                handlePageLoadError(error);
            }
        });
    }
    
    protected void loadCategoryPageData() {
        dataRepository.getPaginatedDataByCategory(currentCategory, currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                updatePageData(entries, hasMorePages, totalCount);
            }
            
            @Override
            public void onError(String error) {
                handlePageLoadError(error);
            }
        });
    }
    
    protected void loadSearchPageData() {
        dataRepository.searchPaginated(currentSearchQuery, currentPage, pageSize, new DataRepository.PaginatedDataCallback() {
            @Override
            public void onSuccess(List<Entry> entries, boolean hasMorePages, int totalCount) {
                updatePageData(entries, hasMorePages, totalCount);
            }
            
            @Override
            public void onError(String error) {
                handlePageLoadError(error);
            }
        });
    }
    
    
    protected void updatePageData(List<Entry> entries, boolean hasMorePages, int totalCount) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            this.hasMorePages = hasMorePages;
            this.totalCount = totalCount;
            this.isLoading = false;
            
            currentPageEntries.clear();
            currentPageEntries.addAll(entries);
            
            if (movieAdapter != null) {
                movieAdapter.setEntryList(currentPageEntries);
            }
            updatePaginationUI();
            
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            
            // Load carousel data if this is the first page and home fragment
            if (currentPage == 0 && carouselAdapter != null && getCategory().isEmpty()) {
                List<Entry> topRatedEntries = dataRepository.getTopRatedEntries(10);
                carouselAdapter.setEntries(topRatedEntries);
                carouselAdapter.notifyDataSetChanged();
            }
        });
    }
    
    protected void handlePageLoadError(String error) {
        if (getActivity() == null) return;
        
        getActivity().runOnUiThread(() -> {
            isLoading = false;
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Toast.makeText(getContext(), "Failed to load page: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    protected void filterByQuery(String query) {
        currentSearchQuery = query;
        currentPage = 0;
        loadPageData();
    }

    // Public method to be called from MainActivity for search
    public void performSearch(String query) {
        filterByQuery(query);
    }
}
package com.cinecraze.free;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.cinecraze.free.R;
import com.cinecraze.free.ui.MainPagerAdapter;
import com.cinecraze.free.repository.DataRepository;
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentMainActivity extends AppCompatActivity {

    private BubbleNavigationConstraintView bottomNavigationView;
    private ImageView closeSearchIcon;
    private LinearLayout searchLayout;
    private AutoCompleteTextView searchBar;
    private FloatingActionButton floatingSearchIcon;
    private ViewPager2 mainViewPager;
    private MainPagerAdapter pagerAdapter;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    private boolean isSearchVisible = false;
    private boolean isProgrammaticChange = false;

    private DataRepository dataRepository;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupStatusBar();
        setContentView(R.layout.activity_main_fragment);

        dataRepository = new DataRepository(this);

        initializeViews();

        syncDataWithProgress();
    }

    private void syncDataWithProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Syncing data...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        dataRepository.syncRemoteData(new DataRepository.DataCallback() {
            @Override
            public void onSuccess() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(FragmentMainActivity.this, "Data synced successfully", Toast.LENGTH_SHORT).show();
                startFragments();
            }

            @Override
            public void onError(String error) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(FragmentMainActivity.this, "Sync failed: " + error, Toast.LENGTH_LONG).show();
                startFragments();
            }
        });
    }

    private void startFragments() {
        setupViewPager();
        setupBottomNavigation();
        setupSearch();
        applyInitialTabFromIntent();
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.BLACK);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decor = window.getDecorView();
                decor.setSystemUiVisibility(0);
            }
        }
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        closeSearchIcon = findViewById(R.id.close_search_icon);
        searchLayout = findViewById(R.id.search_layout);
        searchBar = findViewById(R.id.search_bar);
        floatingSearchIcon = findViewById(R.id.floating_search_icon);
        mainViewPager = findViewById(R.id.main_viewpager);
    }

    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(this);
        mainViewPager.setAdapter(pagerAdapter);
        mainViewPager.setOffscreenPageLimit(1);

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (!isProgrammaticChange) {
                    bottomNavigationView.setCurrentActiveItem(position);
                }
                updateSearchIconVisibility(position);
            }
        };
        mainViewPager.registerOnPageChangeCallback(pageChangeCallback);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setNavigationChangeListener((view, position) -> {
            isProgrammaticChange = true;
            mainViewPager.setCurrentItem(position, true);
            updateSearchIconVisibility(position);
            mainViewPager.post(() -> isProgrammaticChange = false);
        });
        bottomNavigationView.setCurrentActiveItem(0);
    }

    private void updateSearchIconVisibility(int position) {
        if (position == 0) {
            floatingSearchIcon.setVisibility(View.VISIBLE);
        } else {
            floatingSearchIcon.setVisibility(View.GONE);
        }
    }

    private void setupSearch() {
        floatingSearchIcon.setOnClickListener(v -> toggleSearch());
        closeSearchIcon.setOnClickListener(v -> hideSearch());
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(searchBar.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void toggleSearch() {
        if (isSearchVisible) {
            hideSearch();
        } else {
            showSearch();
        }
    }

    private void showSearch() {
        searchLayout.setVisibility(View.VISIBLE);
        searchLayout.animate().translationY(0).alpha(1.0f).setDuration(300).start();
        searchBar.requestFocus();
        isSearchVisible = true;
    }

    private void hideSearch() {
        searchLayout.animate().translationY(-searchLayout.getHeight()).alpha(0.0f).setDuration(300)
                .withEndAction(() -> {
                    searchLayout.setVisibility(View.GONE);
                    searchBar.setText("");
                }).start();
        isSearchVisible = false;
    }

    private void performSearch(String query) {
        int currentItem = mainViewPager.getCurrentItem();
        androidx.fragment.app.Fragment currentFragment = pagerAdapter.getFragmentAt(currentItem);
        if (currentFragment instanceof com.cinecraze.free.ui.BaseFragment) {
            ((com.cinecraze.free.ui.BaseFragment) currentFragment).performSearch(query);
        }
        hideSearch();
    }

    private void applyInitialTabFromIntent() {
        try {
            int tab = getIntent().getIntExtra("initial_tab", 0);
            if (tab >= 0 && tab < pagerAdapter.getItemCount()) {
                isProgrammaticChange = true;
                mainViewPager.setCurrentItem(tab, false);
                bottomNavigationView.setCurrentActiveItem(tab);
                updateSearchIconVisibility(tab);
                mainViewPager.post(() -> isProgrammaticChange = false);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onBackPressed() {
        if (isSearchVisible) {
            hideSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mainViewPager != null && pageChangeCallback != null) {
            mainViewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
    }

    public void hideBottomNavigation() {
        if (bottomNavigationView.getVisibility() == View.VISIBLE) {
            bottomNavigationView.animate().translationY(bottomNavigationView.getHeight()).setDuration(300)
                    .withEndAction(() -> bottomNavigationView.setVisibility(View.GONE)).start();
            setViewPagerMargin(0);
        }
    }

    public void showBottomNavigation() {
        if (bottomNavigationView.getVisibility() == View.GONE) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            bottomNavigationView.animate().translationY(0).setDuration(300).start();
            setViewPagerMargin(60);
        }
    }

    private void setViewPagerMargin(int bottomMarginInDp) {
        if (mainViewPager != null) {
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mainViewPager.getLayoutParams();
            float density = getResources().getDisplayMetrics().density;
            int bottomMarginInPixels = (int) (bottomMarginInDp * density);
            params.setMargins(0, 0, 0, bottomMarginInPixels);
            mainViewPager.setLayoutParams(params);
        }
    }
}
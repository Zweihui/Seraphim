package com.zwh.mvparms.eyepetizer.mvp.ui.activity;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.jess.arms.base.BaseLazyLoadFragment;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.UiUtils;
import com.zwh.annotation.apt.Router;
import com.zwh.mvparms.eyepetizer.R;
import com.zwh.mvparms.eyepetizer.app.EventBusTags;
import com.zwh.mvparms.eyepetizer.app.constants.Constants;
import com.zwh.mvparms.eyepetizer.app.utils.helper.MainFragmentAdapter;
import com.zwh.mvparms.eyepetizer.mvp.ui.fragment.CategoryFragment;
import com.zwh.mvparms.eyepetizer.mvp.ui.fragment.HomeFragment;
import com.zwh.mvparms.eyepetizer.mvp.ui.widget.BottomNavigationViewHelper;
import com.zwh.mvparms.eyepetizer.mvp.ui.widget.CustomViewPager;
import com.zwh.mvparms.eyepetizer.mvp.ui.widget.MaterialSearchView;
import com.zwh.mvparms.eyepetizer.mvp.ui.widget.transition.SearchTransitioner;
import com.zwh.mvparms.eyepetizer.mvp.ui.widget.video.SampleVideo;

import org.simple.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.keyHeight;
import static com.zwh.mvparms.eyepetizer.R.id.toolbar;
import static com.zwh.mvparms.eyepetizer.R.id.view;

/**
 * Created by zwh on 2017/9/15 0015.
 */
@Router(Constants.MAIN)
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;
    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;
    @BindView(toolbar)
    Toolbar mToolbar;
//    @BindView(R.id.appbar)
//    AppBarLayout mAppbar;
    @BindView(R.id.nv_main_navigation)
    NavigationView mNvMainNavigation;
    @BindView(R.id.dl_main_drawer)
    DrawerLayout mDlMainDrawer;
    ActionBarDrawerToggle mToggle;
    @BindView(R.id.viewpager)
    CustomViewPager mViewpager;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    private HomeFragment mHomeFragment;
    private CategoryFragment mCategoryFragment;

    private SearchTransitioner searchTransitioner;

    private long firstTime = 0;
    private int usableHeightPrevious;
    boolean isSoftKeyBoardShow = false;

    @Override
    public void setupActivityComponent(AppComponent appComponent) {

    }

    @Override
    public int initView(Bundle savedInstanceState) {
        return R.layout.activity_main;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        initFragment();
        BottomNavigationViewHelper.disableShiftMode(bottomNavigation);
        setSupportActionBar(mToolbar);
        mToggle = new ActionBarDrawerToggle(
                this, mDlMainDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDlMainDrawer.setDrawerListener(mToggle);
        mToggle.syncState();
        mNvMainNavigation.setNavigationItemSelectedListener(this);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_home:
                        mViewpager.setCurrentItem(0);
                        setTitle("首页");
                        break;
                    case R.id.item_category:
                        mViewpager.setCurrentItem(1);
                        setTitle("分类");
                        break;
                    case R.id.item_attention:
                        mViewpager.setCurrentItem(2);
                        setTitle("关注");
                        break;
                    case R.id.item_mine:
                        mViewpager.setCurrentItem(3);
                        setTitle("我的");
                        break;
                }
                return true;
            }
        });
        bottomNavigation.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.item_home && mHomeFragment.isVisible()) {
                    EventBus.getDefault().post("showLoading", EventBusTags.REFRESH_HOME_DATA);
                }
            }
        });
        bottomNavigation.setSelectedItemId(R.id.item_home);
        mDlMainDrawer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        initToolBar();
        searchTransitioner = new SearchTransitioner(this,((CardView)mDlMainDrawer.findViewById(R.id.card_search)),searchView);
    }

    private void initToolBar() {
        searchView.setToolbar(mToolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItem = item.getItemId();
                switch (menuItem) {
                    case R.id.action_search:
                        searchView.showView();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        searchView.findViewById(R.id.clearSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSearch();
            }
        });
    }

    private void gotoSearch(){
        searchTransitioner.transitionToSearch();
    }

    private void initFragment() {
        mHomeFragment = HomeFragment.newInstance();
        mCategoryFragment = CategoryFragment.newInstance();
        List<BaseLazyLoadFragment> list = new ArrayList<>();
        list.add(mHomeFragment);
        list.add(mCategoryFragment);
        list.add(HomeFragment.newInstance());
        list.add(HomeFragment.newInstance());
        mViewpager.setOffscreenPageLimit(4);
        mViewpager.setPagingEnabled(false);
        mViewpager.setAdapter(MainFragmentAdapter.newInstance(getSupportFragmentManager(), list));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_manage) ;
        else if (item.getItemId() == R.id.nav_share) ;
        else if (item.getItemId() == R.id.nav_theme) ;
        mDlMainDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mDlMainDrawer.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                isSoftKeyBoardShow = true;
            } else {
                isSoftKeyBoardShow = false;
                if (searchView.isShowing())
                    searchView.showView();
            }
            mDlMainDrawer.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mDlMainDrawer.getWindowVisibleDisplayFrame(r);
        return (r.bottom - r.top);
    }

    @Override
    public void onBackPressed() {
        if (mDlMainDrawer.isDrawerOpen(GravityCompat.START)) {
            mDlMainDrawer.closeDrawer(GravityCompat.START);
            return;
        }

        if (System.currentTimeMillis() - firstTime > 2000) {
            UiUtils.snackbarText("再按一次退出应用");
            firstTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }
}
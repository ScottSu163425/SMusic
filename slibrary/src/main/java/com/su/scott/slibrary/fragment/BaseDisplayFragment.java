package com.su.scott.slibrary.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.su.scott.slibrary.R;
import com.su.scott.slibrary.view.BaseDisplayView;

/**
 * @类名 BaseDisplayFragment
 * @描述 基于RecyclerView、SwipeRefreshLayout的列表展示封装基类
 * @作者 Su
 * @时间 2016年7月
 */
public abstract class BaseDisplayFragment<E, VH> extends BaseFragment implements BaseDisplayView<E> {
    private View mRootView;
    private RecyclerView mDisplayRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FrameLayout mLoadingLayout;
    private FrameLayout mEmptyLayout;
    private FrameLayout mErrorLayout;
    private boolean mIsFirstTimeCreateView = true;


    protected abstract void onFirstTimeCreateView();

    protected abstract RecyclerView.Adapter getAdapter();

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract int getLoadingLayout();

    protected abstract int getEmptyLayout();

    protected abstract int getErrorLayout();

    protected abstract boolean canSwipeRefresh();

    protected abstract boolean canLoadMore();

    protected abstract void onSwipeRefresh();

    protected abstract void onLoadMore();

    protected abstract void onEmptyClick();

    protected abstract void onErrorClick();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_base_display, container, false);
            mDisplayRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view_fragment_base_display);
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout_fragment_base_display);
            mLoadingLayout = (FrameLayout) mRootView.findViewById(R.id.fl_container_loading_layout_fragment_base_display);
            mEmptyLayout = (FrameLayout) mRootView.findViewById(R.id.fl_container_empty_layout_fragment_base_display);
            mErrorLayout = (FrameLayout) mRootView.findViewById(R.id.fl_container_error_layout_fragment_base_display);

            mLoadingLayout.addView(inflater.inflate(getLoadingLayout(), container, false), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            mEmptyLayout.addView(inflater.inflate(getEmptyLayout(), container, false), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            mErrorLayout.addView(inflater.inflate(getErrorLayout(), container, false), new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

            mEmptyLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEmptyClick();
                }
            });

            mErrorLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onErrorClick();
                }
            });

            mDisplayRecyclerView.setHasFixedSize(true);
            mDisplayRecyclerView.setLayoutManager(getLayoutManager());
            mDisplayRecyclerView.setAdapter(getAdapter());

            //setup loadmore
            mDisplayRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    //上拉自动加载更多
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        //获取最后一个完全显示的ItemPosition
                        int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                        int totalItemCount = manager.getItemCount();

                        // 判断是否滚动到底部，并且是向下滚动
                        if (lastVisibleItem == (totalItemCount - 1)) {
                            //加载更多
                            if (canLoadMore()) {
                                onLoadMore();
                            }
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }
            });

            mSwipeRefreshLayout.setColorSchemeResources(R.color.md_indigo_500, R.color.md_red_500, R.color.md_green_500, R.color.md_yellow_500);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    onSwipeRefresh();
                }
            });
            mSwipeRefreshLayout.setEnabled(canSwipeRefresh());
        }
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsFirstTimeCreateView) {
            onFirstTimeCreateView();
            mIsFirstTimeCreateView = false;
        }
    }

    @Override
    public void display() {
        stopSwipeRefresh();
        mLoadingLayout.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.GONE);
        mDisplayRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading() {
        stopSwipeRefresh();
        mLoadingLayout.setVisibility(View.VISIBLE);
        mEmptyLayout.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.GONE);
        mDisplayRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void showEmpty() {
        stopSwipeRefresh();
        mLoadingLayout.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.VISIBLE);
        mErrorLayout.setVisibility(View.GONE);
        mDisplayRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void showError() {
        stopSwipeRefresh();
        mLoadingLayout.setVisibility(View.GONE);
        mEmptyLayout.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.VISIBLE);
        mDisplayRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void performSwipeRefresh() {
        if (!canSwipeRefresh()) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void stopSwipeRefresh() {
        if (!canSwipeRefresh()) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        //解决Viewpager切换重复加载
        if (null != mRootView) {
            ((ViewGroup) mRootView).removeView(mRootView);
        }
        super.onDestroy();
    }


    @Override
    public void initPreData() {

    }

    @Override
    public void initToolbar() {

    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public View getSnackbarParent() {
        return mDisplayRecyclerView;
    }

    protected RecyclerView getDisplayRecyclerView() {
        return mDisplayRecyclerView;
    }

    private int getFirstVisibleItemPosition() {
        return ((LinearLayoutManager) (getDisplayRecyclerView().getLayoutManager())).findFirstVisibleItemPosition();
    }

    private int getLastVisibleItemPosition() {
        return ((LinearLayoutManager) (getDisplayRecyclerView().getLayoutManager())).findLastVisibleItemPosition();
    }

    public VH getViewHolder(int position) {
        int firstItemPosition = getFirstVisibleItemPosition();
        if (position - firstItemPosition >= 0) {
            //得到要更新的item的view
            View view = getDisplayRecyclerView().getChildAt(position - firstItemPosition /*+ 1*/);
            if (null != getDisplayRecyclerView().getChildViewHolder(view)) {
                VH viewHolder = (VH) getDisplayRecyclerView().getChildViewHolder(view);
                return viewHolder;
                //do something
            }
        }
        return null;
    }

    public void scrollToPosition(int positon) {
        getDisplayRecyclerView().scrollToPosition(positon);
    }

    public void smoothScrollToPosition(int positon) {
        getDisplayRecyclerView().smoothScrollToPosition(positon);
    }

    public RecyclerView getRecyclerView() {
        return getDisplayRecyclerView();
    }


}

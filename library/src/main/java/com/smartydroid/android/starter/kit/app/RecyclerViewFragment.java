/**
 * Created by YuGang Yang on September 25, 2015.
 * Copyright 2007-2015 Laputapp.com. All rights reserved.
 */
package com.smartydroid.android.starter.kit.app;

import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.carlosdelachica.easyrecycleradapters.adapter.EasyRecyclerAdapter;
import com.carlosdelachica.easyrecycleradapters.decorations.DividerItemDecoration;
import com.smartydroid.android.starter.kit.R;
import com.smartydroid.android.starter.kit.contracts.Pagination.Paginator;
import com.smartydroid.android.starter.kit.model.ErrorModel;
import com.smartydroid.android.starter.kit.model.entity.Entitiy;
import com.smartydroid.android.starter.kit.network.callback.PaginatorCallback;
import com.smartydroid.android.starter.kit.utilities.RecyclerViewHandler;
import com.smartydroid.android.starter.kit.utilities.ViewHandler;
import com.smartydroid.android.starter.kit.utilities.ViewUtils;
import com.smartydroid.android.starter.kit.widget.LoadMoreView;
import com.smartydroid.android.starter.kit.widget.LoadingLayout;
import java.util.ArrayList;

public abstract class RecyclerViewFragment<E extends Entitiy> extends StarterFragment
    implements LoadingLayout.OnButtonClickListener,
    SwipeRefreshLayout.OnRefreshListener, ViewHandler.OnScrollBottomListener, View.OnClickListener,
    PaginatorCallback<E> {

  private LoadingLayout mLoadingLayout;
  private LoadMoreView mLoadMoreView;

  private SwipeRefreshLayout mSwipeRefreshLayout;
  private RecyclerView mRecyclerView;

  private EasyRecyclerAdapter mRecyclerAdapter;

  private Paginator<E> mPagePaginator;
  private RecyclerViewHandler mRecyclerViewHandler;

  public abstract void bindViewHolders(EasyRecyclerAdapter adapter);
  public abstract Paginator<E> buildPaginator();

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPagePaginator = buildPaginator();
    mLoadMoreView = new LoadMoreView();
    mRecyclerViewHandler = new RecyclerViewHandler();

    mRecyclerAdapter = new EasyRecyclerAdapter(getContext());
    bindViewHolders(mRecyclerAdapter);
  }

  @Override protected int getFragmentLayout() {
    return R.layout.content_recycler_view;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mLoadingLayout = ViewUtils.getView(view, R.id.container_loading_layout);
    mSwipeRefreshLayout = mLoadingLayout.getContentView();
    mRecyclerView = ViewUtils.getView(mSwipeRefreshLayout, android.R.id.list);
    mSwipeRefreshLayout.setOnRefreshListener(this);

    setupEmptyView();
    setupRecyclerView();
  }

  @Override public void onResume() {
    super.onResume();
    updateView();

    if (!mPagePaginator.dataHasLoaded()) {
      mPagePaginator.refresh();
    }
  }

  @Override public void onPause() {
    super.onPause();
    if (mPagePaginator.isLoading()) {
      mPagePaginator.cancel();
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    mLoadingLayout = null;
    mSwipeRefreshLayout = null;
    mRecyclerView = null;
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mPagePaginator = null;
    mLoadMoreView = null;
    mRecyclerViewHandler = null;

    mRecyclerAdapter = null;
  }

  /**
   * setup
   */
  private void setupRecyclerView() {
    buildRecyclerView();
    mRecyclerViewHandler.handleSetAdapter(mRecyclerView, mRecyclerAdapter, mLoadMoreView, this);
    mRecyclerViewHandler.setOnScrollBottomListener(mRecyclerView, this);
  }

  public void buildRecyclerView() {
    // set layout manager
    mRecyclerView.setLayoutManager(buildLayoutManager());
    // add item decoration
    mRecyclerView.addItemDecoration(buildItemDecoration());
  }

  public RecyclerView.LayoutManager buildLayoutManager() {
    return new LinearLayoutManager(getContext());
  }

  public RecyclerView.ItemDecoration buildItemDecoration() {
    DividerItemDecoration decoration = new DividerItemDecoration(getContext());
    decoration.setInsets(buildInsets());
    final int dividerRes = buildDivider();
    if (dividerRes > 0) {
      decoration.setDivider(dividerRes);
    }
    return decoration;
  }

  public @DrawableRes int buildDivider() {
    return 0;
  }

  public @DimenRes int buildInsets() {
    return R.dimen.starter_divider_insets;
  }

  private void updateView() {
    if (! isEmpty()) {
      mLoadingLayout.showContentView();
    } else if (! mPagePaginator.dataHasLoaded()) {
      mLoadingLayout.showLoadingView();
    } else if (mPagePaginator.hasError()) {
      mLoadingLayout.showErrorView();
    } else {
      mLoadingLayout.showEmptyView();
    }
  }

  public boolean isEmpty() {
    return mPagePaginator.isEmpty();
  }

  @Override public void onRefresh() {
    if (!mPagePaginator.isLoading()) {
      mPagePaginator.refresh();
    } else {
      mSwipeRefreshLayout.setRefreshing(false);
    }
  }

  public void setupEmptyView() {
    mLoadingLayout.setOnButtonClickListener(this);
  }

  /**
   * load more click
   */
  @Override public void onClick(View v) {
    mPagePaginator.loadMore();
  }

  @Override public void onEmptyButtonClick(View view) {
    mPagePaginator.refresh();
  }

  @Override public void onErrorButtonClick(View view) {
    mPagePaginator.refresh();
  }

  @Override public void onScorllBootom() {
    if (mPagePaginator.canLoadMore()) {
      mPagePaginator.loadMore();
    }
  }


  ///////////////////////////////////////////////////////////
  ///////////////////////////////////////////////////////////
  @Override public void startRequest() {
    if (isEmpty()) {
      mLoadingLayout.showLoadingView();
      mLoadMoreView.hideLoading();
      return;
    }

    if (! mPagePaginator.isRefresh()) {
      mLoadMoreView.showLoading();
    }
  }

  @Override public void endRequest() {
    if (mPagePaginator.isRefresh()) {
      mSwipeRefreshLayout.setRefreshing(false);
    }

    if (! mPagePaginator.hasMorePages()) {
      mLoadMoreView.showNoMore();
    }

    updateView();
  }

  @Override public void respondSuccess(ArrayList<E> data) {
    mRecyclerAdapter.addAll(mPagePaginator.items());
  }

  @Override public void respondWithError(Throwable error) {
    mLoadMoreView.showFailure(error);
  }

  @Override public void errorNotFound(ErrorModel errorModel) {

  }

  @Override public void errorUnprocessable(ErrorModel errorModel) {

  }

  @Override public void errorUnauthorized(ErrorModel errorModel) {

  }

  @Override public void errorForbidden(ErrorModel errorModel) {

  }

  @Override public void eNetUnreach(Throwable t) {

  }

  @Override public void errorSocketTimeout(Throwable t) {

  }

  @Override public void error(ErrorModel errorModel) {

  }
}

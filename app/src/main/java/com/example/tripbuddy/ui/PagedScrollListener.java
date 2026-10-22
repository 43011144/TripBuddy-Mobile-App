package com.example.tripbuddy.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/** Calls onLoadMore when the user scrolls near the end of a list, so long histories load page by page. */
public abstract class PagedScrollListener extends RecyclerView.OnScrollListener {

    private static final int PREFETCH = 5;

    protected abstract void onLoadMore();

    @Override
    public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
        if (dy <= 0) return;
        LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
        if (lm == null) return;
        if (lm.findLastVisibleItemPosition() >= lm.getItemCount() - PREFETCH) onLoadMore();
    }
}

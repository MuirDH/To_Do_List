package com.example.android.todolist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;


/**
 * EmptyRecyclerView Created by Muir on 01/12/2017.
 * Code adapted from http://akbaribrahim.com/empty-view-for-androids-recyclerview/
 */

public class EmptyRecyclerView extends RecyclerView {

    private View emptyView;

    /**
     * calls checkIfEmpty() every time it observes an event that changes the content of the adapter.
     */
    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public EmptyRecyclerView(Context context, AttributeSet attributes, int defStyle) {
        super(context, attributes, defStyle);
    }

    /**
     * checks if both the empty view and adapter are not null. If the item count provided by the
     * adapter is equal to zero, the empty view is shown and the EmptyRecyclerView is hidden. If the
     * item count is not zero, then the empty view is hidden and EmptyRecyclerView is shown.
     */
    private void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            final boolean emptyViewVisible;
            emptyViewVisible = getAdapter().getItemCount() == 0;
            if (emptyViewVisible) {
                emptyView.setVisibility(VISIBLE);
                setVisibility(GONE);
            } else {
                emptyView.setVisibility(GONE);
                setVisibility(VISIBLE);
            }
        }

    }

    /**
     * overrides the setAdapter() method of its superclass and registers an
     * AdapterObserver whenever an adapter is set. It also unregisters the observer whenever the
     * adapter is changed or unset.
     */
    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) adapter.registerAdapterDataObserver(observer);
        super.setAdapter(adapter);
        if (adapter != null) adapter.registerAdapterDataObserver(observer);
        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}

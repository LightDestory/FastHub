package com.fastaccess.ui.adapter;

import androidx.annotation.NonNull;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import com.fastaccess.data.dao.SearchCodeModel;
import com.fastaccess.ui.adapter.viewholder.SearchCodeViewHolder;
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter;
import com.fastaccess.ui.base.adapter.BaseViewHolder;

import java.util.List;

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */

public class SearchCodeAdapter extends BaseRecyclerAdapter<SearchCodeModel, SearchCodeViewHolder, BaseViewHolder
        .OnItemClickListener<SearchCodeModel>> {

    private boolean showRepoName;

    public SearchCodeAdapter(@NonNull List<SearchCodeModel> data) {
        super(data);
    }

    @Override protected SearchCodeViewHolder viewHolder(ViewGroup parent, int viewType) {
        return SearchCodeViewHolder.newInstance(parent, this);
    }

    @Override protected void onBindView(SearchCodeViewHolder holder, int position) {
        holder.bind(getItem(position), showRepoName);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void showRepoName(boolean showRepoName) {
        this.showRepoName = showRepoName;
        notifyDataSetChanged();
    }
}

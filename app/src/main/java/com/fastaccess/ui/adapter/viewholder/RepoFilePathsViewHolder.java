package com.fastaccess.ui.adapter.viewholder;

import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.fastaccess.R;
import com.fastaccess.data.dao.model.RepoFile;
import com.fastaccess.ui.widgets.FontTextView;
import com.fastaccess.ui.widgets.recyclerview.BaseRecyclerAdapter;
import com.fastaccess.ui.widgets.recyclerview.BaseViewHolder;

import butterknife.BindView;

/**
 * Created by Kosh on 18 Feb 2017, 2:53 AM
 */

public class RepoFilePathsViewHolder extends BaseViewHolder<RepoFile> {

    FontTextView pathName;

    private RepoFilePathsViewHolder(@NonNull View itemView, @NonNull BaseRecyclerAdapter<?,?,?> baseAdapter) {
        super(itemView, baseAdapter);
        this.pathName = itemView.findViewById(R.id.pathName);
    }

    public static RepoFilePathsViewHolder newInstance(ViewGroup viewGroup, BaseRecyclerAdapter<?,?,?> adapter) {
        return new RepoFilePathsViewHolder(getView(viewGroup, R.layout.file_path_row_item), adapter);
    }

    @Override public void bind(@NonNull RepoFile filesModel) {
        pathName.setText(filesModel.getName());
    }
}

package com.fastaccess.ui.modules.pinned.issue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fastaccess.data.dao.model.Issue;
import com.fastaccess.ui.base.mvp.BaseMvp;
import com.fastaccess.ui.base.adapter.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kosh on 25 Mar 2017, 7:57 PM
 */

public interface PinnedIssueMvp {

    interface View extends BaseMvp.FAView {
        void onNotifyAdapter(@Nullable List<Issue> items);

        void onDeletePinnedIssue(long id, int position);
    }

    interface Presenter extends BaseViewHolder.OnItemClickListener<Issue> {
        @NonNull ArrayList<Issue> getPinnedIssue();

        void onReload();
    }
}

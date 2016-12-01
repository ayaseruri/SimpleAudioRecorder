package com.x.simpleaudiorecorder.ui.main;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.x.simpleaudiorecorder.R;
import com.x.simpleaudiorecorder.bean.RecordFileInfo;
import com.x.simpleaudiorecorder.ui.player.PlayerDialog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EView;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wufeiyang on 2016/12/1.
 */
@EView
public class RecordResultView extends RecyclerView {

    public RecordResultView(Context context) {
        super(context);
    }

    public RecordResultView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @AfterViews
    void init(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        setLayoutManager(layoutManager);

        RecordResultAdapter adapter = new RecordResultAdapter();
        setAdapter(adapter);

        setItemAnimator(new DefaultItemAnimator());
    }

    @UiThread
    public void add(RecordFileInfo info){
        RecordResultAdapter adapter = (RecordResultAdapter) getAdapter();
        adapter.add(info);
    }

    @UiThread
    public void add(List<RecordFileInfo> infos){
        RecordResultAdapter adapter = (RecordResultAdapter) getAdapter();
        adapter.add(infos);
    }

    @UiThread
    public void refresh(List<RecordFileInfo> infos){
        RecordResultAdapter adapter = (RecordResultAdapter) getAdapter();
        adapter.refresh(infos);
    }

    private static class RecordResultAdapter extends RecyclerView.Adapter<RecordResultAdapter.ViewHolder>{

        private List<RecordFileInfo> mFileInfos;

        public RecordResultAdapter() {
            mFileInfos = new ArrayList<>();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new ViewHolder(inflater.inflate(R.layout.item_record_result, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final RecordFileInfo info = mFileInfos.get(position);
            holder.mFileNameTV.setText(info.getFileName());
            holder.mPlayBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    new PlayerDialog(view.getContext(), info.getFileName()
                            , info.getFilePath()).show();
                }
            });
        }

        public void add(RecordFileInfo info){
            mFileInfos.add(info);
            notifyItemInserted(mFileInfos.size());
        }

        public void add(List<RecordFileInfo> infos){
            mFileInfos.addAll(infos);
            notifyDataSetChanged();
        }

        public void refresh(List<RecordFileInfo> infos){
            mFileInfos.clear();
            add(infos);
        }

        @Override
        public int getItemCount() {
            return mFileInfos.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder{
            public TextView mFileNameTV;
            public ImageButton mPlayBtn;

            public ViewHolder(View itemView) {
                super(itemView);
                mFileNameTV = (TextView) itemView.findViewById(R.id.file_name);
                mPlayBtn = (ImageButton) itemView.findViewById(R.id.play_btn);
            }
        }
    }
}

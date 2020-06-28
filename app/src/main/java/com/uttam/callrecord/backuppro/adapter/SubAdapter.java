package com.uttam.callrecord.backuppro.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.HomeActivity;
import com.uttam.callrecord.backuppro.database.Database;
import com.uttam.callrecord.backuppro.model.CallListModelClass;
import com.uttam.callrecord.backuppro.R;

public class SubAdapter extends RecyclerView.Adapter<SubAdapter.MyViewHolder> {

    private ArrayList<CallListModelClass> arrayList;
    private Context context;


    public SubAdapter(Context context, ArrayList<CallListModelClass> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public SubAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = (View) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_activity_record_list_model, viewGroup, false);

        SubAdapter.MyViewHolder vh = new SubAdapter.MyViewHolder(context, arrayList, v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (arrayList.get(position).getCallIndicator().equalsIgnoreCase("incoming")) {
            holder.relativeLayout.setBackgroundResource(R.drawable.home_activity_record_list_incoming_call_background);
            holder.callIndicator.setImageResource(R.drawable.ic_call_incoming);
        } else {
            holder.relativeLayout.setBackgroundResource(R.drawable.home_activity_record_list_outgoing_call_background);
            holder.callIndicator.setImageResource(R.drawable.ic_call_outgoing);
        }
        holder.avatar.setImageResource(R.drawable.ic_account_circle);
        int duration = Integer.parseInt(arrayList.get(position).getDuration());
        holder.duration.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
        holder.name.setText(arrayList.get(position).getName());
        holder.time.setText(arrayList.get(position).getTime());
//        holder.playButton.setImageResource(R.drawable.ic_play_circle);
        holder.shareButton.setImageResource(R.drawable.ic_share);
        holder.deleteButton.setImageResource(R.drawable.ic_delete_black);

        File folderName=new File(Environment.getExternalStorageDirectory(), Constants.downloadFolderName);
        if (!folderName.exists()) {
            folderName.mkdirs();
        }
        String filePath = folderName.getAbsolutePath() +"/"+arrayList.get(position).getFile();
        File file=new File(filePath);
        if (file.exists() && file.isFile()){
            holder.playButton.setImageResource(R.drawable.ic_play_circle);
        }else {
            holder.playButton.setImageResource(R.drawable.ic_download_icon);
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Context context;
        private HomeActivity homeActivity;
        private Database database;
        private ArrayList<CallListModelClass> arrayList;
        private RelativeLayout relativeLayout;
        private ImageView avatar;
        private ImageView callIndicator;
        private TextView duration;
        private TextView name;
        private TextView time;
        private ImageView playButton;
        private ImageView shareButton;
        private ImageView deleteButton;

        public MyViewHolder(Context context, ArrayList<CallListModelClass> arrayList, @NonNull View v) {
            super(v);

            this.context = context;
            this.arrayList = arrayList;
            this.homeActivity= (HomeActivity) context;
            relativeLayout = v.findViewById(R.id.homeActivityModelViewRelativeLayoutId);
            avatar = v.findViewById(R.id.homeActivityRecordListAccountCircleImageViewId);
            callIndicator = v.findViewById(R.id.homeActivityRecordListModelCallIndicatorImageViewId);
            duration = v.findViewById(R.id.homeActivityRecordListDurationTextViewId);
            name = v.findViewById(R.id.homeActivityRecordListModelNameTextViewId);
            time = v.findViewById(R.id.homeActivityRecordListTimeTextViewId);
            playButton = v.findViewById(R.id.homeActivityRecordListModelPlayIconImageViewId);
            shareButton = v.findViewById(R.id.homeActivityRecordListModelShareImageViewId);
            deleteButton = v.findViewById(R.id.homeActivityRecordListModelDeleteImageViewId);

            playButton.setOnClickListener(this);
            shareButton.setOnClickListener(this);
            deleteButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.homeActivityRecordListModelShareImageViewId:
                    homeActivity.recordShareButtonClicked(arrayList.get(getAdapterPosition()).getFileId(),arrayList.get(getAdapterPosition()).getFile());
                    break;

                case R.id.homeActivityRecordListModelPlayIconImageViewId:
                    homeActivity.recordPlayButtonClicked(arrayList.get(getAdapterPosition()).getFileId(),arrayList.get(getAdapterPosition()).getFile());
                    break;

                case R.id.homeActivityRecordListModelDeleteImageViewId:
                    homeActivity.alertDialogForDeletingFileFromDrive(arrayList.get(getAdapterPosition()).getFileId(),arrayList.get(getAdapterPosition()).getFile());
                    break;
            }
        }
    }
}

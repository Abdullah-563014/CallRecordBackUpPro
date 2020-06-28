package com.uttam.callrecord.backuppro.adapter;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.uttam.callrecord.backuppro.model.MainModelClass;
import com.uttam.callrecord.backuppro.R;

public class AllCallRecyclerViewAdapter extends RecyclerView.Adapter<AllCallRecyclerViewAdapter.MyViewHolder> {


    private ArrayList<MainModelClass> arrayList;
    private Context context;
    private String currentDate;
    private String currentMonth;
    private String title;

    public AllCallRecyclerViewAdapter(Context context, ArrayList<MainModelClass> arrayList, String currentDate, String currentMonth) {
        this.arrayList = arrayList;
        this.context = context;
        this.currentDate=currentDate;
        this.currentMonth=currentMonth;
    }


    @Override
    public AllCallRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_model_view, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        holder.title.setText(titleModifier(arrayList.get(position).getDate(),arrayList.get(position).getMonth(),arrayList.get(position).getYear()));
        holder.recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        holder.recyclerView.setLayoutManager(layoutManager);
        SubAdapter mAdapter;
        mAdapter = new SubAdapter(context,arrayList.get(position).getArrayList());
        holder.recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {


        TextView title;
        RecyclerView recyclerView;

        public MyViewHolder(View v) {
            super(v);

            title = v.findViewById(R.id.mainModelTitleTextViewId);
            recyclerView = v.findViewById(R.id.mainModelRecyclerViewId);
        }
    }


    public String titleModifier(String userDate, String userMonth, String userYear) {
        if (currentDate!=null && currentMonth!=null){
            if (userDate.equalsIgnoreCase(currentDate) && userMonth.equalsIgnoreCase(currentMonth)) {
                title = "Today";
            } else if (userDate.equalsIgnoreCase(String.valueOf(Integer.parseInt(currentDate) - 1)) && userMonth.equalsIgnoreCase(currentMonth)) {
                title = "Yesterday";
            } else {
                title = userDate + "/" + userMonth + "/" + userYear;
            }
        }

        return title;
    }

}
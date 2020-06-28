package com.uttam.callrecord.backuppro.fragment;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.services.drive.model.FileList;
import com.uttam.callrecord.backuppro.Constants;
import com.uttam.callrecord.backuppro.HomeActivity;
import com.uttam.callrecord.backuppro.R;
import com.uttam.callrecord.backuppro.adapter.AllCallRecyclerViewAdapter;
import com.uttam.callrecord.backuppro.database.Database;
import com.uttam.callrecord.backuppro.model.CallListModelClass;
import com.uttam.callrecord.backuppro.model.DatabaseModelClass;
import com.uttam.callrecord.backuppro.model.MainModelClass;

public class IncomingCallFragment extends Fragment {

    private View view;
    private HomeActivity homeActivity;
    public RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AllCallRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<MainModelClass> arrayList=new ArrayList<>();
    private ArrayList<CallListModelClass> callArrayList;
    private ArrayList<DatabaseModelClass> allData = new ArrayList<>();
    private String userDate;
    private String userMonth;
    private String userYear;
    private ArrayList<String> temporaryIdList;
    private int allDataSize;

    public IncomingCallFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.incoming_call_fragment, container, false);
        recyclerView = view.findViewById(R.id.incomingCallRecyclerViewId);
        progressBar = view.findViewById(R.id.incomingCallFragmentSpinKitId);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity()!=null){
            homeActivity= (HomeActivity) getActivity();

            initAll();

            queryFiles();
        }
    }

    private void initAll() {
        Date date = new Date();
        String userDate = (String) DateFormat.format("dd", date.getTime());
        String userMonth = (String) DateFormat.format("MM", date.getTime());
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVerticalScrollBarEnabled(true);
        mAdapter = new AllCallRecyclerViewAdapter(getContext(), arrayList, userDate, userMonth);
        recyclerView.setAdapter(mAdapter);
        recyclerView.smoothScrollToPosition(0);
        recyclerView.setVerticalScrollbarPosition(0);

        progressBar.setVisibility(View.GONE);
    }

    public void categorizedUserAllData() {

        temporaryIdList = new ArrayList<>();

        helperOFCategorizedMethod();

        while (allData.size() != 0) {
            temporaryIdList.clear();
            callArrayList = new ArrayList<>();
            for (int i = 0; i < allData.size(); i++) {
                if (allData.get(i).getYear().equalsIgnoreCase(userYear) && allData.get(i).getMonth().equalsIgnoreCase(userMonth) && allData.get(i).getDate().equalsIgnoreCase(userDate)) {
                    CallListModelClass callListModelClass = new CallListModelClass(allData.get(i).getId(),allData.get(i).getCallIndicator(), allData.get(i).getDuration(), allData.get(i).getName(), allData.get(i).getTime(), allData.get(i).getFile());
                    callArrayList.add(callListModelClass);
                    temporaryIdList.add(allData.get(i).getId());
                }
                if (allData.size() == (i + 1)) {
                    arrayList.add(new MainModelClass(userDate, userMonth, userYear, callArrayList));
                    for (int j = 0; j < temporaryIdList.size(); j++) {
                        int k;
                        for (k = 0; k < allData.size(); k++) {
                            if (temporaryIdList.get(j).equalsIgnoreCase(allData.get(k).getId())) {
                                allData.remove(k);
                                k = 0;
                            }
                        }
                    }
                    if (!allData.isEmpty()) {
                        helperOFCategorizedMethod();
                    }
                    temporaryIdList.clear();
                }
            }
            allDataSize = allDataSize - 1;
        }
        mAdapter.notifyDataSetChanged();
    }

    public void helperOFCategorizedMethod() {
        if (!allData.isEmpty()) {
            userYear = allData.get(0).getYear();
            userMonth = allData.get(0).getMonth();
            userDate = allData.get(0).getDate();
            allDataSize = allData.size();
        } else {
            allDataSize = 0;
        }
    }

    private void queryFiles() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        homeActivity.driveServiceHelper.queryFiles()
                .addOnSuccessListener(new OnSuccessListener<FileList>() {
                    @Override
                    public void onSuccess(FileList fileList) {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        allData.clear();
                        arrayList.clear();
                        for (com.google.api.services.drive.model.File file : fileList.getFiles()){
                            String fileId=file.getId();
                            String fileNameWithExtension=file.getName();
                            int extensionPosition=fileNameWithExtension.lastIndexOf(".");
                            String fileNameWithoutExtension;
                            if (extensionPosition<=0){
                                fileNameWithoutExtension=fileNameWithExtension;
                            }else {
                                fileNameWithoutExtension=fileNameWithExtension.substring(0,extensionPosition);
                            }
                            if (fileNameWithoutExtension.contains(",")){
                                String[] fileInfo=fileNameWithoutExtension.split(",");
                                if (fileInfo.length==5){
                                    String callIndicator=fileInfo[0];
                                    String dateMonthYear=fileInfo[1];
                                    String[] dateMonthYearInfo=dateMonthYear.split("-");
                                    String date=dateMonthYearInfo[0];
                                    String month=dateMonthYearInfo[1];
                                    String year=dateMonthYearInfo[2];
                                    String fileName=fileInfo[2];
                                    String time=fileInfo[3];
                                    String duration=fileInfo[4];
                                    String uploadStatus="true";
                                    if (callIndicator.equalsIgnoreCase("incoming")){
                                        DatabaseModelClass databaseModelClass=new DatabaseModelClass(fileId,date,month,year,callIndicator,duration,fileName,time,fileNameWithExtension,uploadStatus);
                                        allData.add(databaseModelClass);
                                    }
                                }
                            }
                        }
                        categorizedUserAllData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        Log.d(Constants.TAG,"result is error for "+e.getMessage());
                    }
                });
    }
}

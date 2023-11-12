package com.example.projectmanagement_calcapp.TaskDetailsHandler;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.projectmanagement_calcapp.R;

import java.util.Arrays;
import java.util.List;

public class AssigneeHandler{

    String[] assigneeArray;

    public void populateAssigneeList(Activity activity, ListView assigneeListView){
        assigneeArray = activity.getResources().getStringArray(R.array.assignees);
        Arrays.stream(assigneeArray).forEach(System.out::println);
        List<String> assigneeList = Arrays.asList(assigneeArray);

        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(activity.getBaseContext(),R.layout.assignee_layout, R.id.assignee_list_content, assigneeList);

        assigneeListView.setAdapter(assigneeAdapter);
    }
}

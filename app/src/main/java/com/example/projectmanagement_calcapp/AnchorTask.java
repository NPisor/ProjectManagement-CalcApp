package com.example.projectmanagement_calcapp;

import android.graphics.Bitmap;

import java.sql.Timestamp;
public class AnchorTask {


    Long id;
    String message;
    Long assignerId;
    Long assigneeId;
    String taskDescription;
    Long priority;
    Timestamp createdTs;
    Timestamp submittedForReviewTs;
    Timestamp completedTs;
    Long status;
    Long clientId;
    Bitmap taskImage;

    public AnchorTask(Long id, String message, Long assignerId, Long assigneeId, String taskDescription, Long priority, Timestamp createdTs, Timestamp submittedForReviewTs, Timestamp completedTs, Long status, Long clientId, Bitmap taskImage) {
        this.id = id;
        this.message = message;
        this.assignerId = assignerId;
        this.assigneeId = assigneeId;
        this.taskDescription = taskDescription;
        this.priority = priority;
        this.createdTs = createdTs;
        this.submittedForReviewTs = submittedForReviewTs;
        this.completedTs = completedTs;
        this.status = status;
        this.clientId = clientId;
        this.taskImage = taskImage;
    }


    public AnchorTask() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getAssignerId() {
        return assignerId;
    }

    public void setAssignerId(Long assignerId) {
        this.assignerId = assignerId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public Timestamp getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Timestamp createdTs) {
        this.createdTs = createdTs;
    }

    public Timestamp getSubmittedForReviewTs() {
        return submittedForReviewTs;
    }

    public void setSubmittedForReviewTs(Timestamp submittedForReviewTs) {
        this.submittedForReviewTs = submittedForReviewTs;
    }

    public Timestamp getCompletedTs() {
        return completedTs;
    }

    public void setCompletedTs(Timestamp completedTs) {
        this.completedTs = completedTs;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Bitmap getTaskImage() {
        return taskImage;
    }

    public void setTaskImage(Bitmap taskImage) {
        this.taskImage = taskImage;
    }
}

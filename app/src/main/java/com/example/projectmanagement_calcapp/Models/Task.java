package com.example.projectmanagement_calcapp.Models;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class Task {

    private Long id;

    private Float longitude;

    private Float latitude;

    private Float altitude;

    private String description;

    private String imageUuid;

    private Long assignee;

    private Long priority;

    private Long status;

    Timestamp createdTs;

    Long assignerId;

    String title;

    public Task(Long id, Float longitude, Float latitude, Float altitude, String description, String imageUuid, Long assignee, Long priority, Long status, Timestamp createdTs, Long assignerId, String title) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.description = description;
        this.imageUuid = imageUuid;
        this.assignee = assignee;
        this.priority = priority;
        this.status = status;
        this.createdTs = createdTs;
        this.assignerId = assignerId;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getAltitude() {
        return altitude;
    }

    public void setAltitude(Float altitude) {
        this.altitude = altitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUuid() {
        return imageUuid;
    }

    public void setImageUuid(String imageUuid) {
        this.imageUuid = imageUuid;
    }

    public Long getAssignee() {
        return assignee;
    }

    public void setAssignee(Long assignee) {
        this.assignee = assignee;
    }

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Timestamp getCreatedTs() {
        return createdTs;
    }

    public void setCreatedTs(Timestamp createdTs) {
        this.createdTs = createdTs;
    }

    public Long getAssignerId() {
        return assignerId;
    }

    public void setAssignerId(Long assignerId) {
        this.assignerId = assignerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

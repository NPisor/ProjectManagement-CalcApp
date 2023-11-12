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
}

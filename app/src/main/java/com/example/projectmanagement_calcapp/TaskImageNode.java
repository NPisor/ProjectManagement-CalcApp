package com.example.projectmanagement_calcapp;

import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

import java.io.Serializable;

public class TaskImageNode extends TransformableNode implements Serializable {

    int imageId;
    public TaskImageNode(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }
}

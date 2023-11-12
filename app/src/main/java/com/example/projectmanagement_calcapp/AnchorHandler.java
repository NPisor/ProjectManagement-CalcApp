package com.example.projectmanagement_calcapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.GeospatialPose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class AnchorHandler extends Activity implements Serializable {
    ImageView savedTaskImage;



    public Anchor placeAnchor(Anchor anchor, ArFragment fragment, String imageUuid, Renderable renderable, ViewRenderable viewRenderable, Bitmap bitmap) throws ExecutionException, InterruptedException {

        System.out.println(imageUuid);
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(fragment.getArSceneView().getScene());

        // Create the transformable model and add it to the anchor.
        TransformableNode model = animateModel(fragment, anchorNode, renderable);
        model.setName(imageUuid);
        Node titleNode = new Node();
        titleNode.setParent(model);
        titleNode.setEnabled(false);
        titleNode.setLocalPosition(new Vector3(0.0f, 0.5f, 0.0f));
        titleNode.setRenderable(viewRenderable);
        titleNode.setEnabled(true);
        savedTaskImage = viewRenderable.getView().findViewById(R.id.TaskImage);

        fragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            Vector3 cameraPosition = new Vector3(fragment.getArSceneView().getScene().getCamera().getWorldPosition().x, 0, fragment.getArSceneView().getScene().getCamera().getWorldPosition().z);
            Vector3 cardPosition = new Vector3(titleNode.getWorldPosition().x, 0, titleNode.getWorldPosition().z);
            Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
            Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
            titleNode.setWorldRotation(lookRotation);
            viewRenderable.getView().setAlpha(1 - calculateDistance(fragment.getArSceneView().getScene().getCamera(), model));
        });
        savedTaskImage.setImageBitmap(bitmap);
        addModelListeners(fragment, model, imageUuid);
//        assigneeHandler = new AssigneeHandler();
//        ListView assigneeListView = (ListView) viewRenderable.getView().findViewById(R.id.assigneeList);
//        assigneeHandler.populateAssigneeList(activity, assigneeListView);

        return anchor;
    }

    public TransformableNode animateModel(ArFragment fragment, AnchorNode anchorNode, Renderable renderable) {
        TransformableNode model = new TransformableNode(fragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setLocalScale(new Vector3(0.3f, 0.3f, 0.3f));
        model.setRenderable(renderable).animate(true).start();
        model.select();

        return model;
    }

    public void addModelListeners(ArFragment fragment, TransformableNode model, String imageUuid) {
        model.setOnTapListener((hitTestResult, motionEvent) -> {
            if (fragment.getArSceneView().isTrackingPlane()) {
                Toast.makeText(fragment.getContext(), imageUuid, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public float calculateDistance(Camera camera, TransformableNode model) {
        final int MAX_DISTANCE = 30;
        final int MIN_DISTANCE = 20;
        float convertedDistance = 0;

        convertedDistance = (float) Math.sqrt(Math.pow(camera.getWorldPosition().x - model.getWorldPosition().x, 2) +
                Math.pow(camera.getWorldPosition().y - model.getWorldPosition().y, 2) +
                Math.pow(camera.getWorldPosition().z - model.getWorldPosition().z, 2) * 100);

        convertedDistance = (convertedDistance - MIN_DISTANCE) / (MAX_DISTANCE - MIN_DISTANCE);

        return convertedDistance;
    }

}

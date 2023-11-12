package com.example.projectmanagement_calcapp;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentOnAttachListener;
import androidx.lifecycle.MutableLiveData;

import com.example.projectmanagement_calcapp.Models.CreateAnchorRequest;
import com.example.projectmanagement_calcapp.Models.Task;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Earth;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.ResolveAnchorOnTerrainFuture;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.SceneView;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gorisse.thomas.sceneform.light.LightEstimationConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import lombok.NonNull;
import lombok.Synchronized;

public class ArCoreActivity extends AppCompatActivity implements FragmentOnAttachListener,
        BaseArFragment.OnTapArPlaneListener,
        BaseArFragment.OnSessionConfigurationListener,
        ArFragment.OnViewCreatedListener{

    public ArFragment arFragment;
    public Renderable model;
    MutableLiveData<Bitmap> bitmap;

    Object earthLock = new Object();
    String taskName;

    Long taskPriority;

    Earth earth;

    Long assigneeId;
    AnchorHandler anchorHandler;
    String imageUuid;
    WeakReference<ArCoreActivity> weakActivity;

    private static final String FETCH_USER_TASKS_URL = "http://10.0.0.130:8080/task/assigneeIdJson";

    private static final String POST_NEW_TASK_URL = "http://10.0.0.130:8080/task/new";

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().addFragmentOnAttachListener(this);
        weakActivity = new WeakReference<>(this);
        if (savedInstanceState == null) {
            if (Sceneform.isSupported(this)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.arFragment, ArFragment.class, null)
                        .commit();
            }
        }
        initListeners();
    }

    private void initListeners() {
        Button clearBtn = findViewById(R.id.clearBtn);
        Button resolveBtn = findViewById(R.id.resolveBtn);       //------------------Init models------------------

        ModelRenderable.builder()
                .setSource(this, R.raw.anchormarker)
                .setIsFilamentGltf(true)
                .setAsyncLoadEnabled(true)
                .build()
                .thenAccept(model -> {
                    ArCoreActivity arCoreActivity = weakActivity.get();
                    if (arCoreActivity != null) {
                        arCoreActivity.model = model;
                    }
                })
                .exceptionally(throwable -> {
                    Toast.makeText(
                            this, "Unable to load model", Toast.LENGTH_LONG).show();
                    return null;
                });
    }

    @Override
    public void onAttachFragment(@NonNull FragmentManager fragmentManager, @NonNull Fragment fragment) {
        if (fragment.getId() == R.id.arFragment) {
            arFragment = (ArFragment) fragment;
            arFragment.setOnSessionConfigurationListener(this);
            arFragment.setOnViewCreatedListener(this);
            arFragment.setOnTapArPlaneListener(this);
        }
    }


    @Override
    public void onViewCreated(ArSceneView arSceneView) {
        arFragment.setOnViewCreatedListener(null);
        arSceneView.setFrameRateFactor(SceneView.FrameRate.FULL);
        arSceneView._lightEstimationConfig = LightEstimationConfig.DISABLED;
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        config.setGeospatialMode(Config.GeospatialMode.ENABLED);
        config.setInstantPlacementMode(Config.InstantPlacementMode.DISABLED);
        config.setAugmentedFaceMode(Config.AugmentedFaceMode.DISABLED);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.setDepthMode(Config.DepthMode.AUTOMATIC);
        }
        session.configure(config);
        earth = session.getEarth();
        new LoadAnchors().execute("1");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageUuid = data.getStringExtra("uuid");
        taskName = data.getStringExtra("taskName");
        taskPriority = data.getLongExtra("taskPriority",0);
        assigneeId = data.getLongExtra("assigneeId", 0);

        CreateAnchorRequest currentAnchorRequest = new CreateAnchorRequest();
        currentAnchorRequest.setLatitude((float) data.getDoubleExtra("taskLatitude", 0));
        currentAnchorRequest.setLongitude((float) data.getDoubleExtra("taskLongitude", 0));
        currentAnchorRequest.setDescription(taskName);
        currentAnchorRequest.setImageUuid(imageUuid);
        currentAnchorRequest.setAssignee(assigneeId);
        currentAnchorRequest.setPriority(taskPriority);
        bitmap.postValue(BitmapFactory.decodeFile(data.getStringExtra("imagePath")));

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("assigneeId", assigneeId).put("assignerId", 2L).put("clientId", 1L).put("priority", 3L)
            .put("status", 1L).put("description", "This is a test description.").put("imageUuid", imageUuid).put("taskTitle", taskName).put("latitude", currentAnchorRequest.getLatitude())
                    .put("longitude", (float) data.getDoubleExtra("taskLongitude", 0)).put("latitude", (float) data.getDoubleExtra("taskLatitude", 0));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        String jsonString = jsonObject.toString();
        new PostData().execute(jsonString);

    }

    public void displayTaskEntryScreen(ArCoreActivity activity, double lat, double lon){

        Intent intent = new Intent(activity, TaskInfoActivity.class);
        intent.putExtra("Lat", lat);
        intent.putExtra("Long", lon);
        activity.startActivityForResult(intent, 1);
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        anchorHandler = new AnchorHandler();
        bitmap = new MutableLiveData<>();
        Anchor anchor = hitResult.createAnchor();
        fusedLocationClient.getCurrentLocation(new CurrentLocationRequest.Builder().setDurationMillis(3000).build(), new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        }).addOnSuccessListener(location -> {
                    displayTaskEntryScreen(this, location.getLatitude(), location.getLongitude());
            ViewRenderable.builder()
                            .setView(this, R.layout.view_model_title)
                            .build()
                            .thenAccept(viewRenderable -> {
                                ArCoreActivity activity = weakActivity.get();
                                if (activity != null) {
                                    bitmap.observe(this, bitmap -> {
                                        try {
                                            anchorHandler.placeAnchor(anchor, arFragment, imageUuid, model, viewRenderable, bitmap);
                                        } catch (ExecutionException |
                                                 InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            })
                            .exceptionally(throwable -> {
                                Toast.makeText(this, "Unable to build ViewRenderable", Toast.LENGTH_LONG).show();
                                return null;
                            });
                });

        }
        //Anchor anchor = hitResult.createAnchor();


        //        Anchor anchor = anchorHandler.placeAnchor(this, arFragment, hitResult, model, viewRenderable);
//
//        Future future = arFragment.getArSceneView().getSession().hostCloudAnchorAsync(anchor, 1, (s, cloudAnchorState) -> {
//                if (cloudAnchorState.isError()) {
//                    Toast.makeText(ArCoreActivity.this, "Error hosting anchor: " + cloudAnchorState, Toast.LENGTH_LONG).show();
//                    return;
//                }
//                try {
//                    Toast.makeText(ArCoreActivity.this, "Now hosting anchor...", Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });


    class PostData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {

                // on below line creating a url to post the data.
                URL url = new URL(POST_NEW_TASK_URL);

                // on below line opening the connection.
                HttpURLConnection client = (HttpURLConnection) url.openConnection();

                // on below line setting method as post.
                client.setRequestMethod("POST");

                // on below line setting content type and accept type.
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");

                // on below line setting client.
                client.setDoOutput(true);

                // on below line we are creating an output stream and posting the data.
                try (OutputStream os = client.getOutputStream()) {
                    byte[] input = strings[0].getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // on below line creating and initializing buffer reader.
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), "utf-8"))) {

                    // on below line creating a string builder.
                    StringBuilder response = new StringBuilder();

                    // on below line creating a variable for response line.
                    String responseLine = null;

                    // on below line writing the response
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // on below line displaying a toast message.
                }

            } catch (Exception e) {

                // on below line handling the exception.
                e.printStackTrace();
            }
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    class LoadAnchors extends AsyncTask<String, Void, String> {

        StringBuilder fullResponse = new StringBuilder();

        AnchorHandler anchorHandler = new AnchorHandler();


        @Override
        protected String doInBackground(String... strings) {
            try {
                // on below line creating a url to post the data.
                URL url = null;
                try {
                    url = new URL("http://10.0.0.130:8080/task/assigneeIdJson?assigneeId=1");
                } catch (MalformedURLException ex) {
                    throw new RuntimeException(ex);
                }

                // on below line opening the connection.
                HttpURLConnection client = null;
                try {
                    client = (HttpURLConnection) url.openConnection();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    client.setRequestMethod("GET");
                } catch (ProtocolException ex) {
                    throw new RuntimeException(ex);
                }

                // on below line setting content type and accept type.
                client.setRequestProperty("Content-Type", "application/json");
                client.setRequestProperty("Accept", "application/json");

                // on below line setting client.
                client.setDoOutput(true);

                // on below line we are creating an output stream and posting the data.
                try (OutputStream os = client.getOutputStream()) {
                    byte[] input = new byte[0];
                    try {
                        input = strings[0].getBytes("utf-8");
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        os.write(input, 0, input.length);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                // on below line creating and initializing buffer reader.
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), "utf-8"))) {

                    // on below line creating a variable for response line.
                    String responseLine = null;

                    // on below line writing the response
                    while (true) {
                        try {
                            if ((responseLine = br.readLine()) == null) break;
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        fullResponse.append(responseLine.trim());
                    }

                    // on below line displaying a toast message.
                }



                if (fullResponse != null) {
                    try {
                        List<Task> anchorsToPlace = new ArrayList<>();

                        // Getting JSON Array node
                        JSONArray anchors = new JSONArray(fullResponse.toString());

                        // looping through All Anchors
                        for (int i = 0; i < anchors.length(); i++) {
                            JSONObject node = anchors.getJSONObject(i);
                            String id = node.getString("id");
                            String title = node.getString("title");
                            String description = node.getString("taskDescription");
                            Long assignerId = Long.valueOf(node.getString("assignerId"));
                            Long assigneeId = Long.valueOf(node.getString("assigneeId"));
                            Timestamp createdTs = new Timestamp(Long.valueOf(node.getString("createdTs")));
                            Long status = Long.valueOf(node.getString("status"));
                            Long priority = Long.valueOf(node.getString("priority"));
                            String imageUuid = node.getString("taskImageUrl");
                            Float latitude = Float.valueOf(node.getString("latitude"));
                            Float longitude = Float.valueOf(node.getString("longitude"));

                            Task task = new Task(Long.valueOf(id), longitude, latitude, 0f, description, imageUuid, assigneeId, priority, status, createdTs, assignerId, title);

                            final ResolveAnchorOnTerrainFuture future =
                                    earth.resolveAnchorOnTerrainAsync(latitude, longitude, 0.0f, earth.getCameraGeospatialPose().getEastUpSouthQuaternion()[0], earth.getCameraGeospatialPose().getEastUpSouthQuaternion()[1],
                                            earth.getCameraGeospatialPose().getEastUpSouthQuaternion()[2], earth.getCameraGeospatialPose().getEastUpSouthQuaternion()[3],
                                            (anchor, state) -> {
                                                if (state == Anchor.TerrainAnchorState.SUCCESS) {
                                                    ViewRenderable.builder()
                                                                    .setView(arFragment.getContext(), R.layout.view_task_info)
                                                                    .build().thenAccept(viewRenderable -> {
                                                                        ArCoreActivity activity = weakActivity.get();
                                                                        if (activity != null) {
                                                                            try {
                                                                                anchorHandler.placeAnchor(anchor, arFragment, imageUuid, model, viewRenderable, null);
                                                                            } catch (
                                                                                    ExecutionException |
                                                                                    InterruptedException e) {
                                                                                throw new RuntimeException(e);
                                                                            }
                                                                        }
                                                                    });
                                                    Toast.makeText(ArCoreActivity.this, "Anchor resolved", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(ArCoreActivity.this, "Anchor not resolved", Toast.LENGTH_LONG).show();
                                                }
                                            });
                            anchorsToPlace.add(task);
                        }
                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(e.getMessage());
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                } else {
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                return null;

            } catch (Exception e) {

                // on below line handling the exception.
                e.printStackTrace();
            }
            return fullResponse.toString();
        }
    }


}

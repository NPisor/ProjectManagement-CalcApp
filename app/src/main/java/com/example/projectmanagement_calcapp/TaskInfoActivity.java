package com.example.projectmanagement_calcapp;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.example.projectmanagement_calcapp.Models.Task;
import com.google.android.material.slider.Slider;

import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

public class TaskInfoActivity extends AppCompatActivity implements Serializable {
    PreviewView taskLocationImage;
    Button saveTaskBtn;
    Bitmap bitmap;
    ProcessCameraProvider cameraProvider;

    EditText taskName;

    Slider taskPriority;

    TextView gpsCoords;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_task_info_layout);
        taskLocationImage = findViewById(R.id.taskLocationImage);
        saveTaskBtn = findViewById(R.id.closeTaskWidgetBtn);
        gpsCoords = findViewById(R.id.gpscoordstext);
        taskName = findViewById(R.id.taskName);
        taskPriority = findViewById(R.id.prioritySlider);

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("Lat", 0);
        double longitude = intent.getDoubleExtra("Long", 0);
        gpsCoords.setText(String.format("Lat: %s, Long: %s", latitude, longitude));
        try {
            setListeners(populateCameraThumbnail(this), latitude, longitude);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public PreviewView populateCameraThumbnail(TaskInfoActivity view) throws ExecutionException, InterruptedException {
        PreviewView previewView = findViewById(R.id.taskLocationImage);

        cameraProvider = ProcessCameraProvider.getInstance(this).get();
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);

        return previewView;
    }

    public void setListeners(PreviewView previewView, double latitude, double longitude) {
        saveTaskBtn.setOnClickListener(v -> {
            bitmap = previewView.getBitmap();
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            String uuid = new String(java.util.UUID.randomUUID().toString());
            // Create imageDir
            File mypath=new File(directory, uuid + ".png");

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("imagePath", directory.getAbsolutePath() + "/" + uuid + ".png");
            returnIntent.putExtra("uuid", uuid);
            returnIntent.putExtra("taskName", taskName.getText().toString());
            returnIntent.putExtra("taskPriority", taskPriority.getValue());
            returnIntent.putExtra("taskLatitude", latitude);
            returnIntent.putExtra("taskLongitude", longitude);
            setResult(1, returnIntent);
            finish();
        });
    }
}
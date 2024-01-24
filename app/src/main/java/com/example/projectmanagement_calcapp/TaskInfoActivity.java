package com.example.projectmanagement_calcapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.android.material.slider.Slider;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class TaskInfoActivity extends AppCompatActivity implements Serializable {
    PreviewView taskLocationImage;
    Button saveTaskBtn, captureBtn;
    ProcessCameraProvider cameraProvider;

    ImageCapture imageCapture;

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
        captureBtn = findViewById(R.id.captureBtn);

        try {
            populateCameraThumbnail();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("Lat", 0);
        double longitude = intent.getDoubleExtra("Long", 0);
        gpsCoords.setText(String.format("Lat: %s, Long: %s", latitude, longitude));

        captureBtn.setOnClickListener(v -> {
            try {
                capturePhoto(latitude, longitude);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void capturePhoto(double latitude, double longitude) throws ExecutionException, InterruptedException {
        if (imageCapture == null) {
            return;
        }
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String uuid = new String(java.util.UUID.randomUUID().toString());
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(new File(directory, uuid + ".png")).build();
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Bitmap bitmap = BitmapFactory.decodeFile(Objects.requireNonNull(outputFileResults.getSavedUri()).getPath());
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.preRotate(90);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
                File convertedFile = new File(directory, uuid + "-converted.png");

                OutputStream os;
                try {
                    os = new FileOutputStream(convertedFile);
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 25, os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
                }
                setListeners(convertedFile.getPath(), rotatedBitmap, uuid, latitude, longitude);

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        });
    }

    public void populateCameraThumbnail() throws ExecutionException, InterruptedException {
        PreviewView previewView = findViewById(R.id.taskLocationImage);

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();

                    startCameraX(cameraProvider, previewView);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, ContextCompat.getMainExecutor(this));

        cameraProvider = ProcessCameraProvider.getInstance(this).get();
        Preview preview = new Preview.Builder().build();
        @SuppressLint("RestrictedApi") CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.DEFAULT_BACK_CAMERA.getLensFacing())
                .build();


        Preview.SurfaceProvider surfaceProvider = previewView.getSurfaceProvider();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

    }

    private void startCameraX(ProcessCameraProvider cameraProvider, PreviewView previewView) {

        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        try{
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setListeners(String imagePath, Bitmap bitmap, String uuid, double latitude, double longitude) {
        saveTaskBtn.setOnClickListener(v -> {

            Intent returnIntent = new Intent();
            returnIntent.putExtra("imagePath", imagePath);
            returnIntent.putExtra("uuid", uuid);
            returnIntent.putExtra("taskName", taskName.getText().toString());
            returnIntent.putExtra("taskPriority", (long) taskPriority.getValue());
            returnIntent.putExtra("taskLatitude", latitude);
            returnIntent.putExtra("taskLongitude", longitude);
            setResult(1, returnIntent);
            finish();
        });
    }

}
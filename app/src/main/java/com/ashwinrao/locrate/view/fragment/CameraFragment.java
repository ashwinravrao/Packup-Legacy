package com.ashwinrao.locrate.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ashwinrao.locrate.BuildConfig;
import com.ashwinrao.locrate.R;
import com.ashwinrao.locrate.databinding.FragmentCameraBinding;
import com.ashwinrao.locrate.util.callback.BackNavCallback;
import com.ashwinrao.locrate.view.activity.CameraActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.VIBRATOR_SERVICE;


public class CameraFragment extends Fragment implements Toolbar.OnMenuItemClickListener, BackNavCallback {

    private TextureView textureView;
    private CardView shutterButton;
    private ArrayList<String> paths = new ArrayList<>();

    private static final String CLOUD_VISION_KEY = BuildConfig.CV_Key;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 5;
    private static final String TAG = "CameraFragment";

    private int useCase = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((CameraActivity) Objects.requireNonNull(getActivity())).registerBackNavigationListener(this);

        final Bundle args = getArguments();
        if (args != null) {
            useCase = args.getInt("useCase");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCameraBinding binding = FragmentCameraBinding.inflate(inflater);
        textureView = binding.preview;
        shutterButton = binding.shutter.findViewById(R.id.button);
        setupDoneButton(binding.doneButton);
        checkPermissionsBeforeBindingTextureView();
        return binding.getRoot();
    }

    private void setupDoneButton(ImageView doneButton) {
        doneButton.setOnClickListener(view -> {
            finishUpActivity();
        });
    }

    private void checkPermissionsBeforeBindingTextureView() {
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted, no need to ask
            textureView.post(this::startCamera);
        } else {
            // Permission has not been granted, ask for permission
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void finishUpActivity() {
        if (paths.size() > 0) {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("paths", paths);
            Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK, intent);
        }
        Objects.requireNonNull(getActivity()).finish();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.toolbar_done) {
            finishUpActivity();
            return true;
        }
        return false;
    }


    private void startCamera() {

        DisplayMetrics metrics = new DisplayMetrics();
        textureView.getDisplay().getRealMetrics(metrics);
        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);
        Rational screenAspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);

        final PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetResolution(screenSize)
                .build();

        final Preview preview = new Preview(previewConfig);

        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup parent = (ViewGroup) textureView.getParent();
            parent.removeView(textureView);
            parent.addView(textureView, 0);

            textureView.setSurfaceTexture(output.getSurfaceTexture());
        });

        final ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetResolution(screenSize)
                .build();

        final ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);
        shutterButton.setOnClickListener(v -> {

            final File file = new File(Objects.requireNonNull(getActivity()).getExternalMediaDirs()[0], System.currentTimeMillis() + ".jpg");
            imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {

                @Override
                public void onImageSaved(@NonNull File file) {
                    paths.add(file.getAbsolutePath());

                    // Provide vibration feedback (check for API deprecation)
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
                        ((Vibrator) Objects.requireNonNull(getActivity()).getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        ((Vibrator) Objects.requireNonNull(getActivity()).getSystemService(VIBRATOR_SERVICE)).vibrate(50);
                    }

                    if(useCase == 1) {
                        try {
                            final FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(getContext(), Uri.fromFile(file));
                            final FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
                            recognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    final List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
                                    if(blocks.size() == 0) {
                                        Toast.makeText(getContext(), "No text found", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    for(int i = 0; i < blocks.size(); i++) {
                                        final List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                                        for(int j = 0; j < lines.size(); j++) {
                                            final List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                                            for(int k = 0; k < elements.size(); k++) {
                                                Toast.makeText(getContext(), elements.get(k).getText(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (IOException e) {
                            Log.e(TAG, "onImageSaved: " + e.getMessage());
                        }
                    } else {
                        // Notify user of saved image
                        Toast toast = Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT);
                        toast.setGravity(toast.getGravity(), toast.getXOffset(), 500);
                        toast.show();
                    }

                }

                @Override
                public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                    Toast toast = Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT);
                    toast.setGravity(toast.getGravity(), toast.getXOffset(), 500);
                    toast.show();
                    Log.e(TAG, message);
                }
            });
        });

        CameraX.bindToLifecycle(this, preview, imageCapture);
    }

    private static int getCameraPermissionRequestCode() {
        return CAMERA_PERMISSION_REQUEST_CODE;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == getCameraPermissionRequestCode()) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                textureView.post(this::startCamera);
            } else {
                // Permission was denied
                // Notify the user the camera functionality will not be available
                Toast.makeText(getActivity(), "You will not be able to take photos unless you grant permission to use the camera.", Toast.LENGTH_SHORT).show();
                Objects.requireNonNull(getActivity()).finish();
            }

        }
    }

    @Override
    public void onBackPressed() {
        finishUpActivity();

    }
}

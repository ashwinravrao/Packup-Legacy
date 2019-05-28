package com.ashwinrao.boxray.view;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.ashwinrao.boxray.Boxray;
import com.ashwinrao.boxray.R;
import com.ashwinrao.boxray.databinding.FragmentCameraBinding;
import com.ashwinrao.boxray.viewmodel.BoxViewModel;
import com.camerakit.CameraKitView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

import javax.inject.Inject;

public class CameraFragment extends Fragment implements Toolbar.OnMenuItemClickListener {

    private BoxViewModel viewModel;
    private CameraKitView camera;

    private static final String TAG = "CameraFragment";

    @Inject
    ViewModelProvider.Factory factory;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((Boxray) context.getApplicationContext()).getAppComponent().inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), factory).get(BoxViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCameraBinding binding = FragmentCameraBinding.inflate(inflater);
        camera = binding.camera;
        setupToolbar(binding.toolbar);
        setupShutterButton(binding.shutter.findViewById(R.id.button));
        return binding.getRoot();
    }

    private void setupShutterButton(CardView button) {
        button.setOnClickListener(view -> camera.captureImage((cameraKitView, bytes) -> {
            final File savedPhoto = new File(Objects.requireNonNull(getActivity()).getExternalMediaDirs()[0],System.currentTimeMillis() + ".jpg");

            try {
                FileOutputStream outputStream = new FileOutputStream(savedPhoto.getAbsolutePath());
                outputStream.write(bytes);
                outputStream.close();
                Toast toast = Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT);
                toast.setGravity(toast.getGravity(), toast.getXOffset(), 500);
                toast.show();
            } catch (java.io.IOException e) {
                Log.e(TAG, "setupShutterButton: " + e.getMessage());
            }
        }));
    }

    private void setupToolbar(Toolbar toolbar) {
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.toolbar_camera);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(view -> {
            Objects.requireNonNull(getActivity()).finish();
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.toolbar_done) {
            // todo save photos to box instance
            Objects.requireNonNull(getActivity()).finish();
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        camera.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        camera.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        camera.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        camera.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        camera.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}

package com.ashwinrao.locrate.view.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ashwinrao.locrate.R;
import com.ashwinrao.locrate.util.callback.BackNavCallback;
import com.ashwinrao.locrate.view.fragment.CameraFragment;

public class CameraActivity extends AppCompatActivity {

    private BackNavCallback listener = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(savedInstanceState == null) {
            CameraFragment fragment = new CameraFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,
                            fragment,
                            "CameraFragment")
                    .commit();
        }
    }

    public void registerBackNavigationListener(@NonNull BackNavCallback listener) {
        this.listener = listener;
    }

    public void unregisterBackNavigationListener() {
        this.listener = null;
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getFragments().get(0).getClass() == CameraFragment.class) {
            if (listener != null) {
                listener.onBackPressed();
            }
        }
        super.onBackPressed();
    }
}

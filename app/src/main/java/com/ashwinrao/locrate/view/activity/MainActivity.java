package com.ashwinrao.locrate.view.activity;

import android.drm.DrmStore;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ashwinrao.locrate.R;
import com.ashwinrao.locrate.util.callback.BackNavCallback;
import com.ashwinrao.locrate.view.fragment.HomeFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


public class MainActivity extends AppCompatActivity {

    private BackNavCallback listener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if(fragment == null) {
            fragment = new HomeFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, "HomeFragment")
                .commit();
    }

    public void registerBackNavigationListener(@NonNull BackNavCallback listener) {
        this.listener = listener;
    }

    public void unregisterBackNavigationListener() {
        this.listener = null;
    }

    @Override
    public void onBackPressed() {
        final Class topFragment = getSupportFragmentManager().getFragments().get(0).getClass();
        if(topFragment == HomeFragment.class) {
            if (listener != null) {
                listener.onBackPressed();
            }
        }
        super.onBackPressed();
    }

    public void startActionMode(ActionMode.Callback callback) {
        this.startSupportActionMode(callback);
    }
}

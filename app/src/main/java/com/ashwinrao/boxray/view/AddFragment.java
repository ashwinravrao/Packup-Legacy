package com.ashwinrao.boxray.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ashwinrao.boxray.Boxray;
import com.ashwinrao.boxray.R;
import com.ashwinrao.boxray.databinding.FragmentAddBinding;
import com.ashwinrao.boxray.util.BackNavCallback;
import com.ashwinrao.boxray.util.CameraInitCallback;
import com.ashwinrao.boxray.util.Utilities;
import com.ashwinrao.boxray.view.adapter.ThumbnailAdapter;
import com.ashwinrao.boxray.viewmodel.BoxViewModel;
import com.ashwinrao.boxray.viewmodel.PhotoViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

public class AddFragment extends Fragment implements Toolbar.OnMenuItemClickListener, CameraInitCallback, BackNavCallback {

    private boolean nameError;
    private boolean[] fieldsUnsaved = new boolean[]{false, false, false};
    private BoxViewModel viewModel;
    private PhotoViewModel photoViewModel;
    private FragmentAddBinding binding;
    private RecyclerView recyclerView;
    private ThumbnailAdapter adapter;
//    private List<String> localPathsCopy;

    private final String PREF_ID_KEY = "next_available_id";
    private static final String TAG = "AddFragment";

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
        ((AddActivity) Objects.requireNonNull(getActivity())).registerBackNavigationListener(this);
        viewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), factory).get(BoxViewModel.class);
        photoViewModel = ViewModelProviders.of(getActivity(), factory).get(PhotoViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater);

        // data binding
        binding.setBoxNum(getBoxNumber());
        binding.setNumItems(getString(R.string.num_items_default));

        // widgets
        setupToolbar(binding.toolbar);
        setupNameField(binding.nameEditText);
        setupDescriptionField(binding.descriptionEditText);
        setupRecyclerView(binding.recyclerView);
        setupPrioritySwitch(binding.prioritySwitch);

        return binding.getRoot();
    }

    private void setupPrioritySwitch(SwitchCompat unpackSwitch) {
        unpackSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.getBox().setPriority(isChecked);
        });
    }

    private SharedPreferences getSharedPreferences(@NonNull Activity activity) {
        return activity.getPreferences(Context.MODE_PRIVATE);
    }

    private int getBoxNumber() {
        // Retrieve next available id
        return getSharedPreferences(Objects.requireNonNull(getActivity())).getInt(PREF_ID_KEY, 1);
    }

    private void saveBoxNumber() {
        SharedPreferences sharedPref = getSharedPreferences(Objects.requireNonNull(getActivity()));
        int nextAvailableId = sharedPref.getInt(PREF_ID_KEY, 1);

        // Store next available id
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(PREF_ID_KEY, nextAvailableId + 1);
        editor.apply();  // .apply() >= .commit()
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        Utilities.addItemDecoration(getContext(), recyclerView, 2);
        adapter = new ThumbnailAdapter(getContext(), Utilities.dpToPx(Objects.requireNonNull(getContext()), 150f), Utilities.dpToPx(getContext(), 150f));
        adapter.registerStartCameraListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupDescriptionField(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    viewModel.getBox().setDescription(s.toString());
                    fieldsUnsaved[1] = true;
                } else {
                    viewModel.getBox().setDescription(null);
                    fieldsUnsaved[1] = false;
                }
            }
        });
    }

    private void setupNameField(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    viewModel.getBox().setName(s.toString());
                    fieldsUnsaved[0] = true;
                } else {
                    viewModel.getBox().setName(null);
                    fieldsUnsaved[0] = false;
                }
            }
        });
    }

    private void setupToolbar(Toolbar toolbar) {
        toolbar.inflateMenu(R.menu.toolbar_add);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(v -> {
            AlertDialog dialog = createUnsavedChangesDialog(getContext());
            if (fieldsUnsaved[0] || fieldsUnsaved[1] || fieldsUnsaved[2]) {
                dialog.show();
            } else {
                Objects.requireNonNull(getActivity()).finish();
            }
        });
    }

    private AlertDialog createUnsavedChangesDialog(Context context) {
        return new AlertDialog.Builder(context)
                .setTitle(getString(R.string.dialog_discard_box_title))
                .setMessage(getString(R.string.dialog_discard_box_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), (dialog1, which) -> {
                    if(photoViewModel.getPaths() != null) {
                        for (String path : photoViewModel.getPaths()) {
                            new File(path).delete();
                        }
                    }
                    Objects.requireNonNull(getActivity()).finish();
                })
                .setNegativeButton(getResources().getString(R.string.no), (dialog12, which) -> dialog12.cancel())
                .create();
    }

    private AlertDialog createEmptyBoxDialog(Context context) {
        return new AlertDialog.Builder(context)
                .setTitle(getString(R.string.dialog_empty_box_title))
                .setMessage(getResources().getString(R.string.dialog_empty_box_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok), (dialog1, which) -> {
                    dialog1.cancel();
                })
                .setNegativeButton(getResources().getString(R.string.discard), (dialog12, which) -> Objects.requireNonNull(getActivity()).finish())
                .create();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.toolbar_done) {
            if (viewModel.getBox().getContents() == null || viewModel.getBox().getContents().size() < 1) {
                createEmptyBoxDialog(getContext()).show();
                return true;
            } else {
                if (viewModel.saveBox()) {
                    saveBoxNumber();
                    Objects.requireNonNull(getActivity()).finish();
                    return true;
                } else {
                    nameError = true;
                    binding.nameEditText.setError(getResources().getString(R.string.name_field_error_message));
                    binding.nameEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            nameError = false;
                            binding.nameEditText.setError(null);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                    return true;
                }
            }
        }
        return false;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(photoViewModel.getPaths() != null) {
//            viewModel.getBox().setContents(photoViewModel.getPaths());
//            adapter.setPaths(photoViewModel.getPaths());
//            recyclerView.setAdapter(adapter);
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                ArrayList<String> paths = data.getStringArrayListExtra("paths");
                if(paths != null) {
                    fieldsUnsaved[2] = true;
                    adapter.setPaths(paths);
                    recyclerView.setAdapter(adapter);
                    photoViewModel.setPaths(paths);
                    Log.d(TAG, "onActivityResult: num photos saved to photoViewModel: " + photoViewModel.getPaths().size());
                    viewModel.getBox().setContents(paths);
                    binding.setNumItems(viewModel.getBox().getNumItems());
                }

//                photoViewModel.setPaths(paths);
//                Objects.requireNonNull(getActivity())
//                        .getSupportFragmentManager()
//                        .beginTransaction()
//                        .addToBackStack(null)
//                        .replace(R.id.fragment_container, new PhotoReviewFragment())
//                        .commit();
            }
        }
    }

    @Override
    public void startCamera() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = createUnsavedChangesDialog(getContext());
        if (fieldsUnsaved[0] || fieldsUnsaved[1] || fieldsUnsaved[2]) {
            dialog.show();
        } else {
            Objects.requireNonNull(getActivity()).finish();
        }
    }
}

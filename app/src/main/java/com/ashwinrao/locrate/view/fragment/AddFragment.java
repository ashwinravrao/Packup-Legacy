package com.ashwinrao.locrate.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ashwinrao.locrate.Locrate;
import com.ashwinrao.locrate.R;
import com.ashwinrao.locrate.databinding.FragmentAddBinding;
import com.ashwinrao.locrate.util.callback.BackNavCallback;
import com.ashwinrao.locrate.util.callback.CameraInitCallback;
import com.ashwinrao.locrate.view.ConfirmationDialog;
import com.ashwinrao.locrate.view.activity.AddActivity;
import com.ashwinrao.locrate.view.activity.CameraActivity;
import com.ashwinrao.locrate.view.adapter.ThumbnailAdapter;
import com.ashwinrao.locrate.viewmodel.BoxViewModel;
import com.ashwinrao.locrate.viewmodel.PhotoViewModel;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static com.ashwinrao.locrate.util.Decorations.addItemDecoration;
import static com.ashwinrao.locrate.util.UnitConversion.dpToPx;

public class AddFragment extends Fragment implements Toolbar.OnMenuItemClickListener, CameraInitCallback, BackNavCallback {

    private BoxViewModel viewModel;
    private PhotoViewModel photoViewModel;
    private FragmentAddBinding binding;
    private RecyclerView recyclerView;
    private ThumbnailAdapter adapter;
    private List<String> compoundedItems = new ArrayList<>();

    private final String PREF_ID_KEY = "next_available_id";
    private static final String TAG = "AddFragment";

    @Inject
    ViewModelProvider.Factory factory;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ((Locrate) context.getApplicationContext()).getAppComponent().inject(this);
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
        binding.setBoxId(getBoxNumber());
        binding.setNumberOfItems(getString(R.string.num_items_default));

        // widgets
        setupToolbar(binding.toolbar);
        setupNameField(binding.nameInputField);
        setupDescriptionField(binding.descriptionInputField);
        setupRecyclerView(binding.recyclerView);
        setupFillButton(binding.fillButton);

        return binding.getRoot();
    }

    private void setupSaveButton(MaterialCardView saveButton) {
        saveButton.setOnClickListener(view -> {
            if (viewModel.getBox().getContents() == null || viewModel.getBox().getContents().size() < 1) {
                showEmptyBoxDialog();
            } else {
                if (viewModel.saveBox()) {
                    saveBoxNumber();
                    Objects.requireNonNull(getActivity()).finish();
                } else {
                    Objects.requireNonNull(binding.nameInputField).setError(getResources().getString(R.string.name_field_error_message));
                    binding.nameInputField.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            binding.nameInputField.setError(null);
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }
            }

        });
    }

    private void setupFillButton(MaterialCardView cardView) {
        cardView.setOnClickListener(view -> startCamera());
    }

    private SharedPreferences getSharedPreferences(@NonNull Activity activity) {
        return activity.getPreferences(Context.MODE_PRIVATE);
    }

    private int getBoxNumber() {
        // Retrieve next available id
        int lastUsed = getSharedPreferences(Objects.requireNonNull(getActivity())).getInt(PREF_ID_KEY, 1);
        viewModel.getBox().setId(String.valueOf(lastUsed + 1));
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
        addItemDecoration(getContext(), recyclerView, 2);
        adapter = new ThumbnailAdapter(getContext(), dpToPx(Objects.requireNonNull(getContext()), 150f), dpToPx(getContext(), 150f));
        adapter.registerStartCameraListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupDescriptionField(@NonNull EditText editText) {
        Objects.requireNonNull(editText).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.charCount.setText(String.valueOf(s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String description = s.toString().length() > 0 ? s.toString() : null;
                viewModel.getBox().setDescription(description);
            }
        });
    }

    private void setupNameField(@NonNull EditText editText) {
        Objects.requireNonNull(editText).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String name = s.toString().length() > 0 ? s.toString() : null;
                viewModel.getBox().setName(name);
            }
        });
    }

    private void setupToolbar(Toolbar toolbar) {
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(v -> closeWithConfirmation());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.toolbar_done) {
            if (viewModel.getBox().getContents() == null || viewModel.getBox().getContents().size() < 1) {
                showEmptyBoxDialog();
                return true;
            } else {
                if (viewModel.saveBox()) {
                    saveBoxNumber();
                    Objects.requireNonNull(getActivity()).finish();
                    return true;
                } else {
                    Objects.requireNonNull(binding.nameInputField).setError(getResources().getString(R.string.name_field_error_message));
                    binding.nameInputField.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            binding.nameInputField.setError(null);
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

    @Override
    public void onResume() {
        super.onResume();
        updateItems();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                final ArrayList<String> paths = Objects.requireNonNull(data).getStringArrayListExtra("paths");
                if (paths != null) {
                    photoViewModel.setPaths(paths);
                }

                Objects.requireNonNull(getActivity())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .setCustomAnimations(0, 0, 0, R.anim.slide_down_out)
                        .replace(R.id.fragment_container, new PhotoFragment())
                        .commit();
            }
        }
    }

    private void updateItems() {
        if (photoViewModel.getPaths() != null && !photoViewModel.getPaths().equals(compoundedItems)) {
            compoundedItems.addAll(photoViewModel.getPaths());
        }
        if (compoundedItems.size() > 0) {
            adapter.setPaths(compoundedItems);
            recyclerView.setAdapter(adapter);
            viewModel.getBox().setContents(compoundedItems);
            binding.setNumberOfItems(viewModel.getBox().getNumItems());
        }
    }

    @Override
    public void startCamera() {
        Intent intent = new Intent(getActivity(), CameraActivity.class);
        photoViewModel.clearPaths();
        startActivityForResult(intent, 1);
    }

    private void closeWithConfirmation() {
        if (viewModel.areChangesUnsaved()) {
            showUnsavedChangesDialog();
        } else {
            Objects.requireNonNull(getActivity()).finish();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void showUnsavedChangesDialog() {
        ConfirmationDialog.make(getContext(), new String[]{
                getString(R.string.dialog_discard_box_title),
                getString(R.string.dialog_discard_box_message),
                getString(R.string.discard),
                getString(R.string.no)}, false, new int[]{ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent),
                ContextCompat.getColor(Objects.requireNonNull(getContext()), android.R.color.holo_red_dark)}, dialogInterface -> {
            if (photoViewModel.getPaths() != null) {
                for (String path : photoViewModel.getPaths()) {
                    new File(path).delete();
                }
            }
            Objects.requireNonNull(getActivity()).finish();
            return null;
        }, dialogInterface -> {
            dialogInterface.cancel();
            return null;
        });
    }

    private void showEmptyBoxDialog() {
        ConfirmationDialog.make(getContext(), new String[]{
                        getString(R.string.dialog_empty_box_title),
                        getString(R.string.dialog_empty_box_message),
                        getString(R.string.ok),
                        getString(R.string.discard)},
                false,
                new int[]{ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent),
                        ContextCompat.getColor(getContext(), R.color.colorAccent)},
                dialogInterface -> {
                    dialogInterface.cancel();
                    return null;
                }, dialogInterface -> {
                    Objects.requireNonNull(getActivity()).finish();
                    return null;
                });
    }

    @Override
    public void onBackPressed() {
        closeWithConfirmation();
    }
}

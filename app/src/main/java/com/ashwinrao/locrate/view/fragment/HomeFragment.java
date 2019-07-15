package com.ashwinrao.locrate.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.ashwinrao.locrate.Locrate;
import com.ashwinrao.locrate.R;
import com.ashwinrao.locrate.data.model.Box;
import com.ashwinrao.locrate.databinding.FragmentHomeBinding;
import com.ashwinrao.locrate.util.callback.BackNavCallback;
import com.ashwinrao.locrate.util.callback.UpdateActionModeCallback;
import com.ashwinrao.locrate.view.ConfirmationDialog;
import com.ashwinrao.locrate.view.CustomViewPager;
import com.ashwinrao.locrate.view.activity.MainActivity;
import com.ashwinrao.locrate.view.adapter.HomePagerAdapter;
import com.ashwinrao.locrate.view.fragment.pages.BoxesPage;
import com.ashwinrao.locrate.view.fragment.pages.ItemsPage;
import com.ashwinrao.locrate.viewmodel.BoxViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import javax.inject.Inject;

import static com.ashwinrao.locrate.util.UnitConversion.dpToPx;


public class HomeFragment extends Fragment implements BackNavCallback, UpdateActionModeCallback {

    private MenuItem search;
    private SearchView searchView;
    private CustomViewPager viewPager;
    private TabLayout tabLayout;
    private BoxesPage boxesPage;
    private ItemsPage itemsPage;
    private ActionMode actionMode;
    private BoxViewModel boxViewModel;
    private Bundle savedInstanceState;
    private boolean wasBackPressed;

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
        setHasOptionsMenu(true);
        this.savedInstanceState = savedInstanceState;
        boxViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), factory).get(BoxViewModel.class);
        ((MainActivity) Objects.requireNonNull(getActivity())).registerBackNavigationListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        wasBackPressed = false;
        if (search != null) {
            search.collapseActionView();
            viewPager.setPagingEnabled(true);
        }
        if(this.savedInstanceState != null) {
            initializeTabLayout(tabLayout, viewPager);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) Objects.requireNonNull(getActivity())).unregisterBackNavigationListener();
    }

    /***
     * Custom callback function that overrides the system level callback of the same name
     *
     * @return boolean value indicating whether the back press was consumed completely by the custom callback.
     * If the back press was consumed completely, no super calls are necessary.
     */

    @Override
    public boolean onBackPressed() {
        if(!wasBackPressed) {
            Toast.makeText(getActivity(), "Press back again to exit", Toast.LENGTH_SHORT).show();
            wasBackPressed = !wasBackPressed;
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater);
        final AppCompatActivity parent = ((MainActivity) getActivity());
        initializeToolbar(Objects.requireNonNull(parent), binding.toolbar);
        initializeTabLayout(binding.listTabLayout, binding.listViewPager);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_toolbar_list, menu);
        search = menu.findItem(R.id.toolbar_search);
        SearchView searchView = (SearchView) search.getActionView();
        configureSearchView(searchView);
    }

    private void configureSearchView(SearchView searchView) {
        this.searchView = searchView;
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint(getResources().getString(R.string.boxes_query_hint));
        searchView.setPadding(dpToPx(Objects.requireNonNull(getActivity()), -16f), 0, 0, dpToPx(getActivity(), -1f));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (viewPager.getCurrentItem() == 0) {
                    boxesPage.onQueryTextChange(newText);
                } else {
                    itemsPage.onQueryTextChange(newText);
                }

                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                searchView.setQueryHint(position <= 0 ? getResources().getString(R.string.boxes_query_hint) : getResources().getString(R.string.items_query_hint));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            viewPager.setPagingEnabled(((SearchView) v).getQuery().toString().length() <= 0 && !hasFocus);
        });
    }

    private void inflateBottomSheet() {
        final BottomSheetFragment bottomSheet = new BottomSheetFragment();
        bottomSheet.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), bottomSheet.getTag());
    }

    private void initializeTabLayout(@NonNull TabLayout tabLayout, @NonNull CustomViewPager viewPager) {
        this.boxesPage = new BoxesPage();
        boxesPage.setCallback(this);
        this.itemsPage = new ItemsPage();

        final HomePagerAdapter listPagerAdapter = new HomePagerAdapter(getChildFragmentManager(), boxesPage, itemsPage);
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
        viewPager.setAdapter(listPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void initializeToolbar(@NonNull AppCompatActivity parent, @NonNull Toolbar toolbar) {
        parent.setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> inflateBottomSheet());
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_overflow, Objects.requireNonNull(getActivity()).getTheme()));
    }

    @Override
    public boolean update(List<Object> objects, String objectType) {

        if (actionMode == null && !searchView.hasFocus()) {
            ((MainActivity) Objects.requireNonNull(getActivity())).startActionMode(new ActionModeCallback());
            actionMode.setTitle(String.format(getString(R.string.action_mode_title), objects.size(), objectType));
            viewPager.setPagingEnabled(false);
            return true;
        }

        if(actionMode != null) {
            actionMode.setTitle(String.format(getString(R.string.action_mode_title), objects.size(), objectType));
            return true;
        }

        return false;
    }

    private void showBulkDeleteConfirmationDialog(@NonNull List<Box> toDelete, @NonNull ActionMode mode) {
        ConfirmationDialog.make(getContext(), new String[]{
                getString(R.string.dialog_bulk_delete_title),
                getString(R.string.dialog_bulk_delete_message),
                getString(R.string.delete),
                getString(R.string.cancel)}, true, new int[]{ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorAccent),
                ContextCompat.getColor(getContext(), android.R.color.holo_red_dark)}, dialogInterface -> {
            boxViewModel.deleteMultiple(toDelete);
            mode.finish();
            return null;
        }, dialogInterface -> {
            dialogInterface.cancel();
            return null;
        });
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            mode.getMenuInflater().inflate(R.menu.action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.delete) {
                @SuppressWarnings("unchecked") final List<Box> selected = (List<Box>) (List<?>) boxesPage.getAdapter().getSelected();
                showBulkDeleteConfirmationDialog(selected, mode);
                return true;
            } else if (item.getItemId() == R.id.archive) {
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            boxesPage.getAdapter().clearSelected();
            viewPager.setPagingEnabled(true);
            actionMode = null;
        }
    }
}

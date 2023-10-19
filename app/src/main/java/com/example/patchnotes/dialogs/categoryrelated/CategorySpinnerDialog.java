package com.example.patchnotes.dialogs.categoryrelated;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.R;
import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringCategory;
import com.example.patchnotes.contentfilter.ContentFilterByCategory;
import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.database.AppDatabase;
import com.example.patchnotes.dialogs.FilterAlgorithmPromptDialog;

public class CategorySpinnerDialog extends DialogFragment {

    public interface OnCategoryChosenListener {
        void onCategoryChosen(@NonNull Category category, String tag);
    }

    private static final String SAVED_TAG = "savedTag";
    private static final String SAVED_SPINNER_SELECTED_INDEX = "savedSpinnerSelectedIndex";
    private static final String SAVED_CATEGORIES = "savedCategories";
    private static final String SAVED_TITLE = "savedTitle";

    private Spinner catSpinner;
    private TextView descriptionView;

    private String tag, title;
    private OnCategoryChosenListener categoryListener;
    private FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener filterListener;
    private Category[] categories;
    private int initialSelectedIndex = -1;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            categoryListener = (OnCategoryChosenListener) context;
        } catch (ClassCastException ignore) {

        }

        try {
            filterListener = (FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener) context;
        } catch (ClassCastException ignore) {

        }

        if (categoryListener == null && filterListener == null) {
            throw new ClassCastException("Parent activity must implement OnCategoryChosenListener or OnFilterAlgorithmSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        restoreInstanceState(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View customView = initializeCustomView();
        builder.setTitle(title);
        builder.setView(customView);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", (dI, which) -> {
            Category categoryChosen = categories[catSpinner.getSelectedItemPosition()];
            if (categoryListener != null) {
                categoryListener.onCategoryChosen(categoryChosen, tag);
            }

            if (filterListener != null) {
                filterListener.onFilterAlgorithmSelected(new ContentFilterByCategory(categoryChosen));
            }
        });

        return builder.create();
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            categories = (Category[]) savedInstanceState.getSerializable(SAVED_CATEGORIES);
            tag = savedInstanceState.getString(SAVED_TAG);
            initialSelectedIndex = savedInstanceState.getInt(SAVED_SPINNER_SELECTED_INDEX);
            title = savedInstanceState.getString(SAVED_TITLE);
        }

        if (title == null) {
            title = "Select a category";
        }
    }

    private View initializeCustomView() {
        View customView = View.inflate(getContext(), R.layout.dialog_spinner_and_description_view, null);
        catSpinner = customView.findViewById(R.id.dialogView_spinner);
        descriptionView = customView.findViewById(R.id.dialogView_textViewDescription);

        if (categories == null) {
            categories = new AppDatabase(getContext()).getCategoryDatabase().getAllCategories();
        }

        catSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, categories));
        catSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                displayDescriptionOfCategoryInPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (initialSelectedIndex != -1) {
            catSpinner.setSelection(initialSelectedIndex);
            displayDescriptionOfCategoryInPosition(initialSelectedIndex);
        } else {
            displayDescriptionOfCategoryInPosition(0);
        }

        return customView;
    }

    private void displayDescriptionOfCategoryInPosition(int pos) {
        descriptionView.setText(categories[pos].getDescription());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putSerializable(SAVED_CATEGORIES, categories);
        toSave.putInt(SAVED_SPINNER_SELECTED_INDEX, catSpinner.getSelectedItemPosition());
        toSave.putString(SAVED_TAG, tag);
        toSave.putString(SAVED_TITLE, title);
    }

    //

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Sets the category to display. If null is set, then all categories will be displayed (queried from database).
     * @param categories The categories to display. Can be empty
     */
    public void setCategories(Category[] categories) {
        this.categories = categories;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

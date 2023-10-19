package com.example.patchnotes.dialogs.categoryrelated;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.R;
import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.util.Calendar;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

public class SelectCategoryAutoFillAttributesDialog extends DialogFragment {

    public interface OnCategoryAutoFillSelectionConfirmedListener {
        void onCategoryAutoFillSelectionConfirmed(boolean inheritAutoPrioPeriod, boolean inheritAutoPrioDays,
                                                  boolean inheritAutoExpiPeriod, Category categoryId, String tag);
    }

    private static final String SAVED_SELECTED_INDICES = "savedSelectedIndices";
    private static final String SAVED_CATEGORY = "savedCategory";
    private static final String SAVED_CONTENT = "savedContent";
    private static final String SAVED_TAG = "savedTag";
    private static final String SAVED_MESSAGE = "savedMessage";
    private static final String SAVED_TITLE = "savedTitle";

    private String tag, title, message;
    private Category category;
    private Content content;
    private OnCategoryAutoFillSelectionConfirmedListener listener;
    private HashSet<Integer> selectedIndices = new HashSet<>();
    private AutoFillEntryListAdapter entryListAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnCategoryAutoFillSelectionConfirmedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnCategoryAutoFillSelectionConfirmedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        restoreInstanceState(savedInstanceState);
        entryListAdapter = constructListAdapterForSelection();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setView(constructCustomView());
        builder.setPositiveButton("Ok", (dI, which) -> {
            boolean autoFillPrioPeriod = false, autoFillPrioDays = false, autoFillExpirationPeriod = false;
            Set<Integer> selectedIndices = entryListAdapter.getSelectedIndices();
            if (selectedIndices.contains(0)) {
                autoFillPrioPeriod = true;
            }
            if (selectedIndices.contains(1)) {
                autoFillPrioDays = true;
            }
            if (selectedIndices.contains(2)) {
                autoFillExpirationPeriod = true;
            }

            listener.onCategoryAutoFillSelectionConfirmed(autoFillPrioPeriod, autoFillPrioDays,
                    autoFillExpirationPeriod, category, tag);
        });
        builder.setNegativeButton("Cancel", null);

        return builder.create();
    }

    @SuppressWarnings("unchecked")
    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            tag = savedInstanceState.getString(SAVED_TAG);
            selectedIndices.addAll((HashSet<Integer>) savedInstanceState.getSerializable(SAVED_SELECTED_INDICES));
            message = savedInstanceState.getString(SAVED_MESSAGE);
            title = savedInstanceState.getString(SAVED_TITLE);
            category = (Category) savedInstanceState.getSerializable(SAVED_CATEGORY);
            content = (Content) savedInstanceState.getSerializable(SAVED_CONTENT);
        }
    }

    private AutoFillEntryListAdapter constructListAdapterForSelection() {
        entryListAdapter = new AutoFillEntryListAdapter(getContext(), category, content.getPriorityPeriod(),
                content.getPriorityDaysOfWeek(), category.getExpirationPeriod(), selectedIndices);
        return entryListAdapter;
    }

    private View constructCustomView() {
        ListView list = (ListView) View.inflate(getContext(), R.layout.generic_list_view, null);
        list.setAdapter(entryListAdapter);
        return list;
    }

    //

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putString(SAVED_TAG, tag);
        toSave.putSerializable(SAVED_SELECTED_INDICES, (HashSet<Integer>) entryListAdapter.getSelectedIndices());
        toSave.putString(SAVED_MESSAGE, message);
        toSave.putString(SAVED_TITLE, title);
        toSave.putSerializable(SAVED_CATEGORY, category);
        toSave.putSerializable(SAVED_CONTENT, content);
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setContent(Content content) {
        this.content = content;
    }

}

class AutoFillEntryListAdapter implements ListAdapter, EntryCreator.OnEntryPressedListener {

    private Category categoryToInheritFrom;
    private Calendar currentPrioPeriod, currentExpiPeriod;
    private DaysOfWeek[] currentPrioDays;
    private Context context;
    private HashSet<Integer> selectedIndices = new HashSet<>();

    private View entryOfPrioPeriod, entryOfPrioDays, entryOfExpiPeriod;

    AutoFillEntryListAdapter(Context context, Category categoryToInheritFrom, Calendar currentPrioPeriod,
                             DaysOfWeek[] currentPrioDays, Calendar currentExpiPeriod, Set<Integer> selectedIndices) {
        this.context = context;
        this.categoryToInheritFrom = categoryToInheritFrom;
        this.currentPrioPeriod = currentPrioPeriod;
        this.currentPrioDays = currentPrioDays;
        this.currentExpiPeriod = currentExpiPeriod;
        this.selectedIndices.addAll(selectedIndices);

        constructAutoFillPrioPeriodEntry();
        constructAutoFillPrioDaysEntry();
        constructAutoFillExpiPeriodEntry();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    //

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            return constructAutoFillPrioPeriodEntry();
        } else if (position == 1) {
            return constructAutoFillPrioDaysEntry();
        } else if (position == 2) {
            return constructAutoFillExpiPeriodEntry();
        }
        return null;
    }

    private View constructAutoFillPrioPeriodEntry() {
        if (entryOfPrioPeriod == null) {
            boolean isSelected = false;
            if (selectedIndices.contains(0)) {
                isSelected = true;
            }

            entryOfPrioPeriod = new EntryCreator(context, 0, this).createPeriodEntryWithData("Priority Period",
                    "AutoFill Priority Period:", "Current Priority Period:",
                    categoryToInheritFrom.getAutoFillPriorityPeriod(), currentPrioPeriod, isSelected);
        }
        return entryOfPrioPeriod;
    }

    private View constructAutoFillPrioDaysEntry() {
        if (entryOfPrioDays == null) {
            boolean isSelected = false;
            if (selectedIndices.contains(1)) {
                isSelected = true;
            }

            entryOfPrioDays = new EntryCreator(context, 1, this).createDaysEntryWithData("Priority Days",
                    "AutoFill Priority Days:", "Current Priority Days:",
                    categoryToInheritFrom.getAutoFillPriorityDaysOfWeek(), currentPrioDays, isSelected);
        }
        return entryOfPrioDays;
    }

    private View constructAutoFillExpiPeriodEntry() {
        if (entryOfExpiPeriod == null) {
            boolean isSelected = false;
            if (selectedIndices.contains(2)) {
                isSelected = true;
            }

            entryOfExpiPeriod = new EntryCreator(context, 2, this).createPeriodEntryWithData("Expiration Period",
                    "AutoFill Expiration Period:", "Current Expiration Period:",
                    categoryToInheritFrom.getAutoFillExpirationPeriod(), currentExpiPeriod, isSelected);
        }
        return entryOfExpiPeriod;
    }

    @Override
    public void onEntryPressed(int position, boolean isChecked) {
        if (isChecked) {
            selectedIndices.add(position);
        } else {
            selectedIndices.remove(position);
        }
    }

    //

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    //

    public Set<Integer> getSelectedIndices() {
        return new HashSet<>(selectedIndices);
    }
}

class EntryCreator {

    interface OnEntryPressedListener {
        void onEntryPressed(int position, boolean isChecked);
    }

    private Context context;
    private int position;
    private OnEntryPressedListener listener;

    EntryCreator(Context context, int position, OnEntryPressedListener listener) {
        this.context = context;
        this.position = position;
        this.listener = listener;
    }

    View createPeriodEntryWithData(String title, String textLabelForCat, String textLabelForContent,
                                   Calendar categoryTime, Calendar contentTime, boolean isInitialyChecked) {
        View entryView = View.inflate(context, R.layout.dialog_auto_values_configuration_text_description_and_enable, null);

        ((TextView) entryView.findViewById(R.id.auto_values_config_main_text)).setText(title);
        ((TextView) entryView.findViewById(R.id.auto_values_config_main_subText)).setText(getEntryPeriodViewDescription(
                textLabelForCat, categoryTime, textLabelForContent, contentTime));

        CheckBox checkBox = entryView.findViewById(R.id.auto_values_config_checkbox);
        checkBox.setChecked(isInitialyChecked);

        entryView.setOnClickListener(view -> {
            checkBox.setChecked(!checkBox.isChecked());
            listener.onEntryPressed(position, checkBox.isChecked());
        });

        return entryView;
    }

    private String getEntryPeriodViewDescription(String textLabelForCat, Calendar categoryTime,
                                                 String textLabelForContent, Calendar contentTime) {
        StringBuilder builder = new StringBuilder();
        builder.append(getTimeDescription(textLabelForCat, categoryTime)).append("\n")
                .append(getTimeDescription(textLabelForContent, contentTime));

        return builder.toString();
    }

    private String getTimeDescription(String descriptor, Calendar timePeriod) {
        StringBuilder builder = new StringBuilder()
                .append(descriptor).append(" ");

        if (timePeriod != null) {
            builder.append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.MONTH))).append("/")
                    .append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.DAY_OF_MONTH))).append("/")
                    .append(timePeriod.get(Calendar.YEAR));

            builder.append(" at ")
                    .append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.HOUR_OF_DAY))).append(":")
                    .append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.MINUTE)));
        } else {
            builder.append("No value set");
        }

        return builder.toString();
    }

    private String getTimeTwoDigitDisplay(int time) {
        Formatter formatter = new Formatter();
        formatter.format("%02d", time);
        return formatter.toString();
    }

    View createDaysEntryWithData(String title, String textLabelForCat, String textLabelForContent,
                                 DaysOfWeek[] categoryDays, DaysOfWeek[] contentDays, boolean isChecked) {
        View entryView = View.inflate(context, R.layout.dialog_auto_values_configuration_text_description_and_enable, null);

        ((TextView) entryView.findViewById(R.id.auto_values_config_main_text)).setText(title);
        ((TextView) entryView.findViewById(R.id.auto_values_config_main_subText)).setText(getEntryDaysViewDescription(
                textLabelForCat, categoryDays, textLabelForContent, contentDays
        ));
        CheckBox checkBox = entryView.findViewById(R.id.auto_values_config_checkbox);
        checkBox.setChecked(isChecked);

        entryView.setOnClickListener(view -> {
            checkBox.setChecked(!checkBox.isChecked());
            listener.onEntryPressed(position, checkBox.isChecked());
        });
        return entryView;
    }

    private String getEntryDaysViewDescription(String textLabelForCat, DaysOfWeek[] categoryDays,
                                               String textLabelForContent, DaysOfWeek[] contentDays) {
        StringBuilder builder = new StringBuilder();
        builder.append(getDaysOfWeekDescription(textLabelForCat, categoryDays)).append("\n")
                .append(getDaysOfWeekDescription(textLabelForContent, contentDays));
        return builder.toString();
    }

    private String getDaysOfWeekDescription(String descriptor, DaysOfWeek[] daysOfWeek) {
        StringBuilder builder = new StringBuilder();

        builder.append(descriptor).append(" ");

        if (daysOfWeek != null && daysOfWeek.length != 0) {
            for (int i = 0; i < daysOfWeek.length; i++) {
                if (i != daysOfWeek.length - 1) {
                    builder.append(daysOfWeek[i].getRepresentation() + ", ");
                } else {
                    builder.append(daysOfWeek[i].getRepresentation());
                }
            }
        } else {
            builder.append("No value set");
        }
        return builder.toString();
    }

}

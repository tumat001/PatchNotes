package com.example.patchnotes.fragments;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.patchnotes.R;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.util.Calendar;
import java.util.Formatter;

/**
 * A simple {@link Fragment} subclass.
 */
public class AutoValuesConfigurationFragment extends Fragment implements AdapterView.OnItemClickListener,
        AutoValuesConfigurationListAdapter.OnAutoFillStatusChangedListener {

    public AutoValuesConfigurationFragment() {
        // Required empty public constructor
    }

    public interface OnConfigurationValuePressedListener {
        void onAutoFillStatusChanged(boolean autoFillEnabled);
        void onConfigurePriorityPeriodPressed(Calendar currentPriorityPeriod);
        void onConfigurePriorityDaysPressed(DaysOfWeek[] currentPriorityDays);
        void onConfigureExpirationPeriodPressed(Calendar currentExpirationPeriod);
    }

    private Calendar priorityPeriod, expirationPeriod;
    private DaysOfWeek[] daysOfWeeks;
    private boolean isConfigurationEnabled;

    private ListView configurationList;
    private AutoValuesConfigurationListAdapter adapter;
    private OnConfigurationValuePressedListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnConfigurationValuePressedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity does not implement OnConfigurationValuePressedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auto_values_configuration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        configurationList = view.findViewById(R.id.fragment_autoValuesConfigurationList);

        adapter = new AutoValuesConfigurationListAdapter(getContext(), isConfigurationEnabled, priorityPeriod,
                expirationPeriod, daysOfWeeks, this);

        configurationList.setAdapter(adapter);
        configurationList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
        if (view.isEnabled()) {
            if (pos == 2) {
                listener.onConfigurePriorityPeriodPressed(priorityPeriod);
            } else if (pos == 3) {
                listener.onConfigurePriorityDaysPressed(daysOfWeeks);
            } else if (pos == 4) {
                listener.onConfigureExpirationPeriodPressed(expirationPeriod);
            }
        }
    }

    @Override
    public void onAutoFillEnabled() {
        listener.onAutoFillStatusChanged(true);
        isConfigurationEnabled = true;
    }

    @Override
    public void onAutoFillDisabled() {
        listener.onAutoFillStatusChanged(false);
    }

    //

    public void setPriorityPeriod(Calendar priorityPeriod) {
        this.priorityPeriod = priorityPeriod;
        if (adapter != null) {
            updatePriorityPeriodDescriptionDisplay();
        }
    }

    private void updatePriorityPeriodDescriptionDisplay() {
        adapter.updateDescriptionStatusOfPriorityPeriod(priorityPeriod);
    }

    public void setPriorityDays(DaysOfWeek[] days) {
        this.daysOfWeeks = days;
        if (adapter != null) {
            updatePriorityDaysDescriptionDisplay();
        }
    }

    private void updatePriorityDaysDescriptionDisplay() {
        adapter.updateDescriptionStatusOfPriorityDays(daysOfWeeks);
    }

    public void setExpirationPeriod(Calendar expirationPeriod) {
        this.expirationPeriod = expirationPeriod;
        if (adapter != null) {
            updateExpirationPeriodDescriptionDisplay();
        }
    }

    private void updateExpirationPeriodDescriptionDisplay() {
        adapter.updateDescriptionStatusOfExpirationPeriod(expirationPeriod);
    }

    public void setConfigurationListEnabled(boolean enable) {
        this.isConfigurationEnabled = enable;
        if (adapter != null) {
            if (enable) {
                adapter.enableManageConfigurationsButtons();
            } else {
                adapter.disableManageConfigurationsButtons();
            }
        }
    }
}

class AutoValuesConfigurationListAdapter implements ListAdapter, CompoundButton.OnCheckedChangeListener {

    interface OnAutoFillStatusChangedListener {
        void onAutoFillEnabled();
        void onAutoFillDisabled();
    }

    AutoValuesConfigurationListAdapter(Context context, boolean autoFillEnabled,
                                       Calendar autoPrioPeriod, Calendar autoExpiPeriod, DaysOfWeek[] autoDaysOfWeeks,
                                       OnAutoFillStatusChangedListener listener) {
        this.context = context;
        this.autoDaysOfWeek = autoDaysOfWeeks;
        this.autoPrioPeriod = autoPrioPeriod;
        this.autoExpiPeriod = autoExpiPeriod;
        this.autoFillEnabled = autoFillEnabled;
        this.listener = listener;

        if (autoFillEnabled) {
            enableManageConfigurationsButtons();
        } else {
            disableManageConfigurationsButtons();
        }
    }

    private static final int HEADER = 0;
    private static final int ENABLE = 1;
    private static final int MANAGE_ITEM_TYPE = 2;

    private View enableButtonView, managePrioPeriodView, managePrioDaysView, manageExpiPeriodView;

    private Context context;
    private boolean autoFillEnabled;
    private Calendar autoPrioPeriod, autoExpiPeriod;
    private DaysOfWeek[] autoDaysOfWeek;
    private OnAutoFillStatusChangedListener listener;

    public void enableManageConfigurationsButtons() {
        if (managePrioDaysView != null) {
            managePrioDaysView.setEnabled(true);
            managePrioDaysView.setBackgroundResource(R.color.categoryColorEnabled);
        }

        if (managePrioPeriodView != null) {
            managePrioPeriodView.setEnabled(true);
            managePrioPeriodView.setBackgroundResource(R.color.categoryColorEnabled);
        }

        if (manageExpiPeriodView != null) {
            manageExpiPeriodView.setEnabled(true);
            manageExpiPeriodView.setBackgroundResource(R.color.categoryColorEnabled);
        }
    }

    public void disableManageConfigurationsButtons() {
        if (managePrioDaysView != null) {
            managePrioDaysView.setEnabled(false);
            managePrioDaysView.setBackgroundResource(R.color.categoryColorDisabled);
        }

        if (managePrioPeriodView != null) {
            managePrioPeriodView.setEnabled(false);
            managePrioPeriodView.setBackgroundResource(R.color.categoryColorDisabled);
        }

        if (manageExpiPeriodView != null) {
            manageExpiPeriodView.setEnabled(false);
            manageExpiPeriodView.setBackgroundResource(R.color.categoryColorDisabled);
        }
    }

    public void updateDescriptionStatusOfPriorityPeriod(Calendar time) {
        if (managePrioPeriodView != null) {
            ((TextView) managePrioPeriodView.findViewById(R.id.auto_values_config_main_subText)).setText(
                    getTimeDescription("Current value: ", time));
            autoPrioPeriod = time;
        }
    }

    public void updateDescriptionStatusOfPriorityDays(DaysOfWeek[] daysOfWeeks) {
        if (managePrioDaysView != null) {
            ((TextView) managePrioDaysView.findViewById(R.id.auto_values_config_main_subText)).setText(
                   getDaysOfWeekDescription("Current value: ", daysOfWeeks));
            autoDaysOfWeek = daysOfWeeks;
        }
    }

    public void updateDescriptionStatusOfExpirationPeriod(Calendar time) {
        if (manageExpiPeriodView != null) {
            ((TextView) manageExpiPeriodView.findViewById(R.id.auto_values_config_main_subText)).setText(
                    getTimeDescription("Current value: ", time));
            autoExpiPeriod = time;
        }
    }

    //

    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        autoFillEnabled = isChecked;
        if (isChecked) {
            enableManageConfigurationsButtons();
            listener.onAutoFillEnabled();
        } else {
            disableManageConfigurationsButtons();
            listener.onAutoFillDisabled();
        }
    }

    //

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return 5;
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

    @Override
    public View getView(int pos, @Nullable View convertView, ViewGroup parent) {
        if (pos == 0) {
            return getHeader(convertView, parent);
        } else if (pos == 1) {
            return getInitializedEnableButtonView();
        } else if (pos == 2) {
            return getInitializedPriorityPeriodView();
        } else if (pos == 3) {
            return getInitializedPriorityDaysView();
        } else if (pos == 4) {
            return getInitializedExpirationPeriodView();
        }
        return null; //should not reach here
    }

    private View getInitializedEnableButtonView() {
        enableButtonView = getEnableAutoFillButtonItem(enableButtonView, null);
        ((Switch) enableButtonView.findViewById(R.id.auto_values_config_enable_button)).setOnCheckedChangeListener(this);

        ((Switch) enableButtonView.findViewById(R.id.auto_values_config_enable_button)).setChecked(autoFillEnabled);

        return enableButtonView;
    }

    private View getInitializedPriorityPeriodView() {
        managePrioPeriodView = getTextAndDescriptionViewOfPeriod(managePrioPeriodView, null, "Configure AutoFill Priority Period",
                "Current value: ", autoPrioPeriod);

        managePrioPeriodView.setEnabled(autoFillEnabled);
        if (autoFillEnabled) {
            managePrioPeriodView.setBackgroundResource(R.color.categoryColorEnabled);
        } else {
            managePrioPeriodView.setBackgroundResource(R.color.categoryColorDisabled);
        }

        return managePrioPeriodView;
    }

    private View getInitializedPriorityDaysView() {
        managePrioDaysView = getTextAndDescriptionViewOfDaysOfWeek(managePrioDaysView, null, "Configure AutoFill Priority Days",
                "Current value: ", autoDaysOfWeek);

        managePrioDaysView.setEnabled(autoFillEnabled);
        if (autoFillEnabled) {
            managePrioDaysView.setBackgroundResource(R.color.categoryColorEnabled);
        } else {
            managePrioDaysView.setBackgroundResource(R.color.categoryColorDisabled);
        }

        return managePrioDaysView;
    }

    private View getInitializedExpirationPeriodView() {
        manageExpiPeriodView = getTextAndDescriptionViewOfPeriod(manageExpiPeriodView, null, "Configure AutoFill Expiration Period",
                "Current value: ", autoExpiPeriod);

        manageExpiPeriodView.setEnabled(autoFillEnabled);
        if (autoFillEnabled) {
            manageExpiPeriodView.setBackgroundResource(R.color.categoryColorEnabled);
        } else {
            manageExpiPeriodView.setBackgroundResource(R.color.categoryColorDisabled);
        }

        return manageExpiPeriodView;
    }

    private View getHeader(@Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.auto_values_configuration_header, null);
        }
        return convertView;
    }

    private View getEnableAutoFillButtonItem(@Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.auto_values_configuration_enable, null);
        }

        ((Switch) convertView.findViewById(R.id.auto_values_config_enable_button)).setChecked(autoFillEnabled);

        return convertView;
    }

    private View getSeparator(@Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.auto_values_configuration_separator, null);
        }
        return convertView;
    }

    private View getTextAndDescriptionViewOfPeriod(@Nullable View convertView, ViewGroup parent,
                                                   String text, String descriptor, Calendar time) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.auto_values_configuration_text_and_description, null);
        }

        ((TextView) convertView.findViewById(R.id.auto_values_config_main_text)).setText(text);
        ((TextView) convertView.findViewById(R.id.auto_values_config_main_subText)).setText(getTimeDescription(descriptor, time));

        return convertView;
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

    private View getTextAndDescriptionViewOfDaysOfWeek(@Nullable View convertView, ViewGroup parent, String text, String descriptor,
                                                       DaysOfWeek[] daysOfWeeks) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.auto_values_configuration_text_and_description, null);
        }

        ((TextView) convertView.findViewById(R.id.auto_values_config_main_text)).setText(text);
        ((TextView) convertView.findViewById(R.id.auto_values_config_main_subText)).setText(getDaysOfWeekDescription(descriptor, daysOfWeeks));

        return convertView;
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

    @Override
    public int getItemViewType(int pos) {
        if (pos == 0) {
            return HEADER;
        } else if (pos == 1) {
            return ENABLE;
        } else {
            return MANAGE_ITEM_TYPE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}

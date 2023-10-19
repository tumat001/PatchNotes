package com.example.patchnotes;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;
import com.example.patchnotes.database.AppDatabase;
import com.example.patchnotes.dialogs.InputDayOfWeekDialog;
import com.example.patchnotes.dialogs.InputDayPickerDialog;
import com.example.patchnotes.dialogs.InputTimePickerDialog;
import com.example.patchnotes.dialogs.InputYesNoDialog;
import com.example.patchnotes.dialogs.manage_autovalue.RemoveOrEditManageDialog;
import com.example.patchnotes.fragments.AutoValuesConfigurationFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CategoryManagerActivity extends AbstractContentAttributeSetterActivity implements
        AutoValuesConfigurationFragment.OnConfigurationValuePressedListener, InputDayPickerDialog.OnDayPickedListener,
        InputTimePickerDialog.OnTimePickedListener, InputDayOfWeekDialog.OnDayOfWeekInputChosenListener,
        RemoveOrEditManageDialog.OnOptionSelectedListener, InputYesNoDialog.OnResponseListener {

    static final String INTENT_CATEGORY_TO_MANAGE = "intentCategoryToManage";

    private static final String SAVED_CATEGORY_TO_MANAGE = "savedCategoryToManage";
    private static final String SAVED_YEAR = "savedYear";
    private static final String SAVED_MONTH = "savedMonth";
    private static final String SAVED_DAY = "savedDay";
    private static final String SAVED_CATEGORY_ID = "savedCategoryId";

    private static final String TAG_AUTO_PRIORITY_PERIOD_PROMPT = "tagAutoPriorityPeriodPrompt";
    private static final String TAG_AUTO_PRIORITY_DAY_PROMPT = "tagAutoPriorityDayPrompt";
    private static final String TAG_AUTO_EXPIRATION_PERIOD_PROMPT = "tagAutoExpirationPeriodPrompt";

    private DrawerLayout drawerLayout;
    private AutoValuesConfigurationFragment fragment;
    private EditText displayNameView, descriptionView;

    private boolean isDatabaseUpdated;

    private Category.Builder categoryBuilderToManage;
    private Category constructedCategoryFromDatabase;

    @NonNull
    @Override
    protected Content getContent() {
        return categoryBuilderToManage;
    }

    @Override
    protected int getNavigationViewId() {
        return R.id.catManager_navRight;
    }
    @Override
    protected int getDrawerViewId() {
        return R.id.catManager_drawer;
    }

    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        restoreInputYMD(savedInstanceState);

        initializeToolbar();
        initializeDrawerView();

        initializeCategoryToManage(savedInstanceState);

        initializeAutoValuesConfigurationFragment(savedInstanceState);
        initializeDisplayNameView(savedInstanceState);
        initializeDescriptionView();
    }

    private void restoreInputYMD(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            year = savedInstanceState.getInt(SAVED_YEAR);
            month = savedInstanceState.getInt(SAVED_MONTH);
            dayOfMonth = savedInstanceState.getInt(SAVED_DAY);
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = findViewById(R.id.catManager_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.category_manager_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.catManager_toolbarMenuId_showSelfAttr) {
            openDrawer(true);
            return true;
        }
        return false;
    }

    private void initializeDrawerView() {
        drawerLayout = findViewById(R.id.catManager_drawer);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (displayNameView.hasFocus()) {
                    hideSoftKeyboard(displayNameView);
                } else if (descriptionView.hasFocus()) {
                    hideSoftKeyboard(descriptionView);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    protected void onNewPriorityPeriodChosen(Calendar time) {
        categoryBuilderToManage.setSelfPriorityPeriod(time);
        super.onNewPriorityPeriodChosen(time);
    }

    @Override
    protected void onNewPriorityDaysChosen(DaysOfWeek[] daysOfWeeks) {
        categoryBuilderToManage.setSelfPriorityDaysOfWeek(daysOfWeeks);
        super.onNewPriorityDaysChosen(daysOfWeeks);
    }

    @Override
    protected void onNewExpirationPeriodChosen(Calendar time) {
        categoryBuilderToManage.setSelfDeletionPeriod(time);
        super.onNewExpirationPeriodChosen(time);
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initializeCategoryToManage(Bundle savedInstanceState) {
        Category categoryToManage;
        if (savedInstanceState == null) {
            categoryToManage = (Category) getIntent().getSerializableExtra(INTENT_CATEGORY_TO_MANAGE);
        } else {
            String uniqueId = savedInstanceState.getString(SAVED_CATEGORY_ID);

            if (uniqueId != null) {
                categoryToManage = new AppDatabase(this).getCategoryDatabase().getCategoryByUniqueId(uniqueId);
            } else {
                categoryToManage = new Category.Builder("").constructCategory();
            }
        }

        if (categoryToManage != null) {
            categoryBuilderToManage = categoryToManage.getBuilderWithValuesOfThis();
        } else {
            categoryBuilderToManage = new Category.Builder("");
        }
    }

    private void initializeAutoValuesConfigurationFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            fragment = new AutoValuesConfigurationFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.catManager_autoFillListContainer, fragment, "AutoValsFrag");
            ft.commit();
        } else {
            fragment = (AutoValuesConfigurationFragment) getSupportFragmentManager().findFragmentByTag("AutoValsFrag");
        }

        fragment.setConfigurationListEnabled(categoryBuilderToManage.getAutoFillEnable());
        fragment.setPriorityPeriod(categoryBuilderToManage.getAutoFillPriorityPeriod());
        fragment.setPriorityDays(categoryBuilderToManage.getAutoFillPriorityDaysOfWeek());
        fragment.setExpirationPeriod(categoryBuilderToManage.getAutoFillExpirationPeriod());
    }

    private void initializeDisplayNameView(Bundle savedInstanceState) {
        displayNameView = findViewById(R.id.catManager_displayName);
        displayNameView.setText(categoryBuilderToManage.getDisplayName());

        if (savedInstanceState == null) {
            displayNameView.requestFocus();
            hideSoftKeyboard(displayNameView);
        }
    }

    private void initializeDescriptionView() {
        descriptionView = findViewById(R.id.catManager_description);
        descriptionView.setText(categoryBuilderToManage.getDescription());
    }

    //

    @Override
    public void onConfigurePriorityPeriodPressed(Calendar priorityPeriod) {
        if (categoryBuilderToManage.getAutoFillPriorityPeriod() == null) {
            showInputDayPickerWithValuesAndTag(GregorianCalendar.getInstance(), TAG_AUTO_PRIORITY_PERIOD_PROMPT);
        } else {
            RemoveOrEditManageDialog dialog = new RemoveOrEditManageDialog();
            dialog.setSavedToManageText("Priority Period");
            dialog.setTitle("Manage Priority Period");
            dialog.setTag(TAG_AUTO_PRIORITY_PERIOD_PROMPT);
            dialog.show(getSupportFragmentManager(), "managePrioPeriod");
        }
    }

    private void showInputDayPickerWithValuesAndTag(Calendar time, String tag) {
        InputDayPickerDialog dialog = new InputDayPickerDialog();
        dialog.setInitialDayValues(time);
        dialog.setTag(tag);
        dialog.show(getSupportFragmentManager(), "DayPickerDialog");
    }

    @Override
    public void onConfigurePriorityDaysPressed(DaysOfWeek[] daysOfWeeks) {
        if (categoryBuilderToManage.getAutoFillPriorityDaysOfWeek() == null) {
            showInputDaysOfWeekDialogWithValues(daysOfWeeks, TAG_AUTO_PRIORITY_DAY_PROMPT);
        } else {
            RemoveOrEditManageDialog dialog = new RemoveOrEditManageDialog();
            dialog.setSavedToManageText("Priority Days");
            dialog.setTitle("Manage Priority Days");
            dialog.setTag(TAG_AUTO_PRIORITY_DAY_PROMPT);
            dialog.show(getSupportFragmentManager(), "manageDays");
        }
    }

    private void showInputDaysOfWeekDialogWithValues(DaysOfWeek[] daysOfWeeks, String tag) {
        InputDayOfWeekDialog dialog = new InputDayOfWeekDialog();
        dialog.setInitialSelection(daysOfWeeks);
        dialog.setTag(tag);
        dialog.show(getSupportFragmentManager(), "DaysOfWeekPickerDialog");
    }

    @Override
    public void onConfigureExpirationPeriodPressed(Calendar expirationPeriod) {
        if (categoryBuilderToManage.getAutoFillExpirationPeriod() == null) {
            showInputDayPickerWithValuesAndTag(GregorianCalendar.getInstance(), TAG_AUTO_EXPIRATION_PERIOD_PROMPT);
        } else {
            RemoveOrEditManageDialog dialog = new RemoveOrEditManageDialog();
            dialog.setSavedToManageText("Expiration Period");
            dialog.setTitle("Manage Expiration Period");
            dialog.setTag(TAG_AUTO_EXPIRATION_PERIOD_PROMPT);
            dialog.show(getSupportFragmentManager(), "manageExpiPeriod");
        }
    }

    @Override
    public void onAutoFillStatusChanged(boolean autoFillEnabled) {
        categoryBuilderToManage.setAutoFillEnabled(autoFillEnabled);
    }

    //

    private int year, month, dayOfMonth;

    @Override
    public void onDayPicked(int year, int month, int dayOfMonth, String tag) {
        if (tag.equals(TAG_AUTO_PRIORITY_PERIOD_PROMPT) || tag.equals(TAG_AUTO_EXPIRATION_PERIOD_PROMPT)) {
            InputTimePickerDialog dialog = new InputTimePickerDialog();
            if (tag.equals(TAG_AUTO_PRIORITY_PERIOD_PROMPT)) {
                dialog.setInitialTime(categoryBuilderToManage.getAutoFillPriorityPeriod());
            } else {
                dialog.setInitialTime(categoryBuilderToManage.getAutoFillExpirationPeriod());
            }

            this.year = year;
            this.month = month;
            this.dayOfMonth = dayOfMonth;

            dialog.setTag(tag);
            dialog.show(getSupportFragmentManager(), "dialogTimePick");
        } else {
            super.onDayPicked(year, month, dayOfMonth, tag);
        }
    }

    @Override
    public void onTimePicked(int hour, int minute, String tag) {
        if (tag.equals(TAG_AUTO_PRIORITY_PERIOD_PROMPT) || tag.equals(TAG_AUTO_EXPIRATION_PERIOD_PROMPT)) {
            Calendar time = getSettedCalendar(GregorianCalendar.getInstance(), hour, minute);

            if (tag.equals(TAG_AUTO_PRIORITY_PERIOD_PROMPT)) {
                categoryBuilderToManage.setAutoFillPriorityPeriod(time);
                fragment.setPriorityPeriod(time);
            } else {
                categoryBuilderToManage.setAutoFillExpirationPeriod(time);
                fragment.setExpirationPeriod(time);
            }
        } else {
            super.onTimePicked(hour, minute, tag);
        }
    }

    private Calendar getSettedCalendar(Calendar time, int hour, int minute) {
        time.set(Calendar.YEAR, year);
        time.set(Calendar.MONTH, month);
        time.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        time.set(Calendar.HOUR_OF_DAY, hour);
        time.set(Calendar.MINUTE, minute);
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);

        return time;
    }

    @Override
    public void onDaysOfWeekInputChosen(DaysOfWeek[] daysOfWeeks, String tag) {
        if (tag.equals(TAG_AUTO_PRIORITY_DAY_PROMPT)) {
            categoryBuilderToManage.setAutoFillPriorityDaysOfWeek(daysOfWeeks);
            fragment.setPriorityDays(daysOfWeeks);
        } else {
            super.onDaysOfWeekInputChosen(daysOfWeeks, tag);
        }
    }

    @Override
    public void onEditSelected(String tag) {
        if (tag.equals(TAG_AUTO_PRIORITY_PERIOD_PROMPT)) {
            showInputDayPickerWithValuesAndTag(categoryBuilderToManage.getAutoFillPriorityPeriod(), tag);
        } else if (tag.equals(TAG_AUTO_PRIORITY_DAY_PROMPT)) {
            showInputDaysOfWeekDialogWithValues(categoryBuilderToManage.getAutoFillPriorityDaysOfWeek(), tag);
        } else if (tag.equals(TAG_AUTO_EXPIRATION_PERIOD_PROMPT)) {
            showInputDayPickerWithValuesAndTag(categoryBuilderToManage.getAutoFillExpirationPeriod(), tag);
        }
    }

    @Override
    public void onRemoveSelected(String tag) {
        InputYesNoDialog dialog = new InputYesNoDialog();

        if (tag.equals(TAG_AUTO_PRIORITY_PERIOD_PROMPT)) {
            dialog.setText("Remove autoFill Priority Period?");
        } else if (tag.equals(TAG_AUTO_PRIORITY_DAY_PROMPT)) {
            dialog.setText("Remove autoFill Priority Days?");
        } else if (tag.equals(TAG_AUTO_EXPIRATION_PERIOD_PROMPT)) {
            dialog.setText("Remove autoFill Expiration Period");
        }
        dialog.setTag(tag);

        dialog.show(getSupportFragmentManager(), "yesNoRemoveConfirmation");
    }

    @Override
    public void onYesSelected(String tag) {
        if (tag.equals(TAG_AUTO_PRIORITY_PERIOD_PROMPT)) {
            categoryBuilderToManage.setAutoFillPriorityPeriod(null);
            fragment.setPriorityPeriod(null);
        } else if (tag.equals(TAG_AUTO_PRIORITY_DAY_PROMPT)) {
            categoryBuilderToManage.setAutoFillPriorityDaysOfWeek(null);
            fragment.setPriorityDays(null);
        } else if (tag.equals(TAG_AUTO_EXPIRATION_PERIOD_PROMPT)) {
            categoryBuilderToManage.setAutoFillExpirationPeriod(null);
            fragment.setExpirationPeriod(null);
        } else {
            super.onYesSelected(tag);
        }
    }

    @Override
    public void onNoSelected(String tag) {
        //do nothing
    }

    //

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        super.onSaveInstanceState(toSave);

        Category categoryConstructedFromDatabase = updateDatabaseOnce();

        toSave.putSerializable(SAVED_CATEGORY_TO_MANAGE, categoryConstructedFromDatabase);
        toSave.putInt(SAVED_YEAR, year);
        toSave.putInt(SAVED_MONTH, month);
        toSave.putInt(SAVED_DAY, dayOfMonth);
        toSave.putString(SAVED_CATEGORY_ID, categoryConstructedFromDatabase.getUniqueId());
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            Intent intent = new Intent(this, CategoryListDisplayActivity.class);
            updateDatabaseOnce();
            startActivity(intent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        updateDatabaseOnce();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        isDatabaseUpdated = false;
    }

    private Category updateDatabaseOnce() {
        if (!isDatabaseUpdated) {
            AppDatabase database = new AppDatabase(this);

            setUpFieldsOfBuilder();

            if (categoryBuilderToManage.getUniqueId() == null) {
                if (!categoryBuilderToManage.isEmpty()) {
                    constructedCategoryFromDatabase = database.getCategoryDatabase().addCategory(categoryBuilderToManage);
                } else {
                    constructedCategoryFromDatabase = categoryBuilderToManage.constructCategory();
                }
            } else {
                database.getCategoryDatabase().updateCategory(categoryBuilderToManage.getUniqueId(), categoryBuilderToManage);
                constructedCategoryFromDatabase = categoryBuilderToManage.constructCategory();
            }

            isDatabaseUpdated = true;
        }
        return constructedCategoryFromDatabase;
    }

    private void setUpFieldsOfBuilder() {
        categoryBuilderToManage.setDisplayName(displayNameView.getText().toString());
        categoryBuilderToManage.setDescription(descriptionView.getText().toString());
    }

    @Override
    protected MenuItem[] getBottomPartMenu() {
        return null;
    }
    @Override
    protected MenuItem[] getTopPartMenu() {
        return null;
    }
}

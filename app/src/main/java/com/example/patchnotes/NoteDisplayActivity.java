package com.example.patchnotes;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.database.AppDatabase;
import com.example.patchnotes.dialogs.InputDayOfWeekDialog;
import com.example.patchnotes.dialogs.InputDayPickerDialog;
import com.example.patchnotes.dialogs.InputTimePickerDialog;
import com.example.patchnotes.dialogs.InputYesNoDialog;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;
import com.example.patchnotes.contentrelated.Note;
import com.example.patchnotes.database.NoteSourceWriter;
import com.example.patchnotes.dialogs.ShowInfoDialog;
import com.example.patchnotes.dialogs.categoryrelated.CategorySpinnerDialog;
import com.example.patchnotes.dialogs.categoryrelated.SelectCategoryAutoFillAttributesDialog;
import com.example.patchnotes.dialogs.manage_autovalue.RemoveOrEditManageDialog;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class NoteDisplayActivity extends AppCompatActivity implements InputDayOfWeekDialog.OnDayOfWeekInputChosenListener,
        DayTimePeriodPrompter.OnDayTimePeriodReceivedListener, InputYesNoDialog.OnResponseListener,
        CategorySpinnerDialog.OnCategoryChosenListener, SelectCategoryAutoFillAttributesDialog.OnCategoryAutoFillSelectionConfirmedListener,
        RemoveOrEditManageDialog.OnOptionSelectedListener {

    static final String INTENT_NOTE_TO_DISPLAY = "intentNoteToDisplay";

    private static final String SAVED_CURRENT_NOTE_BUILDER = "savedCurrentNoteBuilder";
    private static final String SAVED_ORIGINAL_NOTE_ID = "savedOriginalNoteId";
    private static final String SAVED_TITLE_EDIT_TEXT_CARAT_POS = "savedTitleEditTextCaratPos";
    private static final String SAVED_CONTENT_EDIT_TEXT_CARAT_POS = "savedContentEditTextCaratPos";
    private static final String SAVED_PROMPTER = "savedPrompter";

    private static final String TAG_PRIORITY_PERIOD = "tagPriorityPeriod";
    private static final String TAG_EXPIRATION_PERIOD = "tagExpirationPeriod";
    private static final String TAG_YES_NO_DIALOG = "tagYesNo";
    private static final String TAG_REMOVE_PRIORITY_PERIOD = "tagRemovePriorityPeriod";
    private static final String TAG_REMOVE_PRIORITY_DAY = "tagRemovePriorityDay";
    private static final String TAG_REMOVE_EXPIRATION_PERIOD = "tagRemoveExpirationDay";
    private static final String TAG_YESNO_REMOVE_CATEGORY = "tagRemoveCategory";
    private static final String TAG_REMOVEEDIT_CATEGORY = "tagRemoveEditCategory";

    private EditText titleEditText, contentEditText;
    private NavigationView noteSettingsNavView;
    private DrawerLayout drawerLayout;

    private Note originalNote;
    private String originalNoteId;
    private Note.Builder currentNote;

    private NoteSourceWriter noteSourceWriter;
    private AppDatabase appDatabase;

    private DayTimePeriodPrompter prompter;

    private boolean databaseAlreadyUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_display);
        setSupportActionBar(findViewById(R.id.noteDisplay_toolbar));

        if (savedInstanceState != null) {
            prompter = (DayTimePeriodPrompter) savedInstanceState.getSerializable(SAVED_PROMPTER);
            if (prompter != null) {
                prompter.setFragmentManager(getSupportFragmentManager());
                prompter.setListener(this);
            }
        }

        initializeDrawerAndNavigation();
        initializeNoteSourceWriter();
        initializeOriginalNoteAndNoteId(savedInstanceState);
        initializeCurrentNote(savedInstanceState);
        initializeTitleEditText(savedInstanceState);
        initializeContentEditText(savedInstanceState);
        initializeNavigationBehavior();
    }

    private void initializeDrawerAndNavigation() {
        drawerLayout = findViewById(R.id.noteDisplay_drawerLayout);
        noteSettingsNavView = findViewById(R.id.noteDisplay_navView);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (contentEditText.hasFocus()) {
                    hideSoftKeyboard(contentEditText);
                } else if (titleEditText.hasFocus()) {
                    hideSoftKeyboard(titleEditText);
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

    private void hideSoftKeyboard(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void initializeNoteSourceWriter() {
        appDatabase = new AppDatabase(this);
        noteSourceWriter = appDatabase.getMainNoteDatabase();
    }

    private void initializeOriginalNoteAndNoteId(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Note noteToDisplay = getNoteFromMain();

            setUpNote(noteToDisplay);
        } else {
            originalNote = null;
            originalNoteId = savedInstanceState.getString(SAVED_ORIGINAL_NOTE_ID);
        }
    }

    private Note getNoteFromMain() {
        return (Note) getIntent().getSerializableExtra(INTENT_NOTE_TO_DISPLAY);
    }

    private void setUpNote(Note originalNote) {
        if (originalNote != null && originalNote.getIncId() != null) {
            this.originalNote = originalNote;
            originalNoteId = originalNote.getIncId();
        } else {
            originalNoteId = null;
        }
    }

    private void initializeCurrentNote(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            currentNote = new Note.Builder();

            if (originalNote != null) {
                currentNote.setText(originalNote.getText());
                currentNote.setTitle(originalNote.getTitle());
                currentNote.setPriorityPeriod(originalNote.getPriorityPeriod());
                currentNote.setPriorityDaysOfWeek(originalNote.getPriorityDaysOfWeek());
                currentNote.setExpirationPeriod(originalNote.getExpirationPeriod());
                currentNote.setCategoryUniqueId(originalNote.getCategoryUniqueId());
            } else {
                if (getTextFromImplicitIntent() != null) {
                    currentNote.setText(getTextFromImplicitIntent());
                }
                if (getTitleFromImplicitIntent() != null) {
                    currentNote.setTitle(getTitleFromImplicitIntent());
                }
            }
        } else {
            currentNote = (Note.Builder) savedInstanceState.getSerializable(SAVED_CURRENT_NOTE_BUILDER);
        }
    }

    private String getTextFromImplicitIntent() {
        return getIntent().getStringExtra(Intent.EXTRA_TEXT);
    }

    private String getTitleFromImplicitIntent() {
        return getIntent().getStringExtra(Intent.EXTRA_TITLE);
    }

    private void initializeTitleEditText(Bundle savedInstanceState) {
        titleEditText = findViewById(R.id.noteDisplay_titleEdit);
        titleEditText.setText(currentNote.getTitle());
        // todo try to figure out how to set background color while retaining width/height
        titleEditText.setHint("Title");
        if (savedInstanceState != null) {
            titleEditText.setSelection(savedInstanceState.getInt(SAVED_TITLE_EDIT_TEXT_CARAT_POS));
        }

        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentNote.setTitle(s.toString());
            }
        });
    }

    private void initializeContentEditText(Bundle savedInstanceState) {
        contentEditText = findViewById(R.id.noteDisplay_contentEdit);
        contentEditText.setText(currentNote.getText());
        contentEditText.requestFocus();
        contentEditText.setSelection(0);
        if (savedInstanceState != null) {
            contentEditText.setSelection(savedInstanceState.getInt(SAVED_CONTENT_EDIT_TEXT_CARAT_POS));
        }
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentNote.setText(s.toString());
            }
        });
    }

    private void initializeNavigationBehavior() {
        updateNavItemPriorityPeriodStatus();
        updateNavItemPriorityDayStatus();
        updateNavItemExpirationPeriodStatus();

        initializeBehaviorOfManageCategoryItem();
        initializeBehaviorOfViewCategoryItem();
    }

    private void updateNavItemPriorityPeriodStatus() {
        MenuItem togglePrioPeriod = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_priorityPeriod_toggleAddRemove);
        MenuItem editPrioPeriod = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_priorityPeriod_edit);

        if (currentNote.getPriorityPeriod() == null) {
            togglePrioPeriod.setTitle("Add Priority Period");
            togglePrioPeriod.setOnMenuItemClickListener(item -> {
                promptDayAndTimePeriod(GregorianCalendar.getInstance(), TAG_PRIORITY_PERIOD);
                return true;
            });
            editPrioPeriod.setEnabled(false);
        } else {
            togglePrioPeriod.setTitle("Remove Priority Period");
            togglePrioPeriod.setOnMenuItemClickListener(item -> {
                promptYesNoDialog("", "Are you sure you want to delete this note's priority period?", TAG_REMOVE_PRIORITY_PERIOD);
                return true;
            });
            editPrioPeriod.setEnabled(true);
        }

        editPrioPeriod.setOnMenuItemClickListener(item -> {
            promptDayAndTimePeriod(currentNote.getPriorityPeriod(), TAG_PRIORITY_PERIOD);
            return true;
        });
    }

    private void promptDayAndTimePeriod(Calendar initialTimeValues, String tag) {
        prompter = new DayTimePeriodPrompter(getSupportFragmentManager()
                , initialTimeValues, this, tag);
        prompter.promptDayAndTimeValues();
    }

    private void promptYesNoDialog(String title, String message, String tag) {
        InputYesNoDialog dialog = new InputYesNoDialog();
        dialog.setTitle(title);
        dialog.setText(message);
        dialog.setTag(tag);
        dialog.show(getSupportFragmentManager(), TAG_YES_NO_DIALOG);
    }

    private void updateNavItemPriorityDayStatus() {
        MenuItem togglePrioDay = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_priorityDay_toggleAddRemove);
        MenuItem editPrioDay = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_priorityDay_edit);

        if (currentNote.getPriorityDaysOfWeek() == null) {
            togglePrioDay.setTitle("Add Priority Days");
            togglePrioDay.setOnMenuItemClickListener(item -> {
                promptDayOfWeekValue(null);
                return true;
            });
            editPrioDay.setEnabled(false);
        } else {
            togglePrioDay.setTitle("Remove Priority Days");
            togglePrioDay.setOnMenuItemClickListener(item -> {
                promptYesNoDialog("", "Are you sure you want to remove this note's priority days?", TAG_REMOVE_PRIORITY_DAY);
                return true;
            });
            editPrioDay.setEnabled(true);
        }

        editPrioDay.setOnMenuItemClickListener(item -> {
            promptDayOfWeekValue(currentNote.getPriorityDaysOfWeek());
            return true;
        });
    }

    private void promptDayOfWeekValue(DaysOfWeek[] initialSelected) {
       InputDayOfWeekDialog dialog = new InputDayOfWeekDialog();
       dialog.setInitialSelection(initialSelected);
       dialog.show(getSupportFragmentManager(), "promptDayOfWeekValue");
    }

    @Override
    public void onDaysOfWeekInputChosen(DaysOfWeek[] daysOfWeeks, String tag) {
        if (daysOfWeeks.length != 0) {
            currentNote.setPriorityDaysOfWeek(daysOfWeeks);
        } else {
            currentNote.setPriorityDaysOfWeek(null);
        }
        updateNavItemPriorityDayStatus();
    }

    private void updateNavItemExpirationPeriodStatus() {
        MenuItem toggleExpiPeriod = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_expirationPeriod_toggleAddRemove);
        MenuItem editExpiPeriod = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_expirationPeriod_edit);

        if (currentNote.getExpirationPeriod() == null) {
            toggleExpiPeriod.setTitle("Add Expiration Period");
            toggleExpiPeriod.setOnMenuItemClickListener(item -> {
                promptDayAndTimePeriod(GregorianCalendar.getInstance(), TAG_EXPIRATION_PERIOD);
                return true;
            });
            editExpiPeriod.setEnabled(false);
        } else {
            toggleExpiPeriod.setTitle("Remove Expiration Period");
            toggleExpiPeriod.setOnMenuItemClickListener(item -> {
                promptYesNoDialog("", "Are you sure you want to remove this note's expiration period?", TAG_REMOVE_EXPIRATION_PERIOD);
                return true;
            });
            editExpiPeriod.setEnabled(true);
        }

        editExpiPeriod.setOnMenuItemClickListener(item -> {
            promptDayAndTimePeriod(currentNote.getExpirationPeriod(), TAG_EXPIRATION_PERIOD);
            return true;
        });
    }

    @Override
    public void onDayTimePeriodReceived(Calendar dayAndTimePeriod, String tag) {
        if (dayAndTimePeriod != null) {
            if (tag.equals(TAG_PRIORITY_PERIOD)) {
                currentNote.setPriorityPeriod(dayAndTimePeriod);
                updateNavItemPriorityPeriodStatus();
            } else if (tag.equals(TAG_EXPIRATION_PERIOD)) {
                currentNote.setExpirationPeriod(dayAndTimePeriod);
                updateNavItemExpirationPeriodStatus();
            }
        }
        prompter = null;
    }

    private void initializeBehaviorOfManageCategoryItem() {
        MenuItem manageCatItem = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_manageCategory);
        manageCatItem.setOnMenuItemClickListener(item -> {
            Category currentCategory = appDatabase.getCategoryDatabase().getCategoryByUniqueId(currentNote.getCategoryUniqueId());

            if (currentCategory == null) {
                showSelectCategoryIfCategoryDatabaseIsNotEmpty();
            } else {
                showManageDialog();
            }
            return true;
        });
    }

    private void initializeBehaviorOfViewCategoryItem() {
        MenuItem viewCatItem = noteSettingsNavView.getMenu().findItem(R.id.noteDisplay_nav_viewCategory);
        viewCatItem.setOnMenuItemClickListener(item -> {
            Category currentCategory = appDatabase.getCategoryDatabase().getCategoryByUniqueId(currentNote.getCategoryUniqueId());
            String toDisplay;

            if (currentCategory == null) {
                toDisplay = "Note is currently under no category";
            } else {
                toDisplay = "Note's current category is \"" + currentCategory.getDisplayName() + "\"";
            }

            ShowInfoDialog dialog = new ShowInfoDialog();
            dialog.setContent(toDisplay);
            dialog.show(getSupportFragmentManager(), "ShowInfoCurrNote");

            return true;
        });
    }

    private void showSelectCategoryIfCategoryDatabaseIsNotEmpty() {
        Category[] allCategory = appDatabase.getCategoryDatabase().getAllCategories();

        if (allCategory.length != 0) {
            showSelectCategory(allCategory);
        } else {
            Toast.makeText(this, "There are no categories to select from", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSelectCategory(Category[] toDisplay) {
        CategorySpinnerDialog dialog = new CategorySpinnerDialog();
        dialog.setCategories(toDisplay);
        dialog.setTitle("Set this note's category");
        dialog.show(getSupportFragmentManager(), "categorySpinDialog");
    }

    @Override
    public void onCategoryChosen(Category category, String tag) {
        if (category.isAutoFillEnabled()) {
            showSelectAutoFillConfiguration(category, tag);
        } else {
            setCurrentNoteCategoryTo(category);
        }
    }

    private void showSelectAutoFillConfiguration(Category category, String tag) {
        SelectCategoryAutoFillAttributesDialog dialog = new SelectCategoryAutoFillAttributesDialog();
        dialog.setCategory(category);
        dialog.setContent(currentNote.constructNote());
        dialog.setTag(tag);
        dialog.setTitle("Configure Attributes");
        dialog.setMessage("Category to inherit has auto fill values. Please configure which ones should be inherited.");
        dialog.show(getSupportFragmentManager(), "selectAutoFill");
    }

    private void setCurrentNoteCategoryTo(@Nullable Category category) {
        if (category != null) {
            currentNote.setCategoryUniqueId(category.getUniqueId());
            Toast.makeText(this, "Category set to " + category.getDisplayName(), Toast.LENGTH_SHORT).show();
        } else {
            currentNote.setCategoryUniqueId(null);
            Toast.makeText(this, "Removed category of this note", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCategoryAutoFillSelectionConfirmed(boolean inheritPrioPeriod, boolean inheritPrioDays,
                                                     boolean inheritExpiPeriod, Category category, String tag) {
        if (inheritPrioPeriod) {
            currentNote.setPriorityPeriod(category.getAutoFillPriorityPeriod());
            updateNavItemPriorityPeriodStatus();
        }
        if (inheritPrioDays) {
            currentNote.setPriorityDaysOfWeek(category.getAutoFillPriorityDaysOfWeek());
            updateNavItemPriorityDayStatus();
        }
        if (inheritExpiPeriod) {
            currentNote.setExpirationPeriod(category.getAutoFillExpirationPeriod());
            updateNavItemExpirationPeriodStatus();
        }

        setCurrentNoteCategoryTo(category);
    }

    //

    private void showManageDialog() {
        RemoveOrEditManageDialog dialog = new RemoveOrEditManageDialog();
        dialog.setTag(TAG_REMOVEEDIT_CATEGORY);
        dialog.setSavedToManageText("Note's Current Category");
        dialog.setTitle("Manage Category");
        dialog.show(getSupportFragmentManager(), "ManageDialog");
    }

    @Override
    public void onEditSelected(String tag) {
        if (tag.equals(TAG_REMOVEEDIT_CATEGORY)) {
            showSelectCategoryIfCategoryDatabaseIsNotEmpty();
        }
    }

    @Override
    public void onRemoveSelected(String tag) {
        if (tag.equals(TAG_REMOVEEDIT_CATEGORY)) {
            InputYesNoDialog dialog = new InputYesNoDialog();
            dialog.setTag(TAG_YESNO_REMOVE_CATEGORY);
            dialog.setText("Remove this note from current category?");
            dialog.show(getSupportFragmentManager(), "yesNoRemoveDialog");
        }
    }

    //

    @Override
    public void onYesSelected(String tag) {
        if (tag.equals(TAG_REMOVE_PRIORITY_PERIOD)) {
            currentNote.setPriorityPeriod(null);
            updateNavItemPriorityPeriodStatus();
        } else if (tag.equals(TAG_REMOVE_PRIORITY_DAY)) {
            currentNote.setPriorityDaysOfWeek(null);
            updateNavItemPriorityDayStatus();
        } else if (tag.equals(TAG_REMOVE_EXPIRATION_PERIOD)) {
            currentNote.setExpirationPeriod(null);
            updateNavItemExpirationPeriodStatus();
        } else if (tag.equals(TAG_YESNO_REMOVE_CATEGORY)) {
            currentNote.setCategoryUniqueId(null);
        }
    }

    @Override
    public void onNoSelected(String tag) {
        //ignore
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_display_toolbar_menu_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.noteDisplay_toolbar_noteSettings) {
            drawerLayout.openDrawer(GravityCompat.END);
            return true;
        } else if (item.getItemId() == R.id.noteDisplay_shareNote) {
            shareNoteContents();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareNoteContents() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, currentNote.getText());
        intent.putExtra(Intent.EXTRA_TITLE, currentNote.getTitle());
        intent.putExtra(Intent.EXTRA_SUBJECT, currentNote.getTitle());
        intent = Intent.createChooser(intent, "Share title and text to");
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        super.onSaveInstanceState(toSave);
        updateDatabaseOnlyOnce();

        toSave.putSerializable(SAVED_ORIGINAL_NOTE_ID, originalNoteId);
        toSave.putSerializable(SAVED_CURRENT_NOTE_BUILDER, currentNote);
        toSave.putInt(SAVED_CONTENT_EDIT_TEXT_CARAT_POS, contentEditText.getSelectionStart());
        toSave.putInt(SAVED_TITLE_EDIT_TEXT_CARAT_POS, titleEditText.getSelectionStart());
        toSave.putSerializable(SAVED_PROMPTER, prompter);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        databaseAlreadyUpdated = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateDatabaseOnlyOnce();
        prompter = null;
    }

    private void updateDatabaseOnlyOnce() {
        if (!databaseAlreadyUpdated) {
            Note currentNoteCreated = currentNote.constructNote();

            if (originalNoteId == null) {
                if (!currentNoteCreated.isEmpty()) {
                    Note noteAdded = noteSourceWriter.addNote(currentNote);
                    originalNoteId = noteAdded.getIncId();
                    currentNote.setUniqueId(originalNoteId);
                }
            } else {
                noteSourceWriter.updateNote(originalNoteId, currentNote);
            }

            databaseAlreadyUpdated = true;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            updateDatabaseOnlyOnce();
            prompter = null;
            Intent intent = new Intent(this, MainActivity.class);

            MainSettingsData data = (MainSettingsData) getIntent().getSerializableExtra(MainActivity.INTENT_MAIN_SETTINGS_DATA);
            if (data != null) {
                intent.putExtra(MainActivity.INTENT_MAIN_SETTINGS_DATA, data);
            }
            startActivity(intent);
        }
    }
}

class DayTimePeriodPrompter implements InputDayPickerDialog.OnDayPickedListener,
        InputTimePickerDialog.OnTimePickedListener, Serializable {

    interface OnDayTimePeriodReceivedListener extends Serializable {
        void onDayTimePeriodReceived(Calendar dayAndTimePeriod, String tag);
    }

    private transient FragmentManager fragmentManager;
    private Calendar initialTime;
    private int year, month, day;
    private OnDayTimePeriodReceivedListener listener;
    private String tag;

    DayTimePeriodPrompter(FragmentManager fragmentManager, Calendar initialTime, OnDayTimePeriodReceivedListener listener, String tag) {
        this.fragmentManager = fragmentManager;
        this.initialTime = initialTime;
        this.listener = listener;
        this.tag = tag;
    }

    void promptDayAndTimeValues() {
        InputDayPickerDialog dialog = new InputDayPickerDialog();
        dialog.setInitialDayValues(initialTime);
        dialog.setDayPickedListener(this);
        dialog.setTag(tag);
        dialog.show(fragmentManager, "promptDay");
    }

    @Override
    public void onDayPicked(int year, int month, int day, String tag) {
        this.year = year;
        this.month = month;
        this.day = day;
        InputTimePickerDialog dialog = new InputTimePickerDialog();
        dialog.setInitialTime(initialTime);
        dialog.setTimePickedListener(this);
        dialog.setTag(tag); //today ay do is eating
        dialog.show(fragmentManager, "promptTime");
    }

    @Override
    public void onTimePicked(int hour, int minute, String tag) {
        Calendar dayTimePeriod = GregorianCalendar.getInstance();
        dayTimePeriod.set(Calendar.YEAR, year);
        dayTimePeriod.set(Calendar.MONTH, month);
        dayTimePeriod.set(Calendar.DAY_OF_MONTH, day);
        dayTimePeriod.set(Calendar.HOUR_OF_DAY, hour);
        dayTimePeriod.set(Calendar.MINUTE, minute);

        listener.onDayTimePeriodReceived(dayTimePeriod, tag);
    }

    void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    void setListener(OnDayTimePeriodReceivedListener listener) {
        this.listener = listener;
    }
}

package com.example.patchnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.example.patchnotes.contentdatarelated.DaysOfWeek;
import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentrelated.NoteList;
import com.example.patchnotes.database.AppDatabase;
import com.example.patchnotes.dialogs.InputYesNoDialog;
import com.example.patchnotes.dialogs.ListChooserDialog;
import com.example.patchnotes.dialogs.ShowInfoDialog;
import com.example.patchnotes.dialogs.categoryrelated.CategorySpinnerDialog;
import com.example.patchnotes.dialogs.categoryrelated.SelectCategoryAutoFillAttributesDialog;
import com.example.patchnotes.dialogs.manage_autovalue.RemoveOrEditManageDialog;
import com.example.patchnotes.fragments.NoteListEntryManager;
import com.google.android.material.navigation.NavigationView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;

public class NoteListDisplayActivity extends AbstractContentAttributeSetterActivity implements
        CategorySpinnerDialog.OnCategoryChosenListener, SelectCategoryAutoFillAttributesDialog.OnCategoryAutoFillSelectionConfirmedListener,
        RemoveOrEditManageDialog.OnOptionSelectedListener, InputYesNoDialog.OnResponseListener,
        ListChooserDialog.OnListEntryChosenListener, NoteListEntryManager.OnToggleMultipleSelectionListener {

    static final String INTENT_NOTE_LIST_TO_DISPLAY = "intentNoteListToPass";

    private static final String SAVED_NOTE_LIST = "savedNoteList";

    private static final String TAG_CATEGORY_SPINNER = "tagCategorySpinner";
    private static final String TAG_MANAGE_REMOVE_EDIT_CATEGORY = "tagRemoveEditCategory";
    private static final String TAG_CHOOSE_ENTRY_TYPE = "tagChooseEntryType";
    private static final String TAG_YESNO_DELETE_SELECTED_ENTRIES = "tagYesNoDeleteSelectedEntries";

    private static final int NAV_ITEM_SET_LIST_TYPE_ID = 300200566;
    private static final int NAV_ITEM_TO_DELETE_MODE_ID = 300200567;
    private static final int NAV_ITEM_SPACE_ONE_ID = 1010;
    private static final int NAV_ITEM_MANAGE_CATEGORY_ID = 500600777;
    private static final int NAV_ITEM_VIEW_CATEGORY_ID = 500600888;

    private NoteList noteList;
    private NoteList.Builder noteListBuilder;
    private String initialId;

    private DrawerLayout drawerLayout;
    private NavigationView navViewRight;
    private NoteListEntryManager entryManager;
    private Toolbar toolbar;
    private Menu toolbarMenu;

    private EditText titleView, subTextView;

    private Menu uniqueMenu;

    private boolean databaseAlreadyUpdated;

    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list_display);

        initializeAppDatabase();
        initializeNoteList(savedInstanceState);
        initializeInitialNoteListId();
        initializeNoteListBuilder();

        initializeDrawerLayoutAndNavigationView();
        initializeToolbar();
        initializeUniqueMenu();
        initializeTitleView();
        initializeSubTextView();
        initializeNoteListEntryManagerFragment(savedInstanceState);
    }


    private void initializeAppDatabase() {
        appDatabase = new AppDatabase(this);
    }

    private void initializeNoteList(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            noteList = (NoteList) intent.getSerializableExtra(INTENT_NOTE_LIST_TO_DISPLAY);
        } else {
            noteList = (NoteList) savedInstanceState.getSerializable(SAVED_NOTE_LIST);
        }
    }

    private void initializeInitialNoteListId() {
        if (noteList == null) {
            initialId = null;
        } else {
            initialId = noteList.getUniqueId();
        }
    }

    private void initializeNoteListBuilder() {
        if (noteList == null) {
            noteListBuilder = new NoteList.Builder();
        } else {
            noteListBuilder = noteList.createBuilderWithThisNotesContents();
        }
    }

    private void initializeToolbar() {
        toolbar = findViewById(R.id.noteList_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.note_list_toolbar_menu, menu);
        toolbarMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (entryManager != null && menu != null) {
            menu.findItem(R.id.noteList_toolbar_deleteSelected).setVisible(entryManager.isInMultipleSelectionMode());
            getSupportActionBar().setDisplayHomeAsUpEnabled(entryManager.isInMultipleSelectionMode());
            getSupportActionBar().setDisplayShowTitleEnabled(!entryManager.isInMultipleSelectionMode());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.noteList_toolbar_openDrawer) {
            openDrawer(true);
            return true;
        } else if (item.getItemId() == R.id.noteList_toolbar_deleteSelected) {
            if (!entryManager.getCopyOfHighlightedNoteEntryPosition().isEmpty()) {
                InputYesNoDialog dialog = new InputYesNoDialog();
                dialog.setTag(TAG_YESNO_DELETE_SELECTED_ENTRIES);
                dialog.setText("Delete selected list items?");
                dialog.show(getSupportFragmentManager(), "yesNoTagDeleteEntries");
            } else {
                Toast.makeText(this, R.string.noteListEntry_noEntryToDelete, Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == android.R.id.home) {
            entryManager.setIsInMultipleSelectionMode(false);
            entryManager.setHighlightedEntryPos(new HashSet<>());
            entryManager.setNoteEntryList(entryManager.getCopyOfNoteEntryList());
            entryManager.setNoteEntryType(entryManager.getEntryType());
            entryManager.refreshManager();

            onLeavingMultipleSelectionMode();
        }
        return super.onOptionsItemSelected(item);
    }



    private void initializeDrawerLayoutAndNavigationView() {
        drawerLayout = findViewById(R.id.noteList_drawerLayout);
        navViewRight = findViewById(R.id.noteList_navViewRight);
    }

    private void initializeUniqueMenu() {
        PopupMenu p = new PopupMenu(this, null);
        Menu menu = p.getMenu();
        menu.add(Menu.NONE, NAV_ITEM_SET_LIST_TYPE_ID, 1,
                getResources().getString(R.string.noteListDisplay_nav_setListType_text));
        menu.add(Menu.NONE, NAV_ITEM_TO_DELETE_MODE_ID, 2,
                getResources().getString(R.string.noteListDisplay_nav_toDeleteMode));
        menu.add(Menu.NONE, NAV_ITEM_SPACE_ONE_ID, 3, "");

        menu.add(Menu.NONE, NAV_ITEM_MANAGE_CATEGORY_ID, 1,
                getResources().getString(R.string.noteListDisplay_nav_manageCategory_text));
        menu.add(Menu.NONE, NAV_ITEM_VIEW_CATEGORY_ID, 2,
                getResources().getString(R.string.noteDisplay_nav_viewCategory_text));

        menu.findItem(NAV_ITEM_MANAGE_CATEGORY_ID).setIcon(R.drawable.baseline_category_black_48dp);
        menu.findItem(NAV_ITEM_SET_LIST_TYPE_ID).setIcon(R.drawable.baseline_list_alt_black_48);
        menu.findItem(NAV_ITEM_TO_DELETE_MODE_ID).setIcon(R.drawable.baseline_remove_circle_black_48);

        uniqueMenu = menu;
    }

    private void initializeTitleView() {
        titleView = findViewById(R.id.noteList_titleView);
        titleView.setText(noteListBuilder.getDisplayName());
    }

    private void initializeSubTextView() {
        subTextView = findViewById(R.id.noteList_subTextView);
        subTextView.setText(noteListBuilder.getSubText());
    }

    private void initializeNoteListEntryManagerFragment(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            entryManager = new NoteListEntryManager();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.noteList_listEntryDisplayContainer, entryManager, "entryManager");
            ft.commit();
        } else {
            entryManager = (NoteListEntryManager) getSupportFragmentManager().findFragmentByTag("entryManager");
        }

        entryManager.setNoteEntryType(noteListBuilder.getEntryType());
        entryManager.setNoteEntryList(noteListBuilder.getListEntries());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        updateDatabaseOnlyOnce();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        databaseAlreadyUpdated = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        super.onSaveInstanceState(toSave);

        updateDatabaseOnlyOnce();

        toSave.putSerializable(SAVED_NOTE_LIST, noteList);
    }

    private void updateDatabaseOnlyOnce() {
        if (!databaseAlreadyUpdated) {
            updateValuesOfBuilder();

            AppDatabase.NoteListDatabase noteListDatabase = appDatabase.getMainNoteListDatabase();
            NoteList list = null;

            if (initialId == null) {
                if (!noteListBuilder.isEmpty()) {
                    list = noteListDatabase.addNoteList(noteListBuilder);
                }
            } else {
                list = noteListBuilder.constructNoteList();
                noteListDatabase.updateNoteList(initialId, noteListBuilder);
            }

            noteList = list;
            databaseAlreadyUpdated = true;
        }
    }

    private void updateValuesOfBuilder() {
        noteListBuilder.setDisplayName(((TextView) findViewById(R.id.noteList_titleView)).getText().toString());
        noteListBuilder.setSubText(((TextView) findViewById(R.id.noteList_subTextView)).getText().toString());
        noteListBuilder.setListEntries(entryManager.getCopyOfNoteEntryList());
    }

    //

    @Override
    @NonNull
    protected Content getContent() {
        return noteListBuilder;
    }

    @Override
    protected MenuItem[] getTopPartMenu() {
        return new MenuItem[] {
                uniqueMenu.findItem(NAV_ITEM_SET_LIST_TYPE_ID),
                uniqueMenu.findItem(NAV_ITEM_TO_DELETE_MODE_ID),
                uniqueMenu.findItem(NAV_ITEM_SPACE_ONE_ID)
        };
    }

    @Override
    protected MenuItem[] getBottomPartMenu() {
        return new MenuItem[] {
                uniqueMenu.findItem(NAV_ITEM_MANAGE_CATEGORY_ID),
                uniqueMenu.findItem(NAV_ITEM_VIEW_CATEGORY_ID)
        };
    }

    @Override
    protected void onUnhandledNavigationOptionsSelected(MenuItem item) {
        if (item.getItemId() == NAV_ITEM_SET_LIST_TYPE_ID) {
            showSetListTypeDialog();
        } else if (item.getItemId() == NAV_ITEM_TO_DELETE_MODE_ID) {
            toggleToDeleteEntryMode();
        } else if (item.getItemId() == NAV_ITEM_MANAGE_CATEGORY_ID) {
            manageCategoryOfContent();
        } else if (item.getItemId() == NAV_ITEM_VIEW_CATEGORY_ID) {
            showViewCategoryDialog();
        }
    }

    private void showSetListTypeDialog() {
        ListChooserDialog<NoteList.EntryType> dialog = new ListChooserDialog<>();
        dialog.setMessage("Choose the type of list entry you want to use");
        dialog.setEntryList(Arrays.asList(NoteList.EntryType.values()));
        dialog.setHighlightedEntries(Collections.singletonList(noteListBuilder.getEntryType()));
        dialog.setPictureResourceIdList(NoteList.EntryType.getImageResourcesOfTypes());
        dialog.setTag(TAG_CHOOSE_ENTRY_TYPE);
        dialog.show(getSupportFragmentManager(), "chooseEntryType");
    }

    @Override
    public void onListEntryChosen(Object entryChosen, int pos, String tag) {
        if (tag.equals(TAG_CHOOSE_ENTRY_TYPE)) {
            NoteList.EntryType type = (NoteList.EntryType) entryChosen;

            noteListBuilder.setEntryType(type);
            entryManager.setNoteEntryList(entryManager.getCopyOfNoteEntryList());
            entryManager.setNoteEntryType(type);
            entryManager.setHighlightedEntryPos(entryManager.getHighlightedEntryPos());
            entryManager.setIsInMultipleSelectionMode(entryManager.isInMultipleSelectionMode());
            entryManager.refreshManager();
        }
    }

    private void toggleToDeleteEntryMode() {
        if (!entryManager.isInMultipleSelectionMode()) {
            entryManager.setIsInMultipleSelectionMode(true);
            entryManager.setHighlightedEntryPos(new HashSet<>());
            entryManager.setNoteEntryList(entryManager.getCopyOfNoteEntryList());
            entryManager.setNoteEntryType(entryManager.getEntryType());
            entryManager.refreshManager();

            onEnteringMultipleSelectionMode();
        }

        if (drawerLayout.isDrawerOpen(navViewRight)) {
            closeDrawer(true);
        }
    }

    @Override
    public void onEnteringMultipleSelectionMode() {
        onPrepareOptionsMenu(toolbarMenu);
    }

    @Override
    public void onLeavingMultipleSelectionMode() {
        onPrepareOptionsMenu(toolbarMenu);
    }

    private void manageCategoryOfContent() {
        if (appDatabase.getCategoryDatabase().getCategoryByUniqueId(noteListBuilder.getCategoryUniqueId()) == null) {
            showCategorySelectionDialogIfNotEmpty();
        } else {
            showManageCategoryDialog();
        }
    }

    private void showCategorySelectionDialogIfNotEmpty() {
        Category[] categories = appDatabase.getCategoryDatabase().getAllCategories();

        if (categories.length != 0) {
            CategorySpinnerDialog dialog = new CategorySpinnerDialog();
            dialog.setCategories(categories);
            dialog.setTag(TAG_CATEGORY_SPINNER);
            dialog.setTitle("Set this note's category");
            dialog.show(getSupportFragmentManager(), "categorySpinner");
        } else {
            Toast.makeText(this, "There are no categories to select from", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCategoryChosen(@NonNull Category category, String tag) {
        if (category.isAutoFillEnabled()) {
            showConfigureAutoFillValuesOfCategoryDialog(category, tag);
        } else {
            setCurrentNoteListCategoryTo(category);
        }
    }

    private void setCurrentNoteListCategoryTo(Category category) {
        if (category == null) {
            noteListBuilder.setCategoryUniqueId(null);
            Toast.makeText(this, "Removed category of this note", Toast.LENGTH_SHORT).show();
        } else {
            noteListBuilder.setCategoryUniqueId(category.getUniqueId());
            Toast.makeText(this, "Category set to " + category.getDisplayName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfigureAutoFillValuesOfCategoryDialog(Category category, String tag) {
        SelectCategoryAutoFillAttributesDialog dialog = new SelectCategoryAutoFillAttributesDialog();
        dialog.setContent(noteListBuilder);
        dialog.setCategory(category);
        dialog.setTitle("Configure Attributes");
        dialog.setMessage("Category to inherit has auto fill values. Please configure which ones should be inherited.");
        dialog.setTag(tag);
        dialog.show(getSupportFragmentManager(), "selectAutoVals");
    }

    @Override
    public void onCategoryAutoFillSelectionConfirmed(boolean inheritPrioPeriod, boolean inheritPrioDays, boolean inheritExpiPeriod,
                                                     Category category, String tag) {
        if (inheritPrioPeriod) {
            noteListBuilder.setPriorityPeriod(category.getAutoFillPriorityPeriod());
            onNewPriorityPeriodChosen(category.getAutoFillPriorityPeriod());
        }
        if (inheritPrioDays) {
            noteListBuilder.setPriorityDaysOfWeek(category.getAutoFillPriorityDaysOfWeek());
            onNewPriorityDaysChosen(category.getAutoFillPriorityDaysOfWeek());
        }
        if (inheritExpiPeriod) {
            noteListBuilder.setExpirationPeriod(category.getAutoFillExpirationPeriod());
            onNewExpirationPeriodChosen(category.getAutoFillExpirationPeriod());
        }
        setCurrentNoteListCategoryTo(category);
    }

    private void showManageCategoryDialog() {
        RemoveOrEditManageDialog dialog = new RemoveOrEditManageDialog();
        dialog.setSavedToManageText("Note's Current Category");
        dialog.setTitle("Manage Category");
        dialog.setTag(TAG_MANAGE_REMOVE_EDIT_CATEGORY);
        dialog.show(getSupportFragmentManager(), "ManageDialog");
    }

    private void showViewCategoryDialog() {
        Category currentCategory = appDatabase.getCategoryDatabase().getCategoryByUniqueId(noteListBuilder.getCategoryUniqueId());
        String toDisplay;

        if (currentCategory == null) {
            toDisplay = "Note is currently under no category";
        } else {
            toDisplay = "Note's current category is \"" + currentCategory.getDisplayName() + "\"";
        }

        ShowInfoDialog dialog = new ShowInfoDialog();
        dialog.setContent(toDisplay);
        dialog.show(getSupportFragmentManager(), "ShowInfoCurrNoteList");
    }

    @Override
    public void onEditSelected(String tag) {
        if (tag.equals(TAG_MANAGE_REMOVE_EDIT_CATEGORY)) {
            showCategorySelectionDialogIfNotEmpty();
        }
    }

    @Override
    public void onRemoveSelected(String tag) {
        if (tag.equals(TAG_MANAGE_REMOVE_EDIT_CATEGORY)) {
            InputYesNoDialog dialog = new InputYesNoDialog();
            dialog.setTag(tag);
            dialog.setText("Remove this note from current category?");
            dialog.show(getSupportFragmentManager(), "yesNoRemoveDialog");
        }
    }

    @Override
    public void onYesSelected(String tag) {
        super.onYesSelected(tag);
        if (tag.equals(TAG_MANAGE_REMOVE_EDIT_CATEGORY)) {
            setCurrentNoteListCategoryTo(null);
        } else if (tag.equals(TAG_YESNO_DELETE_SELECTED_ENTRIES)) {
            deleteSelectedEntries();
        }
    }

    private void deleteSelectedEntries() {
        int[] entryPosToDelete = new int[entryManager.getCopyOfHighlightedNoteEntryPosition().size()];
        int i = 0;
        for (Integer integer : entryManager.getHighlightedEntryPos()) {
            entryPosToDelete[i] = integer;
            i++;
        }

        entryManager.setHighlightedEntryPos(new HashSet<>());
        entryManager.deleteEntriesAt(entryPosToDelete);
        if (entryManager.getCopyOfNoteEntryList().isEmpty()) {
            entryManager.setIsInMultipleSelectionMode(false);
            entryManager.refreshManager();
        }
        onLeavingMultipleSelectionMode();
    }

    @Override
    public void onNoSelected(String tag) {}

    //

    @IdRes
    @Override
    protected int getNavigationViewId() {
        return R.id.noteList_navViewRight;
    }

    @IdRes
    @Override
    protected int getDrawerViewId() {
        return R.id.noteList_drawerLayout;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navViewRight)) {
            drawerLayout.closeDrawer(navViewRight);
        } else {
            if (entryManager != null && entryManager.isInMultipleSelectionMode()) {
                entryManager.setIsInMultipleSelectionMode(false);
                entryManager.setHighlightedEntryPos(new HashSet<>());
                entryManager.setNoteEntryList(entryManager.getCopyOfNoteEntryList());
                entryManager.setNoteEntryType(entryManager.getEntryType());
                entryManager.refreshManager();

                onLeavingMultipleSelectionMode();
            } else {
                updateDatabaseOnlyOnce();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onNewPriorityPeriodChosen(@Nullable Calendar time) {
        noteListBuilder.setPriorityPeriod(time);
        super.onNewPriorityPeriodChosen(time);
    }

    @Override
    protected void onNewExpirationPeriodChosen(@Nullable Calendar time) {
        noteListBuilder.setExpirationPeriod(time);
        super.onNewExpirationPeriodChosen(time);
    }

    @Override
    protected void onNewPriorityDaysChosen(@Nullable DaysOfWeek[] daysOfWeeks) {
        noteListBuilder.setPriorityDaysOfWeek(daysOfWeeks);
        super.onNewPriorityDaysChosen(daysOfWeeks);
    }


}

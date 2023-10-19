package com.example.patchnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithm;
import com.example.patchnotes.contentfilter.ContentFilterByDisplayName;
import com.example.patchnotes.contentfilter.ContentFilterByExpirationPeriod;
import com.example.patchnotes.contentfilter.ContentFilterByPriorityDays;
import com.example.patchnotes.contentfilter.ContentFilterByPriorityPeriod;
import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;
import com.example.patchnotes.contentdatarelated.TimePhase;
import com.example.patchnotes.contentsorter.ContentSorterAlgorithm;
import com.example.patchnotes.contentsorter.ContentSorterAlgorithmImp;
import com.example.patchnotes.database.AppDatabase;
import com.example.patchnotes.dialogs.AssessmentDialog;
import com.example.patchnotes.dialogs.DeletePromptDialog;
import com.example.patchnotes.dialogs.FilterAlgorithmPromptDialog;
import com.example.patchnotes.dialogs.SortAlgorithmPromptDialog;
import com.example.patchnotes.fragments.ContentListDisplayer;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

abstract class AbstractContentListDisplayActivity extends AppCompatActivity implements ContentListDisplayer.ContentSelectedListener,
        DeletePromptDialog.OnDeletePressedListener, SortAlgorithmPromptDialog.OnSortAlgorithmSelectedListener,
        FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener {

    private enum Mode implements Serializable {
        SELECT_MODE, DELETE_MODE
    }

    static final String INTENT_MAIN_SETTINGS_DATA = "intentMainSettingsData";

    private static final String SAVED_MODE = "savedMode";
    private static final String SAVED_SORT_ALGORITHM = "savedSortAlgorithm";
    private static final String SAVED_FILTER_LIST = "savedFilterList";

    private ArrayList<ContentFilterAlgorithm> filterAlgorithmHistoryList = new ArrayList<>();
    private ArrayList<Content> currentUnfilteredContentList = new ArrayList<>();
    private ArrayList<Content> currentFilteredContentList = new ArrayList<>();

    private DrawerLayout mainDrawer;
    private NavigationView navRightView;

    protected ContentListDisplayer contentListDisplayer;

    private AppDatabase appDatabase;
    private AppDatabase.SortDatabase sortDatabase;

    private Calendar currentTime;

    private ContentSorterAlgorithm sorterAlgorithm;

    private Mode appMode;

    protected Menu appMenu;

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());

        setUpToolbarAndDrawer();
        setUpNavigationViewBehavior();

        setUpDatabase();
        deleteAllExpiredContentOnStartUp(savedInstanceState);

        initializeSortAlgorithm();

        currentTime = GregorianCalendar.getInstance();
        setUpContentListDisplayer(savedInstanceState, currentTime);
        initializeAppMode(savedInstanceState);
        initializeFilterListEntries(savedInstanceState);
        beforeNoteListUpdates();
        refreshNoteDisplayListAndUpdateDisplay();
    }

    private void setUpToolbarAndDrawer() {
        Toolbar mainToolbar = findViewById(getToolbarId());
        setSupportActionBar(mainToolbar);

        mainDrawer = findViewById(getDrawerId());
    }

    private void setUpNavigationViewBehavior() {
        navRightView = findViewById(getNavigationViewId());
        navRightView.inflateMenu(getNavigationMenuId());
        navRightView.setNavigationItemSelectedListener(this::onNavigationMenuItemSelected);
    }

    @CallSuper
    protected boolean onNavigationMenuItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.navItem_deleteNotes) {
            mainDrawer.closeDrawer(GravityCompat.END);
            if (contentListDisplayer.hasEmptyNoteList()) {
                Toast.makeText(this, R.string.main_toast_noNotesToDelete, Toast.LENGTH_SHORT).show();
            } else {
                toDeleteContentMode();
            }
            return true;
        } else if (menuItem.getItemId() == R.id.navItem_sortNotes) {
            promptSortAlgorithmToUse();
            return true;
        } else if (menuItem.getItemId() == R.id.navItem_filterNotes) {
            promptFilterAlgorithmToUse();
            return true;
        }

        return false;
    }

    private void setUpDatabase() {
        appDatabase = getAppDatabase();
    }

    @NonNull
    protected abstract AppDatabase getAppDatabase();

    abstract protected void deleteAllExpiredContentOnStartUp(Bundle savedInstanceState);

    private void initializeSortAlgorithm() {
        sortDatabase = appDatabase.getSortDatabase();
        sorterAlgorithm = sortDatabase.getContentSortAlgorithm();

        selectSortAlgorithm(sorterAlgorithm);
    }

    private void selectSortAlgorithm(ContentSorterAlgorithm sorterAlgorithm) {
        this.sorterAlgorithm = sorterAlgorithm;
        sortDatabase.setContentSortAlgorithm(sorterAlgorithm);
        if (contentListDisplayer != null) {
            updateMainPanelDisplay(currentFilteredContentList, currentTime, sorterAlgorithm);
        }
    }

    private void setUpContentListDisplayer(Bundle savedInstanceState, Calendar time) {
        if (savedInstanceState == null) {
            contentListDisplayer = new ContentListDisplayer();
            contentListDisplayer.setTime(time);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(getContentListFragmentContainer(), contentListDisplayer, "contentListDisplayer");
            ft.commit();
        } else {
            contentListDisplayer = (ContentListDisplayer) getSupportFragmentManager().findFragmentByTag("contentListDisplayer");
        }
    }

    private void updateMainPanelDisplay(List<Content> unsortedContent, Calendar currentTime, ContentSorterAlgorithm sorterAlgorithm) {
        List<Content> sortedNotesByPriority = sortContentByPriority(unsortedContent, currentTime, sorterAlgorithm);

        if (!filterAlgorithmHistoryList.isEmpty()) {
            getSupportActionBar().setSubtitle("Filtered");
        } else {
            getSupportActionBar().setSubtitle("");
        }
        fillContentListDisplayer(sortedNotesByPriority);
        updateVisibilityStatusOfContentListDisplayers();
    }

    private List<Content> sortContentByPriority(List<Content> toSort, Calendar currentTime, ContentSorterAlgorithm sorterAlgorithm) {
        List<Content> prio = new ArrayList<>(sorterAlgorithm.getArrangedPriorityContent(toSort, currentTime));
        List<Content> nonPrio = new ArrayList<>(sorterAlgorithm.getArrangedNonPriorityContent(toSort, currentTime));

        prio.addAll(nonPrio);

        return prio;
    }

    private void fillContentListDisplayer(List<Content> allContent) {
        contentListDisplayer.setContentList(allContent);
    }

    private void updateVisibilityStatusOfContentListDisplayers() {
        if (contentListDisplayer.hasEmptyNoteList()) {
            findViewById(getCreateContentTextViewWhenEmpty()).setVisibility(View.VISIBLE);
            contentListDisplayer.makeViewGone();
        } else {
            findViewById(getCreateContentTextViewWhenEmpty()).setVisibility(View.GONE);
            contentListDisplayer.makeViewVisible();
        }
    }

    private void initializeAppMode(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            appMode = Mode.SELECT_MODE;
        } else {
            appMode = (Mode) savedInstanceState.getSerializable(SAVED_MODE);
        }

        if (appMode == Mode.SELECT_MODE) {
            toSelectContentMode();
        } else if (appMode == Mode.DELETE_MODE) {
            toDeleteContentMode();
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeFilterListEntries(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            filterAlgorithmHistoryList.addAll((List<ContentFilterAlgorithm>) savedInstanceState.getSerializable(SAVED_FILTER_LIST));
        } else {
            MainSettingsData data = (MainSettingsData) getIntent().getSerializableExtra(INTENT_MAIN_SETTINGS_DATA);
            if (data != null && !ignoreFilterList()) {
                filterAlgorithmHistoryList.addAll(data.getFilterAlgorithms());
            }
        }
    }

    protected void beforeNoteListUpdates() {

    }

    @CallSuper
    protected void refreshNoteDisplayListAndUpdateDisplay() {
        currentUnfilteredContentList.clear();
        currentUnfilteredContentList.addAll(getAllContentFromDatabase());

        currentFilteredContentList.clear();
        currentFilteredContentList.addAll(getFilteredContentList(currentUnfilteredContentList));
        updateMainPanelDisplay(currentFilteredContentList, currentTime, sorterAlgorithm);
    }

    private List<Content> getAllContentFromDatabase() {
        return retrieveContentListFromSourceReader();
    }

    abstract protected List<Content> retrieveContentListFromSourceReader();

    private List<Content> getFilteredContentList(List<Content> currentUnfilteredContentList) {
        List<Content> container = new ArrayList<>(currentUnfilteredContentList);
        for (ContentFilterAlgorithm filter: filterAlgorithmHistoryList) {
            container = filter.getFilteredContents(container);
        }
        return container;
    }

    private void toDeleteContentMode() {
        contentListDisplayer.setMultipleSelectionMode();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        appMode = Mode.DELETE_MODE;

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        onPrepareOptionsMenu(appMenu);
    }

    private void toSelectContentMode() {
        contentListDisplayer.setSingleSelectionMode();
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        appMode = Mode.SELECT_MODE;

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        onPrepareOptionsMenu(appMenu);
    }

    @Override
    public void onBackPressed() {
        if (mainDrawer.isDrawerOpen(GravityCompat.END)) {
            mainDrawer.closeDrawer(GravityCompat.END);
            return;
        }

        if (appMode == Mode.DELETE_MODE) {
            toSelectContentMode();
        } else {
            if (!filterAlgorithmHistoryList.isEmpty()) {
                filterAlgorithmHistoryList.remove(filterAlgorithmHistoryList.size() - 1);
                refreshNoteDisplayListAndUpdateDisplay();
            } else {
                exitThisActivity();
            }
        }
    }

    protected void exitThisActivity() {
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getToolbarMenuId(), menu);
        appMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @CallSuper
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (appMode == Mode.SELECT_MODE) {
                menu.findItem(R.id.menuItem_delete).setVisible(false);
                menu.findItem(R.id.menuItem_assessment).setVisible(true);
            } else if (appMode == Mode.DELETE_MODE) {
                menu.findItem(R.id.menuItem_delete).setVisible(true);
                menu.findItem(R.id.menuItem_assessment).setVisible(false);
            } else {
                return super.onPrepareOptionsMenu(menu);
            }
            return true;
        }
        return false;
    }

    @CallSuper
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuItem_otherActions) {
            mainDrawer.openDrawer(GravityCompat.END);
            return true;
        } else if (item.getItemId() == R.id.menuItem_assessment) {
            showAssessmentDialog();
            return true;
        } else if (item.getItemId() == R.id.menuItem_delete) {
            promptDeleteSelectedNotes();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            toSelectContentMode();
            return true;
        }
        return false;
    }

    private void promptDeleteSelectedNotes() {
        if (contentListDisplayer.hasContentSelectedInMultipleSelectionMode()) {
            DialogFragment deletePrompt = new DeletePromptDialog();
            deletePrompt.show(getSupportFragmentManager(), "deleteFragTag");
        } else {
            Toast.makeText(this, R.string.main_toast_noNotesSelectedToDelete, Toast.LENGTH_SHORT).show();
        }
    }

    private void promptSortAlgorithmToUse() {
        SortAlgorithmPromptDialog dialog = new SortAlgorithmPromptDialog();
        dialog.setSorterAlgorithms(ContentSorterAlgorithmImp.values());
        dialog.setSelectedSorterAlgorithm(sorterAlgorithm);
        dialog.show(getSupportFragmentManager(), "promptSortAlgoToUse");
    }

    private void promptFilterAlgorithmToUse() {
        FilterAlgorithmPromptDialog dialog = new FilterAlgorithmPromptDialog();
        dialog.setFilterAlgorithms(getContentFilterTypesAsList());
        dialog.show(getSupportFragmentManager(), "promptFilterAlgoToUse");
    }

    protected List<ContentFilterAlgorithm> getContentFilterTypesAsList() {
        List<ContentFilterAlgorithm> list = new ArrayList<>();
        list.add(new ContentFilterByDisplayName(""));
        list.add(new ContentFilterByExpirationPeriod(GregorianCalendar.getInstance(), TimePhase.DURING_DAY));
        list.add(new ContentFilterByPriorityDays(new DaysOfWeek[0]));
        list.add(new ContentFilterByPriorityPeriod(GregorianCalendar.getInstance(), TimePhase.DURING_DAY));
        return list;
    }

    private void showAssessmentDialog() {
        AssessmentDialog dialog = new AssessmentDialog();
        dialog.setFilterAlgorithmList(filterAlgorithmHistoryList);
        dialog.setSorterAlgorithm(sorterAlgorithm);
        dialog.show(getSupportFragmentManager(), "assessmentDialog");
    }

    @Override
    public void onDeletePressed() {
        for (Content content: contentListDisplayer.getSelectedContentInMultipleSelectionMode()) {
            deleteContent(content);
        }
        contentListDisplayer.unselectAllNotesInMultipleSelectionMode();
        refreshNoteDisplayListAndUpdateDisplay();

        if (currentFilteredContentList.size() == 0) {
            toSelectContentMode();
        }
    }

    abstract protected void deleteContent(@NonNull Content content);

    @Override
    public void onSortAlgorithmSelected(@NonNull ContentSorterAlgorithm sorterAlgorithm) {
        selectSortAlgorithm(sorterAlgorithm);
        mainDrawer.closeDrawer(GravityCompat.END);
    }

    @Override
    public void onFilterAlgorithmSelected(@NonNull ContentFilterAlgorithm filterAlgorithm) {
        filterAlgorithmHistoryList.add(filterAlgorithm);
        refreshNoteDisplayListAndUpdateDisplay();
        mainDrawer.closeDrawer(GravityCompat.END);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putSerializable(SAVED_MODE, appMode);
        toSave.putSerializable(SAVED_SORT_ALGORITHM, sorterAlgorithm);
        toSave.putSerializable(SAVED_FILTER_LIST, filterAlgorithmHistoryList);
    }

    @Override
    abstract public void onContentSelected(Content selectedContent);

    @Override
    public void startActivity(Intent intent) {
        MainSettingsData data = new MainSettingsData(sorterAlgorithm, filterAlgorithmHistoryList);
        intent.putExtra(INTENT_MAIN_SETTINGS_DATA, data);
        super.startActivity(intent);
    }

    protected void addFilterAlgorithmInList(ContentFilterAlgorithm filter) {
        filterAlgorithmHistoryList.add(filter);
    }

    //

    @LayoutRes
    protected abstract int getContentView();
    @IdRes
    protected abstract int getToolbarId();
    @IdRes
    protected abstract int getDrawerId();
    @IdRes
    protected abstract int getNavigationViewId();
    @IdRes
    protected abstract int getContentListFragmentContainer();
    @IdRes
    protected abstract int getCreateContentTextViewWhenEmpty();
    @MenuRes
    protected abstract int getToolbarMenuId();
    @MenuRes
    protected abstract int getNavigationMenuId();

    protected abstract boolean ignoreFilterList();

}


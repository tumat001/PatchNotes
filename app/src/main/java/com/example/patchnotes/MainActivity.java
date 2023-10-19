package com.example.patchnotes;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithm;
import com.example.patchnotes.contentfilter.ContentFilterByCategory;
import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentrelated.NoteList;
import com.example.patchnotes.database.AppDatabase;
import com.example.patchnotes.database.NoteListSourceReader;
import com.example.patchnotes.database.NoteListSourceWriter;
import com.example.patchnotes.database.NoteSourceReader;
import com.example.patchnotes.database.NoteSourceWriter;
import com.example.patchnotes.dialogs.ListChooserDialog;
import com.example.patchnotes.dialogs.ShowInfoDialog;
import com.example.patchnotes.contentrelated.Note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends AbstractContentListDisplayActivity implements
        ListChooserDialog.OnListEntryChosenListener {

    private static final String TAG_CONTENT_TYPE_CHOSEN = "tagContentTypeChosen";

    private AppDatabase.NoteDatabase noteDatabase;
    private AppDatabase.NoteListDatabase noteListDatabase;
    private AppDatabase appDatabase;

    @NonNull
    @Override
    protected AppDatabase getAppDatabase() {
        if (appDatabase == null) {
            appDatabase = new AppDatabase(this);
        }
        return appDatabase;
    }

    @NonNull
    private NoteSourceReader getNoteSourceReader() {
        noteDatabase = getAppDatabase().getMainNoteDatabase();
        return noteDatabase;
    }
    @NonNull
    private NoteSourceWriter getNoteSourceWriter() {
        noteDatabase = getAppDatabase().getMainNoteDatabase();
        return noteDatabase;
    }
    @NonNull
    private NoteListSourceReader getNoteListSourceReader() {
        noteListDatabase = getAppDatabase().getMainNoteListDatabase();
        return noteListDatabase;
    }
    @NonNull
    private NoteListSourceWriter getNoteListSourceWriter() {
        noteListDatabase = getAppDatabase().getMainNoteListDatabase();
        return noteListDatabase;
    }

    @Override
    protected List<Content> retrieveContentListFromSourceReader() {
        List<Content> bucket = new ArrayList<>();
        bucket.addAll(Arrays.asList(getNoteSourceReader().getAllNotes()));
        bucket.addAll(Arrays.asList(getNoteListSourceReader().getAllNoteList()));

        return bucket;
    }

    @LayoutRes
    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected int getToolbarId() {
        return R.id.main_toolbar;
    }

    @Override
    protected int getDrawerId() {
        return R.id.main_drawer;
    }

    @Override
    protected int getNavigationViewId() {
        return R.id.main_navRight;
    }

    @Override
    protected int getContentListFragmentContainer() {
        return R.id.main_notesList;
    }

    @Override
    protected int getCreateContentTextViewWhenEmpty() {
        return R.id.main_createNoteHint;
    }

    @Override
    protected int getToolbarMenuId() {
        return R.menu.main_toolbar_menu_items;
    }

    @Override
    protected int getNavigationMenuId() {
        return R.menu.main_nav_right;
    }

    @Override
    protected boolean onNavigationMenuItemSelected(MenuItem item) {
        boolean noActionDone = !super.onNavigationMenuItemSelected(item);
        if (noActionDone) {
            if (item.getItemId() == R.id.navItem_toExpiredNotes) {
                startDisplayExpiredNotesActivity();
                return true;
            } else if (item.getItemId() == R.id.navItem_appInfo) {
                showAppInfoDialog();
                return true;
            } else if (item.getItemId() == R.id.navItem_toCategoryList) {
                startCategoryListActivity();
                return true;
            } else if (item.getItemId() == R.id.navItem_newNote) {
                showContentTypeChooserDialog();
                return true;
            }
        }
        return false;
    }

    private void startDisplayExpiredNotesActivity() {
        Intent intent = new Intent(this, ExpiredContentListDisplayActivity.class);
        startActivity(intent);
    }

    private void showAppInfoDialog() {
        ShowInfoDialog dialog = new ShowInfoDialog();
        dialog.setTitle("App Info");
        dialog.setContent(getResources().getString(R.string.info_infoAboutApp));
        dialog.show(getSupportFragmentManager(), "AppInfo");
    }

    private void startCategoryListActivity() {
        Intent intent = new Intent(this, CategoryListDisplayActivity.class);
        startActivity(intent);
    }

    private void showContentTypeChooserDialog() {
        ListChooserDialog<String> dialog = new ListChooserDialog<>();
        dialog.setEntryList(getContentTypeAsString());
        dialog.setPictureResourceIdList(getContentTypePictureResId());
        dialog.setMessage("Select type of note to create");
        dialog.setTag(TAG_CONTENT_TYPE_CHOSEN);
        dialog.show(getSupportFragmentManager(), "contentTypeChooser");
    }

    private List<String> getContentTypeAsString() {
        return Arrays.asList(
                "Plain Note",
                "Note List"
        );
    }

    private List<Integer> getContentTypePictureResId() {
        return Arrays.asList(
                R.drawable.baseline_notes_black_48dp,
                R.drawable.baseline_format_list_bulleted_black_48dp
        );
    }

    @Override
    public void onListEntryChosen(Object entry, int index, String tag) {
        if (tag.equals(TAG_CONTENT_TYPE_CHOSEN)) {
            String chosen = String.valueOf(entry);
            if (chosen.equalsIgnoreCase("Plain Note")) {
                onContentSelected(new Note.Builder().constructNote());
            } else if (chosen.equalsIgnoreCase("Note List")) {
                onContentSelected(new NoteList.Builder().constructNoteList());
            }
        }
    }

    @Override
    protected boolean ignoreFilterList() {
        return false;
    }

    @Override
    protected List<ContentFilterAlgorithm> getContentFilterTypesAsList() {
        List<ContentFilterAlgorithm> origList = super.getContentFilterTypesAsList();
        origList.add(new ContentFilterByCategory(null));
        return origList;
    }

    @Override
    protected void deleteAllExpiredContentOnStartUp(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Calendar currentTime = GregorianCalendar.getInstance();
            List<Content> expiredContent = getAllExpiredContent(currentTime);

            if (expiredContent.size() != 0) {
                AppDatabase.NoteDatabase expiredNoteDatabase = appDatabase.getExpiredNoteDatabase();
                AppDatabase.NoteListDatabase expiredNoteListDatabase = appDatabase.getExpiredNoteListDatabase();
                for (Content content: expiredContent) {
                    if (content instanceof Note) {
                        Note note = (Note) content;
                        Note.Builder builder = note.makeBuilderHaveNotesContents();
                        builder.setUniqueId(note.getUniqueId());


                        getNoteSourceWriter().deleteNote(content.getUniqueId());
                        expiredNoteDatabase.addNote(builder);
                    } else if (content instanceof NoteList) {
                        getNoteListSourceWriter().deleteNoteList(content.getUniqueId());

                        NoteList list = (NoteList) content;
                        NoteList.Builder builder = list.createBuilderWithThisNotesContents();
                        builder.setCreationPeriod(GregorianCalendar.getInstance());

                        expiredNoteListDatabase.addNoteList(builder);
                    }
                }
            }

            appDatabase.getCategoryDatabase().deleteAllExpiredCategory(currentTime);
        }
    }

    private List<Content> getAllExpiredContent(Calendar time) {
        List<Content> bucket = new ArrayList<>();

        for (Content content: retrieveContentListFromSourceReader()) {
            if (content.isExpiredAfterTime(time)) {
                bucket.add(content);
            }
        }
        return bucket;
    }

    @Override
    protected void deleteContent(@NonNull Content content) {
        if (content instanceof Note) {
            getNoteSourceWriter().deleteNote(content.getUniqueId());
        } else if (content instanceof NoteList) {
            getNoteListSourceWriter().deleteNoteList(content.getUniqueId());
        }
    }

    @Override
    public void onContentSelected(Content selectedContent) {
        if (selectedContent instanceof Note) {
            Intent intent = new Intent(this, NoteDisplayActivity.class);
            intent.putExtra(NoteDisplayActivity.INTENT_NOTE_TO_DISPLAY, selectedContent);

            startActivity(intent);
        } else if (selectedContent instanceof NoteList) {
            Intent intent = new Intent(this, NoteListDisplayActivity.class);
            intent.putExtra(NoteListDisplayActivity.INTENT_NOTE_LIST_TO_DISPLAY, selectedContent);

            startActivity(intent);
        }
    }

    @Override
    public void onContentLongSelected(Content content) {
        onContentSelected(content);
    }

}

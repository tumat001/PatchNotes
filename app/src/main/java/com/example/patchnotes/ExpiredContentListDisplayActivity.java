package com.example.patchnotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentrelated.NoteList;
import com.example.patchnotes.database.AppDatabase;
import com.example.patchnotes.database.NoteListSourceReader;
import com.example.patchnotes.database.NoteListSourceWriter;
import com.example.patchnotes.database.NoteSourceReader;
import com.example.patchnotes.database.NoteSourceWriter;
import com.example.patchnotes.database.TimeThreshold;
import com.example.patchnotes.dialogs.InputYesNoDialog;
import com.example.patchnotes.dialogs.ShowInfoDialog;
import com.example.patchnotes.dialogs.timethreshold.InputTimeThresholdChoiceDialog;
import com.example.patchnotes.dialogs.timethreshold.OnInputTimeThresholdListener;
import com.example.patchnotes.contentrelated.Note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class ExpiredContentListDisplayActivity extends AbstractContentListDisplayActivity implements InputYesNoDialog.OnResponseListener,
        OnInputTimeThresholdListener {

    private static final String HEADER_TAG_NOTE = "NOTE";
    private static final String HEADER_TAG_NOTE_LIST = "LIST";

    private AppDatabase.NoteDatabase expiredNoteDatabase;
    private AppDatabase.NoteListDatabase expiredNoteListDatabase;
    private AppDatabase appDatabase;
    private AppDatabase.DeletionThresholdDatabase deletionThresholdDatabase;

    private List<Content> markedContent = new ArrayList<>();

    @NonNull
    @Override
    protected AppDatabase getAppDatabase() {
        if (appDatabase == null) {
            appDatabase = new AppDatabase(this);
            deletionThresholdDatabase = appDatabase.getDeletionThresholdDatabase();
        }
        return appDatabase;
    }

    @NonNull
    private NoteSourceWriter getNoteSourceWriter() {
        expiredNoteDatabase = appDatabase.getExpiredNoteDatabase();
        return expiredNoteDatabase;
    }
    @NonNull
    private NoteSourceReader getNoteSourceReader() {
        expiredNoteDatabase = appDatabase.getExpiredNoteDatabase();
        return expiredNoteDatabase;
    }

    @NonNull
    private NoteListSourceReader getNoteListSourceReader() {
        expiredNoteListDatabase = getAppDatabase().getExpiredNoteListDatabase();
        return expiredNoteListDatabase;
    }
    @NonNull
    private NoteListSourceWriter getNoteListSourceWriter() {
        expiredNoteListDatabase = getAppDatabase().getExpiredNoteListDatabase();
        return expiredNoteListDatabase;
    }

    @Override
    protected List<Content> retrieveContentListFromSourceReader() {
        List<Content> allExpiredContent = new ArrayList<>();
        allExpiredContent.addAll(Arrays.asList(getNoteListSourceReader().getAllNoteList()));
        allExpiredContent.addAll(Arrays.asList(getNoteSourceReader().getAllNotes()));

        return allExpiredContent;
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_expired_note_list_display;
    }

    @Override
    protected int getToolbarId() {
        return R.id.expiredNotes_toolbar;
    }

    @Override
    protected int getDrawerId() {
        return R.id.expiredNotes_drawer;
    }

    @Override
    protected int getNavigationViewId() {
        return R.id.expiredNotes_navRight;
    }

    @Override
    protected int getContentListFragmentContainer() {
        return R.id.expiredNotes_notesList;
    }

    @Override
    protected int getCreateContentTextViewWhenEmpty() {
        return R.id.expiredNotes_createNoteHint;
    }

    @Override
    protected int getToolbarMenuId() {
        return R.menu.expired_notes_toolbar_menu_items;
    }

    @Override
    protected int getNavigationMenuId() {
        return R.menu.expired_note_nav_right;
    }

    @Override
    protected boolean ignoreFilterList() {
        return true;
    }

    @Override
    protected void exitThisActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void deleteAllExpiredContentOnStartUp(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            List<Content> toDelete = getNotesBeyondDeletionThreshold();
            for (Content content: toDelete) {
                if (content instanceof Note) {
                    expiredNoteDatabase.deleteNote(content.getUniqueId());
                } else if (content instanceof NoteList) {
                    expiredNoteListDatabase.deleteNoteList(content.getUniqueId());
                }
            }
        }
    }

    @Override
    protected void deleteContent(@NonNull Content content) {
        if (content instanceof Note) {
            getNoteSourceWriter().deleteNote(content.getUniqueId());
        } else if (content instanceof NoteList) {
            getNoteListSourceWriter().deleteNoteList(content.getUniqueId());
        }
    }

    private List<Content> getNotesBeyondDeletionThreshold() {
        Calendar expiThreshold = GregorianCalendar.getInstance();
        TimeThreshold threshold = appDatabase.getDeletionThresholdDatabase().getDeletionThreshold();
        expiThreshold.add(threshold.getUnitThreshold().getCalendarConstantEquivalent(), threshold.getNumberThreshold() * -1);

        List<Content> deleteBucket = new ArrayList<>();

        if (threshold.getNumberThreshold() != TimeThreshold.NEVER_DELETE) {
            for (Content content : retrieveContentListFromSourceReader()) {
                if (content.isExpiredAfterTime(expiThreshold)) {
                    deleteBucket.add(content);
                }
            }
        }
        return deleteBucket;
    }

    @Override
    public void onContentSelected(Content content) {
        InputYesNoDialog dialog = new InputYesNoDialog();
        dialog.setTag(getTagToSet(content));
        dialog.setText("Unarchive \"" + content.getDisplayName() + "\" ?");
        dialog.show(getSupportFragmentManager(), "DIALOG_YES_NO");
    }

    private String getTagToSet(Content content) {
        StringBuilder builder = new StringBuilder();
        String headerTag = "";

        if (content instanceof Note) {
            headerTag = HEADER_TAG_NOTE;
        } else if (content instanceof NoteList) {
            headerTag = HEADER_TAG_NOTE_LIST;
        }

        builder.append(headerTag)
                .append(content.getUniqueId());

        return builder.toString();
    }

    @Override
    public void onContentLongSelected(Content content) {
        onContentSelected(content);
    }

    @Override
    public void onYesSelected(String selectedNoteIdWithHeaderTag) {
        String uniqueId = getUniqueIdInTag(selectedNoteIdWithHeaderTag);
        if (uniqueId != null) {
            if (selectedNoteIdWithHeaderTag.contains(HEADER_TAG_NOTE)) {
                unarchiveNoteWithId(uniqueId);
            } else if (selectedNoteIdWithHeaderTag.contains(HEADER_TAG_NOTE_LIST)) {
                unarchiveNoteListWithId(uniqueId);
            }
        }
    }

    private String getUniqueIdInTag(String tag) {
        if (tag.contains(HEADER_TAG_NOTE)) {
            return tag.substring(HEADER_TAG_NOTE.length());
        } else if (tag.contains(HEADER_TAG_NOTE_LIST)) {
            return tag.substring(HEADER_TAG_NOTE_LIST.length());
        }
        return null;
    }

    private void unarchiveNoteWithId(String noteId) {
        Note expiredNoteToUnarchive = expiredNoteDatabase.getNoteById(noteId);
        if (expiredNoteToUnarchive != null) {
            Note.Builder copiedContents = new Note.Builder().copyContentsFromNote(expiredNoteToUnarchive)
                    .setExpirationPeriod(null);

            appDatabase.getMainNoteDatabase().addNote(copiedContents);
            expiredNoteDatabase.deleteNote(noteId);
            refreshNoteDisplayListAndUpdateDisplay();
        }
    }

    private void unarchiveNoteListWithId(String listId) {
        NoteList listToUnarchive = expiredNoteListDatabase.getNoteListByUniqueId(listId);
        if (listToUnarchive != null) {
            NoteList.Builder builder = listToUnarchive.createBuilderWithThisNotesContents();
            builder.setExpirationPeriod(null)
                    .setCreationPeriod(GregorianCalendar.getInstance());

            appDatabase.getMainNoteListDatabase().addNoteList(builder);
            expiredNoteListDatabase.deleteNoteList(listId);
            refreshNoteDisplayListAndUpdateDisplay();
        }
    }

    @Override
    public void refreshNoteDisplayListAndUpdateDisplay() {
        super.refreshNoteDisplayListAndUpdateDisplay();
        updateDeletionThresholdNoteStatus();
    }

    @Override
    public void onNoSelected(String tag) {}

    @Override
    public boolean onNavigationMenuItemSelected(MenuItem item) {
        boolean successful = super.onNavigationMenuItemSelected(item);
        if (!successful) {
            if (item.getItemId() == R.id.navItem_changeDeletionThreshold) {
                showSelectChangeDeletionThreshold();
                return true;
            } else if (item.getItemId() == R.id.navItem_showDeletionThreshold) {
                showInfoAboutDeletionThreshold();
                return true;
            }
        }
        return false;
    }

    private void showSelectChangeDeletionThreshold() {
        InputTimeThresholdChoiceDialog dialog = new InputTimeThresholdChoiceDialog();
        dialog.setInitialTimeThreshold(getDeletionTimeThreshold());
        dialog.show(getSupportFragmentManager(), "inputTimeThreshold");
    }

    private TimeThreshold getDeletionTimeThreshold() {
        return appDatabase.getDeletionThresholdDatabase().getDeletionThreshold();
    }

    private void showInfoAboutDeletionThreshold() {
        ShowInfoDialog dialog = new ShowInfoDialog();
        dialog.setContent(getDeletionThresholdMessage(deletionThresholdDatabase.getDeletionThreshold()));
        dialog.setTitle("Deletion Threshold");
        dialog.show(getSupportFragmentManager(), "ShowInfoDeletion");
    }

    private String getDeletionThresholdMessage(TimeThreshold timeThreshold) {
        return new DeletionThresholdMessageBuilder(timeThreshold).getMessage();
    }

    @Override
    public void onInputTimeThreshold(TimeThreshold threshold) {
        appDatabase.getDeletionThresholdDatabase().setDeletionThreshold(threshold);
        Toast.makeText(this, "Deletion threshold set", Toast.LENGTH_SHORT).show();

        updateDeletionThresholdNoteStatus();
    }

    private void updateDeletionThresholdNoteStatus() {
        markedContent = getNotesBeyondDeletionThreshold();
        markNotesBeyondDeletionThreshold(markedContent);

        onPrepareOptionsMenu(appMenu);
    }

    private void markNotesBeyondDeletionThreshold(List<Content> toMark) {
        contentListDisplayer.highlightContent(toMark, 0x99FFAAAA);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (super.onPrepareOptionsMenu(menu)) {
            MenuItem deletionWarning = menu.findItem(R.id.menuItem_deletionWarning);
            if (markedContent.size() > 0) {
                deletionWarning.setVisible(true);
            } else {
                deletionWarning.setVisible(false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean successful = super.onOptionsItemSelected(item);
        if (!successful) {
            if (item.getItemId() == R.id.menuItem_deletionWarning) {
                showWarningDialog();
                return true;
            }
        }
        return false;
    }

    private void showWarningDialog() {
        ShowInfoDialog dialog = new ShowInfoDialog();
        dialog.setTitle("Warning");
        dialog.setContent("Notes marked in red are about to be deleted due to the new deletion threshold");
        dialog.show(getSupportFragmentManager(), "DeletionWarning");
    }

    @Override
    protected void beforeNoteListUpdates() {
        updateDeletionThresholdNoteStatus();
    }

    //

    private static class DeletionThresholdMessageBuilder {

        TimeThreshold threshold;

        private DeletionThresholdMessageBuilder(TimeThreshold timeThreshold) {
            this.threshold = timeThreshold;
        }

        private String getMessage() {
            return getIntro() + getNumericalUnit() + getUnitName() + getExtra();
        }

        private String getIntro() {
            if (threshold.getNumberThreshold() == -1) {
                return "Notes will never be deleted regardless of how long they stay expired";
            } else if (threshold.getNumberThreshold() == 0) {
                return "Notes will be deleted right away as they expire (you won't see them)";
            } else {
                return "Notes will be deleted after ";
            }
        }

        private String getNumericalUnit() {
            if (threshold.getNumberThreshold() > 0) {
                return threshold.getNumberThreshold() + " ";
            } else {
                return "";
            }
        }

        private String getUnitName() {
            if (threshold.getNumberThreshold() == -1 || threshold.getNumberThreshold() == 0) {
                return "";
            } else if (threshold.getNumberThreshold() == 1) {
                return threshold.getUnitThreshold().toString().toLowerCase();
            } else {
                return threshold.getUnitThreshold().toString().toLowerCase() + "s";
            }
        }

        private String getExtra() {
            if (threshold.getNumberThreshold() >= 1) {
                return " of staying expired";
            } else {
                return "";
            }
        }

    }

}

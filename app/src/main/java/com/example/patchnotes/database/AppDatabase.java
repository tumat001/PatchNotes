package com.example.patchnotes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentdatarelated.DataListParser;
import com.example.patchnotes.contentdatarelated.NoteListEntryParser;
import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;
import com.example.patchnotes.contentrelated.Note;
import com.example.patchnotes.contentrelated.NoteList;
import com.example.patchnotes.contentsorter.ContentSorterAlgorithm;
import com.example.patchnotes.contentsorter.ContentSorterAlgorithmImp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class AppDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "APP_DATABASE";
    private static final int DATABASE_VERSION = 6;

    private static final String MAIN_NOTE_TABLE_TABLE_NAME = "NOTE_TABLE";
    private static final String EXPIRED_NOTE_TABLE_TABLE_NAME = "EXPIRED_NOTE_TABLE";
    private static final String SORT_CHOSEN_TABLE_TABLE_NAME = "SORT_CHOSEN_TABLE";
    private static final String DELETION_THRESHOLD_TABLE_TABLE_NAME = "DELETION_THRESHOLD_TABLE";
    private static final String CATEGORY_TABLE_TABLE_NAME = "CATEGORY_TABLE";
    private static final String MAIN_NOTE_LIST_TABLE_TABLE_NAME = "NOTE_LIST_TABLE";
    private static final String EXPIRED_NOTE_LIST_TABLE_TABLE_NAME = "EXPIRED_NOTE_LIST_TABLE";

    private static final String NOTE_TABLE_INC_ID = "_id";
    private static final String NOTE_TABLE_TITLE = "TITLE";
    private static final String NOTE_TABLE_TEXT = "TEXT";
    private static final String NOTE_TABLE_PRIORITY_PERIOD = "PRIO_PERIOD";
    private static final String NOTE_TABLE_PRIORITY_DAYS = "PRIO_DAYS";
    private static final String NOTE_TABLE_EXPIRATION_PERIOD = "EXPI_PERIOD";
    private static final String NOTE_TABLE_LAST_VISITED_PERIOD = "LAST_VISIT_PERIOD";
    private static final String NOTE_TABLE_CATEGORY_ID = "NOTE_CATEGORY_ID";

    private static final String SORT_ORDINAL_CHOSEN_COL = "SORT_ORDINAL_CHOSEN";

    private static final String DELETION_THRESHOLD_NUM = "DELETION_THRESHOLD_NUM";
    private static final String DELETION_THRESHOLD_UNIT = "DELETION_THRESHOLD_UNIT";

    private static final String CATEGORY_TABLE_DISPLAY_NAME = "DISPLAY_NAME";
    private static final String CATEGORY_TABLE_DESCRIPTION = "DESCRIPTION";
    private static final String CATEGORY_TABLE_UNIQUE_ID = "_id";
    private static final String CATEGORY_TABLE_SELF_CATEGORY_NAME = "SELF_CATEGORY_NAME";
    private static final String CATEGORY_TABLE_SELF_PRIORITY_PERIOD = "SELF_PRIO_PERIOD";
    private static final String CATEGORY_TABLE_SELF_PRIORITY_DAYS = "SELF_PRIO_DAYS";
    private static final String CATEGORY_TABLE_SELF_EXPIRATION_PERIOD = "SELF_EXPI_PERIOD";
    private static final String CATEGORY_TABLE_LAST_VISITED_PERIOD = "SELF_LAST_VISIT_PERIOD";
    private static final String CATEGORY_TABLE_AUTOFILL_PRIORITY_PERIOD = "AUTOFILL_PRIO_PERIOD";
    private static final String CATEGORY_TABLE_AUTOFILL_PRIORITY_DAYS = "AUTOFILL_PRIO_DAYS";
    private static final String CATEGORY_TABLE_AUTOFILL_EXPIRATION_PERIOD = "AUTOFILL_EXPI_PERIOD";
    private static final String CATEGORY_TABLE_AUTOFILL_ENABLED = "AUTOFILLED_ENABLED";
    private static final String CATEGORY_TABLE_PICTURE_RES_ID = "PICTURE_RES_ID";

    private static final String NOTE_LIST_TABLE_UNIQUE_ID = "_id";
    private static final String NOTE_LIST_TABLE_TITLE = "TITLE";
    private static final String NOTE_LIST_TABLE_SUBTEXT = "SUBTEXT";
    private static final String NOTE_LIST_TABLE_CATEGORY_UNIQUE_ID = "CATEGORY_UNIQUE_ID";
    private static final String NOTE_LIST_TABLE_LAST_VISITED_PERIOD = "LAST_VISITED_PERIOD";
    private static final String NOTE_LIST_TABLE_PRIORITY_PERIOD = "PRIORITY_PERIOD";
    private static final String NOTE_LIST_TABLE_PRIORITY_DAYS_OF_WEEK = "PRIORITY_DAYS_OF_WEEK";
    private static final String NOTE_LIST_TABLE_EXPIRATION_PERIOD = "EXPIRATION_PERIOD";
    private static final String NOTE_LIST_TABLE_ENTRIES = "ENTRIES";
    private static final String NOTE_LIST_TABLE_ENTRY_TYPE = "ENTRY_TYPE";

    private MainNoteDatabase mainNoteDatabase;
    private ExpiredNoteDatabase expiredNoteDatabase;
    private SortDatabase sortDatabase;
    private DeletionThresholdDatabase deletionThresholdDatabase;
    private CategoryDatabase categoryDatabase;
    private MainNoteListDatabase mainNoteListDatabase;
    private ExpiredNoteListDatabase expiredNoteListDatabase;

    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        constructDatabase(database, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldV, int newV) {
        constructDatabase(database, oldV, newV);
    }

    private void constructDatabase(SQLiteDatabase database, int oldV, int newV) {
        if (oldV < 1) {
            database.execSQL("CREATE TABLE " + MAIN_NOTE_TABLE_TABLE_NAME + " ("
                    + NOTE_TABLE_INC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NOTE_TABLE_TITLE + " TEXT,"
                    + NOTE_TABLE_TEXT + " TEXT,"
                    + NOTE_TABLE_PRIORITY_PERIOD + " TEXT,"
                    + NOTE_TABLE_PRIORITY_DAYS + " TEXT,"
                    + NOTE_TABLE_EXPIRATION_PERIOD + " TEXT,"
                    + NOTE_TABLE_LAST_VISITED_PERIOD + " TEXT,"
                    + NOTE_TABLE_CATEGORY_ID + " TEXT,"
                    + "FOREIGN KEY (" + NOTE_TABLE_CATEGORY_ID + ") REFERENCES "
                    + CATEGORY_TABLE_TABLE_NAME + " (" + CATEGORY_TABLE_UNIQUE_ID + ") ON DELETE SET NULL ON UPDATE CASCADE);"
            );
        }
        if (oldV < 2) {
            database.execSQL("CREATE TABLE " + EXPIRED_NOTE_TABLE_TABLE_NAME + " ("
                    + NOTE_TABLE_INC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NOTE_TABLE_TITLE + " TEXT,"
                    + NOTE_TABLE_TEXT + " TEXT,"
                    + NOTE_TABLE_PRIORITY_PERIOD + " TEXT,"
                    + NOTE_TABLE_PRIORITY_DAYS + " TEXT,"
                    + NOTE_TABLE_EXPIRATION_PERIOD + " TEXT,"
                    + NOTE_TABLE_LAST_VISITED_PERIOD + " TEXT,"
                    + NOTE_TABLE_CATEGORY_ID + " TEXT DEFAULT NULL);"
            );
        }
        if (oldV < 3) {
            database.execSQL("CREATE TABLE " + SORT_CHOSEN_TABLE_TABLE_NAME + " ("
                    + SORT_ORDINAL_CHOSEN_COL + " INTEGER);"
            );

            ContentValues defaultSortVal = new ContentValues();
            defaultSortVal.put(SORT_ORDINAL_CHOSEN_COL, 0);

            database.insert(SORT_CHOSEN_TABLE_TABLE_NAME, null, defaultSortVal);
        }
        if (oldV < 4) {
            database.execSQL("CREATE TABLE " + DELETION_THRESHOLD_TABLE_TABLE_NAME + " ("
                    + DELETION_THRESHOLD_NUM + " INTEGER,"
                    + DELETION_THRESHOLD_UNIT + " TEXT);"
            );

            ContentValues deletionDefaultVal = new ContentValues();
            deletionDefaultVal.put(DELETION_THRESHOLD_NUM, 3);
            deletionDefaultVal.put(DELETION_THRESHOLD_UNIT, "D");

            database.insert(DELETION_THRESHOLD_TABLE_TABLE_NAME, null, deletionDefaultVal);
        }
        if (oldV < 5) {
            database.execSQL("CREATE TABLE " + CATEGORY_TABLE_TABLE_NAME + " ("
                    + CATEGORY_TABLE_UNIQUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + CATEGORY_TABLE_DISPLAY_NAME + " TEXT,"
                    + CATEGORY_TABLE_DESCRIPTION + " TEXT,"
                    + CATEGORY_TABLE_SELF_CATEGORY_NAME + " TEXT,"
                    + CATEGORY_TABLE_LAST_VISITED_PERIOD + " TEXT,"
                    + CATEGORY_TABLE_SELF_PRIORITY_DAYS + " TEXT,"
                    + CATEGORY_TABLE_SELF_PRIORITY_PERIOD + " TEXT,"
                    + CATEGORY_TABLE_SELF_EXPIRATION_PERIOD + " TEXT,"
                    + CATEGORY_TABLE_AUTOFILL_ENABLED + " INTEGER,"
                    + CATEGORY_TABLE_AUTOFILL_PRIORITY_DAYS + " TEXT,"
                    + CATEGORY_TABLE_AUTOFILL_PRIORITY_PERIOD + " TEXT,"
                    + CATEGORY_TABLE_AUTOFILL_EXPIRATION_PERIOD + " TEXT,"
                    + CATEGORY_TABLE_PICTURE_RES_ID + " INTEGER);"
            );
        }
        if (oldV < 6) {
            database.execSQL("CREATE TABLE " + MAIN_NOTE_LIST_TABLE_TABLE_NAME + " ("
                    + NOTE_LIST_TABLE_UNIQUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NOTE_LIST_TABLE_TITLE + " TEXT,"
                    + NOTE_LIST_TABLE_SUBTEXT + " TEXT,"
                    + NOTE_LIST_TABLE_LAST_VISITED_PERIOD + " TEXT,"
                    + NOTE_LIST_TABLE_PRIORITY_DAYS_OF_WEEK + " TEXT,"
                    + NOTE_LIST_TABLE_PRIORITY_PERIOD + " TEXT,"
                    + NOTE_LIST_TABLE_EXPIRATION_PERIOD + " TEXT,"
                    + NOTE_LIST_TABLE_ENTRIES + " TEXT,"
                    + NOTE_LIST_TABLE_ENTRY_TYPE + " TEXT,"
                    + NOTE_LIST_TABLE_CATEGORY_UNIQUE_ID + " TEXT,"
                    + "FOREIGN KEY (" + NOTE_LIST_TABLE_CATEGORY_UNIQUE_ID + ") REFERENCES "
                    + CATEGORY_TABLE_TABLE_NAME + " (" + CATEGORY_TABLE_UNIQUE_ID + ") ON DELETE SET NULL ON UPDATE CASCADE);"
            );

            database.execSQL("CREATE TABLE " + EXPIRED_NOTE_LIST_TABLE_TABLE_NAME + " ("
                    + NOTE_LIST_TABLE_UNIQUE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + NOTE_LIST_TABLE_TITLE + " TEXT,"
                    + NOTE_LIST_TABLE_SUBTEXT + " TEXT,"
                    + NOTE_LIST_TABLE_LAST_VISITED_PERIOD + " TEXT,"
                    + NOTE_LIST_TABLE_PRIORITY_DAYS_OF_WEEK + " TEXT,"
                    + NOTE_LIST_TABLE_PRIORITY_PERIOD + " TEXT,"
                    + NOTE_LIST_TABLE_EXPIRATION_PERIOD + " TEXT,"
                    + NOTE_LIST_TABLE_ENTRIES + " TEXT,"
                    + NOTE_LIST_TABLE_ENTRY_TYPE + " TEXT,"
                    + NOTE_LIST_TABLE_CATEGORY_UNIQUE_ID + " TEXT DEFAULT NULL);"
            );

        }
    }

    /**
     * @return A NoteDatabase object that points to the main note database. Only creates one instance, that is, once called, it will
     * return the same database;
     */
    public NoteDatabase getMainNoteDatabase() {
        if (mainNoteDatabase == null) {
            mainNoteDatabase = new MainNoteDatabase(this);
        }
        return mainNoteDatabase;
    }

    /**
     * @return A NoteDatabase object that points to the expired note database. Only creates one instance, that is, once called, it will
     * return the same database;
     */
    public NoteDatabase getExpiredNoteDatabase() {
        if (expiredNoteDatabase == null) {
            expiredNoteDatabase = new ExpiredNoteDatabase(this);
        }
        return expiredNoteDatabase;

    }

    /**
     * @return A NoteDatabase object that points to the sort database. Only creates one instance, that is, once called, it will
     * return the same database;
     */
    public SortDatabase getSortDatabase() {
        if (sortDatabase == null) {
            sortDatabase = new SortDatabase(this);
        }
        return sortDatabase;
    }

    /**
     * @return A NoteDatabase object that points to the deletion threshold database. Only creates one instance, that is, once called, it will
     * return the same database;
     */
    public DeletionThresholdDatabase getDeletionThresholdDatabase() {
        if (deletionThresholdDatabase == null) {
            deletionThresholdDatabase = new DeletionThresholdDatabase(this);
        }
        return deletionThresholdDatabase;
    }

    public CategoryDatabase getCategoryDatabase() {
        if (categoryDatabase == null) {
            categoryDatabase = new CategoryDatabase(this);
        }
        return categoryDatabase;
    }

    public NoteListDatabase getMainNoteListDatabase() {
        if (mainNoteListDatabase == null) {
            mainNoteListDatabase = new MainNoteListDatabase(this);
        }
        return mainNoteListDatabase;
    }

    public NoteListDatabase getExpiredNoteListDatabase() {
        if (expiredNoteListDatabase == null) {
            expiredNoteListDatabase = new ExpiredNoteListDatabase(this);
        }
        return expiredNoteListDatabase;
    }


    //

    private static class MainNoteDatabase extends NoteDatabase {

        MainNoteDatabase(SQLiteOpenHelper helper) {
            super(helper);
        }

        @NonNull
        @Override
        public String getNoteTableName() {
            return MAIN_NOTE_TABLE_TABLE_NAME;
        }

    }

    private static class ExpiredNoteDatabase extends NoteDatabase {

        ExpiredNoteDatabase(SQLiteOpenHelper helper) {
            super(helper);
        }

        @NonNull
        @Override
        public String getNoteTableName() {
            return EXPIRED_NOTE_TABLE_TABLE_NAME;
        }

    }

    //

    public abstract static class NoteDatabase implements NoteSourceWriter, NoteSourceReader {

        public static final String NULL_VALUE = "NULL VAL";

        private SQLiteOpenHelper helper;

        private NoteDatabase(SQLiteOpenHelper helper) {
            this.helper = helper;
        }

        @NonNull
        public abstract String getNoteTableName();

        @Override
        public void clear() {
            SQLiteDatabase database = helper.getWritableDatabase();
            database.delete(getNoteTableName(), null, null);
            database.close();
        }

        /**
         * Adds note into database, setting the note's creation period to the time this method is invoked
         * @param noteBulilder
         * @return the constructed Note object
         */
        @NonNull
        @Override
        public Note addNote(@NonNull Note.Builder noteBulilder) {
            ContentValues noteValues = getContentValuesOfNote(noteBulilder);

            SQLiteDatabase database = helper.getWritableDatabase();
            long id = database.insertOrThrow(getNoteTableName(), null, noteValues);
            database.close();

            return noteBulilder.setUniqueId(String.valueOf(id)).constructNote();
        }

        private ContentValues getContentValuesOfNote(Note.Builder noteBuilder) {
            ContentValues noteValues = new ContentValues();
            noteValues.put(NOTE_TABLE_TITLE, noteBuilder.getTitle());
            noteValues.put(NOTE_TABLE_TEXT, noteBuilder.getText());
            noteValues.put(NOTE_TABLE_PRIORITY_PERIOD, getTimeInMilisOf(noteBuilder.getPriorityPeriod()));
            noteValues.put(NOTE_TABLE_PRIORITY_DAYS, DaysOfWeekParser.parseDaysOfWeek(noteBuilder.getPriorityDaysOfWeek()));
            noteValues.put(NOTE_TABLE_EXPIRATION_PERIOD, getTimeInMilisOf(noteBuilder.getExpirationPeriod()));
            noteValues.put(NOTE_TABLE_LAST_VISITED_PERIOD, getTimeInMilisOf(GregorianCalendar.getInstance()));
            noteValues.put(NOTE_TABLE_CATEGORY_ID, noteBuilder.getCategoryUniqueId());

            return noteValues;
        }

        private String getTimeInMilisOf(@Nullable Calendar time) {
            if (time == null) {
                return NULL_VALUE;
            } else {
                return String.valueOf(time.getTimeInMillis());
            }
        }

        @Override
        public void deleteNote(@NonNull String incId) {
            SQLiteDatabase database = helper.getWritableDatabase();
            database.delete(getNoteTableName(), NOTE_TABLE_INC_ID + " = ?",
                    new String[]{incId});
            database.close();
        }

        @Override
        public void updateNote(@NonNull String incId, @NonNull Note.Builder newNoteContents) {
            ContentValues updateVals = getContentValuesOfNote(newNoteContents);
            SQLiteDatabase database = helper.getWritableDatabase();
            database.update(getNoteTableName(), updateVals, NOTE_TABLE_INC_ID + " = ?",
                    new String[]{incId});
            database.close();
        }

        @NonNull
        @Override
        public Note[] getAllNotes() {
            SQLiteDatabase database = helper.getReadableDatabase();
            Cursor cursor = database.query(getNoteTableName(), null, null, null, null, null, null);

            List<Note> noteList = new ArrayList<>();

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                noteList.add(constructNoteFromValues(getValuesFromCursor(cursor)));
                cursor.moveToNext();
            }
            cursor.close();
            database.close();

            return noteList.toArray(new Note[0]);
        }

        private Note constructNoteFromValues(String[] values) {
            String incId = values[0];
            String title = values[1];
            String text = values[2];
            Calendar prioPeriod = getCalendarFromString(values[3]);
            DaysOfWeek[] prioDays = DaysOfWeekParser.parseStringToDaysOfWeek(values[4]);
            Calendar expiPeriod = getCalendarFromString(values[5]);
            Calendar lastVisitedPeriod = getCalendarFromString(values[6]);
            String categoryId = values[7];

            Note note = new Note.Builder()
                    .setUniqueId(incId)
                    .setTitle(title)
                    .setText(text)
                    .setPriorityPeriod(prioPeriod)
                    .setPriorityDaysOfWeek(prioDays)
                    .setExpirationPeriod(expiPeriod)
                    .setCategoryUniqueId(categoryId)
                    .constructNote();
            note.setCreationPeriod(lastVisitedPeriod);
            return note;
        }

        @Nullable
        private Calendar getCalendarFromString(String toParse) {
            if (toParse.equals(NULL_VALUE)) {
                return null;
            } else {
                Calendar time = GregorianCalendar.getInstance();
                time.setTimeInMillis(Long.parseLong(toParse));
                return time;
            }
        }

        private String[] getValuesFromCursor(Cursor cursor) {
            String[] values = new String[cursor.getColumnCount()];
            values[0] = String.valueOf(cursor.getInt(cursor.getColumnIndex(NOTE_TABLE_INC_ID)));
            values[1] = cursor.getString(cursor.getColumnIndex(NOTE_TABLE_TITLE));
            values[2] = cursor.getString(cursor.getColumnIndex(NOTE_TABLE_TEXT));
            values[3] = cursor.getString(cursor.getColumnIndex(NOTE_TABLE_PRIORITY_PERIOD));
            values[4] = cursor.getString(cursor.getColumnIndex(NOTE_TABLE_PRIORITY_DAYS));
            values[5] = cursor.getString(cursor.getColumnIndex(NOTE_TABLE_EXPIRATION_PERIOD));
            values[6] = cursor.getString(cursor.getColumnIndex(NOTE_TABLE_LAST_VISITED_PERIOD));
            values[7] = cursor.getString(cursor.getColumnIndex(NOTE_TABLE_CATEGORY_ID));

            return values;
        }

        @Nullable
        @Override
        public Note getNoteById(String incId) {
            SQLiteDatabase database = helper.getReadableDatabase();
            Cursor cursor = database.query(getNoteTableName(), null, NOTE_TABLE_INC_ID + " = ?", new String[]{incId},
                    null, null, null);

            Note returnVal = null;
            if (cursor.moveToFirst()) {
                returnVal = constructNoteFromValues(getValuesFromCursor(cursor));
            }

            cursor.close();
            database.close();
            return returnVal;
        }

        @NonNull
        @Override
        public Note[] deleteAllExpiredNotes(@NonNull Calendar time) {
            List<Note> toDeleteBucket = new ArrayList<>();
            for (Note note : getAllNotes()) {
                if (note.isExpiredAfterTime(time)) {
                    toDeleteBucket.add(note);
                }
            }

            for (Note note : toDeleteBucket) {
                deleteNote(note.getIncId());
            }

            return toDeleteBucket.toArray(new Note[0]);
        }

    }

    public static class SortDatabase {

        private SQLiteOpenHelper helper;

        SortDatabase(SQLiteOpenHelper helper) {
            this.helper = helper;
        }

        public ContentSorterAlgorithm getContentSortAlgorithm() {
            Cursor cursor = helper.getReadableDatabase().query(SORT_CHOSEN_TABLE_TABLE_NAME, new String[]{SORT_ORDINAL_CHOSEN_COL},
                    null, null, null, null, null);

            cursor.moveToFirst();
            int sortOrdinal = cursor.getInt(0);
            cursor.close();
            return ContentSorterAlgorithmImp.values()[sortOrdinal];
        }

        public void setContentSortAlgorithm(ContentSorterAlgorithm sortAlgorithm) {
            ContentValues vals = new ContentValues();
            vals.put(SORT_ORDINAL_CHOSEN_COL, sortAlgorithm.getOrdinalId());

            SQLiteDatabase database = helper.getWritableDatabase();
            database.update(SORT_CHOSEN_TABLE_TABLE_NAME, vals, null, null);
        }

    }

    public static class DeletionThresholdDatabase {

        private SQLiteOpenHelper helper;


        DeletionThresholdDatabase(SQLiteOpenHelper helper) {
            this.helper = helper;
        }

        public TimeThreshold getDeletionThreshold() {
            Cursor cursor = helper.getReadableDatabase().query(DELETION_THRESHOLD_TABLE_TABLE_NAME, null, null, null, null, null, null);
            cursor.moveToFirst();

            int number = cursor.getInt(cursor.getColumnIndex(DELETION_THRESHOLD_NUM));
            String rep = cursor.getString(cursor.getColumnIndex(DELETION_THRESHOLD_UNIT));

            cursor.close();
            return new TimeThreshold(TimeThreshold.UnitThreshold.getUnitThresholdWithRepresentation(rep), number);
        }

        public void setDeletionThreshold(TimeThreshold threshold) {
            SQLiteDatabase database = helper.getWritableDatabase();

            ContentValues vals = new ContentValues();
            vals.put(DELETION_THRESHOLD_NUM, threshold.getNumberThreshold());
            vals.put(DELETION_THRESHOLD_UNIT, threshold.getUnitThreshold().getStringRepresentation());

            database.update(DELETION_THRESHOLD_TABLE_TABLE_NAME, vals, null, null);
        }
    }

    public static class CategoryDatabase implements CategorySourceReader, CategorySourceWriter {

        private static final int NO_VALUE = -1;
        private SQLiteOpenHelper helper;

        private CategoryDatabase(SQLiteOpenHelper helper) {
            this.helper = helper;
        }

        @NonNull
        @Override
        public Category[] getAllCategories() {
            Cursor cursor = helper.getReadableDatabase().query(CATEGORY_TABLE_TABLE_NAME, null, null, null, null, null, null);
            cursor.moveToFirst();

            List<Category> categoryList = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                categoryList.add(getCategory(cursor));
                cursor.moveToNext();
            }
            cursor.close();

            return categoryList.toArray(new Category[0]);
        }

        private Category getCategory(Cursor cursor) {
            String displayName = getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_DISPLAY_NAME);
            String description = getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_DESCRIPTION);
            String uniqueId = String.valueOf(cursor.getInt(cursor.getColumnIndex(CATEGORY_TABLE_UNIQUE_ID)));
            String categoryName = getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_SELF_CATEGORY_NAME);
            long creationPeriod = getTimeMilisOf(getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_LAST_VISITED_PERIOD));
            long selfPriorityPeriod = getTimeMilisOf(getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_SELF_PRIORITY_PERIOD));
            DaysOfWeek[] selfPriorityDays = DaysOfWeekParser.parseStringToDaysOfWeek(getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_SELF_PRIORITY_DAYS));
            long selfExpirationPeriod = getTimeMilisOf(getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_SELF_EXPIRATION_PERIOD));
            boolean autoFillEnabled = cursor.getInt(cursor.getColumnIndex(CATEGORY_TABLE_AUTOFILL_ENABLED)) == 1;
            long autoFillPriorityPeriod = getTimeMilisOf(getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_AUTOFILL_PRIORITY_PERIOD));
            DaysOfWeek[] autoFillPriorityDays = DaysOfWeekParser.parseStringToDaysOfWeek(getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_AUTOFILL_PRIORITY_DAYS));
            long autoFillExpirationPeriod = getTimeMilisOf(getStringOfCursorAtColumn(cursor, CATEGORY_TABLE_AUTOFILL_EXPIRATION_PERIOD));
            int pictureResId = cursor.getInt(cursor.getColumnIndex(CATEGORY_TABLE_PICTURE_RES_ID));

            Calendar creationPeriodTime = getCalendarAtMilisTime(creationPeriod);
            Calendar selfPriorityPeriodTime = getCalendarAtMilisTime(selfPriorityPeriod);
            Calendar selfExpirationPeriodTime = getCalendarAtMilisTime(selfExpirationPeriod);
            Calendar autoFillPriorityPeriodTime = getCalendarAtMilisTime(autoFillPriorityPeriod);
            Calendar autoFillExpirationPeriodTime = getCalendarAtMilisTime(autoFillExpirationPeriod);

            Category.Builder builder = new Category.Builder(displayName);
            builder.setDescription(description)
                    .setCategoryUniqueId(categoryName)
                    .setUniqueId(uniqueId)
                    .setCreationPeriod(creationPeriodTime)
                    .setSelfPriorityPeriod(selfPriorityPeriodTime)
                    .setSelfPriorityDaysOfWeek(selfPriorityDays)
                    .setSelfDeletionPeriod(selfExpirationPeriodTime)
                    .setAutoFillEnabled(autoFillEnabled)
                    .setAutoFillPriorityPeriod(autoFillPriorityPeriodTime)
                    .setAutoFillExpirationPeriod(autoFillExpirationPeriodTime)
                    .setAutoFillPriorityDaysOfWeek(autoFillPriorityDays)
                    .setPictureResourceId(pictureResId);
            return builder.constructCategory();
        }

        private long getTimeMilisOf(String timeAsString) {
            if (timeAsString == null) {
                return NO_VALUE;
            } else {
                return Long.parseLong(timeAsString);
            }
        }

        private String getStringOfCursorAtColumn(Cursor cursor, String column) {
            return cursor.getString(cursor.getColumnIndex(column));
        }

        private Calendar getCalendarAtMilisTime(long milis) {
            if (milis != NO_VALUE) {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(milis);
                return calendar;
            } else {
                return null;
            }
        }

        @Nullable
        @Override
        public Category getCategoryByUniqueId(String uniqueId) {
            if (uniqueId != null) {
                Cursor cursor = helper.getReadableDatabase().query(CATEGORY_TABLE_TABLE_NAME, null,
                        CATEGORY_TABLE_UNIQUE_ID + " = ?", new String[]{uniqueId}, null, null, null);

                Category returnVal = null;
                if (cursor.moveToFirst()) {
                    returnVal = getCategory(cursor);
                }
                cursor.close();

                return returnVal;
            } else {
                return null;
            }
        }

        @Override
        public void clear() {
            SQLiteDatabase database = helper.getWritableDatabase();
            database.delete(CATEGORY_TABLE_TABLE_NAME, null, null);
            database.close();
        }

        @NonNull
        @Override
        public Category addCategory(@NonNull Category.Builder builder) {
            builder.setCreationPeriod(GregorianCalendar.getInstance());
            ContentValues values = getValuesOfCategory(builder);

            SQLiteDatabase database = helper.getWritableDatabase();
            long pos = database.insertOrThrow(CATEGORY_TABLE_TABLE_NAME, null, values);
            database.close();

            builder.setUniqueId(String.valueOf(pos));

            return builder.constructCategory();
        }

        /**
         * Does not include the builder's unique Id.
         * @param category
         * @return
         */
        private ContentValues getValuesOfCategory(Category.Builder category) {
            ContentValues values = new ContentValues();

            values.put(CATEGORY_TABLE_DISPLAY_NAME, category.getDisplayName());
            values.put(CATEGORY_TABLE_DESCRIPTION, category.getDescription());
            values.put(CATEGORY_TABLE_SELF_CATEGORY_NAME, category.getCategoryUniqueId());
            values.put(CATEGORY_TABLE_LAST_VISITED_PERIOD, getMilisOfCalendarAsString(category.getCreationPeriod()));
            values.put(CATEGORY_TABLE_SELF_PRIORITY_PERIOD, getMilisOfCalendarAsString(category.getPriorityPeriod()));
            values.put(CATEGORY_TABLE_SELF_PRIORITY_DAYS, DaysOfWeekParser.parseDaysOfWeek(category.getPriorityDaysOfWeek()));
            values.put(CATEGORY_TABLE_SELF_EXPIRATION_PERIOD, getMilisOfCalendarAsString(category.getSelfDeletionPeriod()));
            values.put(CATEGORY_TABLE_AUTOFILL_ENABLED, category.getAutoFillEnable() ? 1 : 0);
            values.put(CATEGORY_TABLE_AUTOFILL_PRIORITY_PERIOD, getMilisOfCalendarAsString(category.getAutoFillPriorityPeriod()));
            values.put(CATEGORY_TABLE_AUTOFILL_PRIORITY_DAYS, DaysOfWeekParser.parseDaysOfWeek(category.getAutoFillPriorityDaysOfWeek()));
            values.put(CATEGORY_TABLE_AUTOFILL_EXPIRATION_PERIOD, getMilisOfCalendarAsString(category.getAutoFillExpirationPeriod()));
            values.put(CATEGORY_TABLE_PICTURE_RES_ID, category.getPictureResourceId());

            return values;
        }

        private String getMilisOfCalendarAsString(Calendar calendar) {
            if (calendar != null) {
                return String.valueOf(calendar.getTimeInMillis());
            } else {
                return null;
            }
        }

        @Override
        public void deleteCategory(@NonNull String uniqueId) {
            SQLiteDatabase database = helper.getWritableDatabase();
            database.delete(CATEGORY_TABLE_TABLE_NAME, CATEGORY_TABLE_UNIQUE_ID + " = ?", new String[] {uniqueId});
            database.close();
        }

        @NonNull
        @Override
        public Category[] deleteAllExpiredCategory(@NonNull Calendar time) {
            List<Category> deleteBucket = new ArrayList<>();

            for (Category category: getAllCategories()) {
                if (category.isExpiredAfterTime(time)) {
                    deleteBucket.add(category);
                }
            }
            for (Category category: deleteBucket) {
                deleteCategory(category.getUniqueId());
            }

            return deleteBucket.toArray(new Category[0]);
        }

        @Override
        public void updateCategory(@NonNull String uniqueId, @NonNull Category.Builder builder) {
            builder.setCreationPeriod(GregorianCalendar.getInstance());
            ContentValues values = getValuesOfCategory(builder);

            SQLiteDatabase database = helper.getWritableDatabase();
            database.update(CATEGORY_TABLE_TABLE_NAME, values, CATEGORY_TABLE_UNIQUE_ID + " = ?", new String[] {uniqueId});
            database.close();
        }
    }

    private static class MainNoteListDatabase extends NoteListDatabase {

        MainNoteListDatabase(SQLiteOpenHelper helper) {
            super(helper, MAIN_NOTE_LIST_TABLE_TABLE_NAME);
        }

    }

    private static class ExpiredNoteListDatabase extends NoteListDatabase {

        ExpiredNoteListDatabase(SQLiteOpenHelper helper) {
            super(helper, EXPIRED_NOTE_LIST_TABLE_TABLE_NAME);
        }

    }

    public abstract static class NoteListDatabase implements NoteListSourceReader, NoteListSourceWriter {

        private SQLiteOpenHelper helper;
        private String tableName;

        private NoteListDatabase(SQLiteOpenHelper helper, String tableName) {
            this.helper = helper;
            this.tableName = tableName;
        }

        @NonNull
        @Override
        public NoteList[] getAllNoteList() {
            List<NoteList> noteLists = new ArrayList<>();
            Cursor cursor = helper.getReadableDatabase().query(tableName,
                    null, null, null, null, null, null);

            cursor.move(1);
            while (!cursor.isAfterLast()) {
                noteLists.add(getNoteListFromCursor(cursor));
                cursor.moveToNext();
            }
            cursor.close();

            return noteLists.toArray(new NoteList[0]);
        }

        private NoteList getNoteListFromCursor(Cursor cursor) {
            String displayName = getStringAtColumnName(cursor, NOTE_LIST_TABLE_TITLE);
            String subText = getStringAtColumnName(cursor, NOTE_LIST_TABLE_SUBTEXT);
            String uniqueId = String.valueOf(cursor.getInt(cursor.getColumnIndex(NOTE_LIST_TABLE_UNIQUE_ID)));
            String categoryUniqueId = getStringAtColumnName(cursor, NOTE_LIST_TABLE_CATEGORY_UNIQUE_ID);
            Calendar lastVisitedPeriod = CalendarValueParser.parseMilisAsStringIntoCalendar(
                    getStringAtColumnName(cursor, NOTE_LIST_TABLE_LAST_VISITED_PERIOD));
            Calendar priorityPeriod = CalendarValueParser.parseMilisAsStringIntoCalendar(
                    getStringAtColumnName(cursor, NOTE_LIST_TABLE_PRIORITY_PERIOD));
            Calendar expirationPeriod = CalendarValueParser.parseMilisAsStringIntoCalendar(
                    getStringAtColumnName(cursor, NOTE_LIST_TABLE_EXPIRATION_PERIOD));
            DaysOfWeek[] daysOfWeek = DaysOfWeekParser.parseStringToDaysOfWeek(
                    getStringAtColumnName(cursor, NOTE_LIST_TABLE_PRIORITY_DAYS_OF_WEEK));
            NoteList.EntryType type = NoteList.EntryType.getEntryTypeByInternalId(getStringAtColumnName(cursor, NOTE_LIST_TABLE_ENTRY_TYPE));
            List<NoteList.Entry<?>> entryList = getEntryList(getStringAtColumnName(cursor, NOTE_LIST_TABLE_ENTRIES));

            return new NoteList.Builder()
                    .setDisplayName(displayName)
                    .setSubText(subText)
                    .setUniqueId(uniqueId)
                    .setCategoryUniqueId(categoryUniqueId)
                    .setCreationPeriod(lastVisitedPeriod)
                    .setPriorityPeriod(priorityPeriod)
                    .setPriorityDaysOfWeek(daysOfWeek)
                    .setExpirationPeriod(expirationPeriod)
                    .setEntryType(type)
                    .setListEntries(entryList)
                    .constructNoteList();
        }

        private String getStringAtColumnName(Cursor cursor, String columnName) {
            return cursor.getString(cursor.getColumnIndex(columnName));
        }

        private List<NoteList.Entry<?>> getEntryList(String toParse) {
            DataListParser<NoteList.Entry<?>, NoteListEntryParser> parser = new DataListParser<>(new NoteListEntryParser());
            return parser.parseEncodingIntoData(toParse);
        }

        @Override
        public NoteList getNoteListByUniqueId(String uniqueId) {
            if (uniqueId == null) {
                return null;
            }

            Cursor cursor = helper.getReadableDatabase().query(tableName, null,
                    NOTE_LIST_TABLE_UNIQUE_ID + " = ?", new String[] {uniqueId}, null, null, null);

            NoteList noteList = null;
            if (cursor.moveToNext()) {
                noteList = getNoteListFromCursor(cursor);
            }
            cursor.close();

            return noteList;
        }

        @Override
        public void clear() {
            helper.getWritableDatabase().delete(tableName, null, null);
        }

        @NonNull
        @Override
        public NoteList addNoteList(@NonNull NoteList.Builder builder) {
            ContentValues values = getValuesOfNoteList(builder);

            long index = helper.getWritableDatabase().insertOrThrow(tableName, null, values);
            builder.setUniqueId(String.valueOf(index));

            return builder.constructNoteList();
        }

        private ContentValues getValuesOfNoteList(NoteList.Builder builder) {
            ContentValues values = new ContentValues();
            values.put(NOTE_LIST_TABLE_TITLE, builder.getDisplayName());
            values.put(NOTE_LIST_TABLE_SUBTEXT, builder.getSubText());
            values.put(NOTE_LIST_TABLE_CATEGORY_UNIQUE_ID, builder.getCategoryUniqueId());
            values.put(NOTE_LIST_TABLE_ENTRY_TYPE, builder.getEntryType().getInternalId());
            values.put(NOTE_LIST_TABLE_LAST_VISITED_PERIOD, CalendarValueParser.parseCalendarMilisToString(builder.getCreationPeriod()));
            values.put(NOTE_LIST_TABLE_PRIORITY_PERIOD, CalendarValueParser.parseCalendarMilisToString(builder.getPriorityPeriod()));
            values.put(NOTE_LIST_TABLE_PRIORITY_DAYS_OF_WEEK, DaysOfWeekParser.parseDaysOfWeek(builder.getPriorityDaysOfWeek()));
            values.put(NOTE_LIST_TABLE_EXPIRATION_PERIOD, CalendarValueParser.parseCalendarMilisToString(builder.getExpirationPeriod()));
            values.put(NOTE_LIST_TABLE_ENTRIES, new DataListParser<>(new NoteListEntryParser()).getEncodingOfData(builder.getListEntries()));

            return values;
        }

        @Override
        public void deleteNoteList(@NonNull String idOfListToDelete) {
            helper.getWritableDatabase().delete(tableName, NOTE_LIST_TABLE_UNIQUE_ID + " = ?",
                    new String[] {idOfListToDelete});
        }

        @NonNull
        @Override
        public NoteList[] deleteAllExpiredNoteList(@NonNull Calendar time) {
            List<NoteList> deleteBucket = new ArrayList<>();

            NoteList[] allNoteList = getAllNoteList();
            for (NoteList noteList: allNoteList) {
                if (noteList.isExpiredAfterTime(time)) {
                    deleteBucket.add(noteList);
                }
            }

            for (NoteList expiredNoteList: deleteBucket) {
                deleteNoteList(expiredNoteList.getUniqueId());
            }

            return deleteBucket.toArray(new NoteList[0]);
        }

        @Override
        public void updateNoteList(@NonNull String id, @NonNull NoteList.Builder builder) {
            builder.setCreationPeriod(GregorianCalendar.getInstance());

            ContentValues values = getValuesOfNoteList(builder);
            helper.getWritableDatabase().update(tableName, values,
                    NOTE_LIST_TABLE_UNIQUE_ID + " = ?", new String[] {id});
        }

    }

}
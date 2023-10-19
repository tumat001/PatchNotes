package com.example.patchnotes.database;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.NoteList;

import java.util.Calendar;

public interface NoteListSourceWriter {
    void clear();
    @NonNull NoteList addNoteList(@NonNull NoteList.Builder builder);
    void deleteNoteList(@NonNull String noteListId);
    @NonNull NoteList[] deleteAllExpiredNoteList(@NonNull Calendar time);
    void updateNoteList(@NonNull String noteListId, @NonNull NoteList.Builder builder);

}

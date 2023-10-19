package com.example.patchnotes.database;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.Note;

import java.util.Calendar;

public interface NoteSourceWriter {
    void clear();
    @NonNull Note addNote(@NonNull Note.Builder noteBuilder);
    void deleteNote(@NonNull String noteId);
    void updateNote(@NonNull String noteId, @NonNull Note.Builder newNoteContent);

    /**
     * Deletes all notes expired by the time provided. If the time provided is after the note's expiration period, then the note
     * in question is deleted.
     * @param calendar the time to compare to the note's expiration period.
     * @return all notes that are deleted. This provides a chance for recovery, if desired
     */
    @NonNull Note[] deleteAllExpiredNotes(@NonNull Calendar calendar);
}

package com.example.patchnotes.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentrelated.Note;

public interface NoteSourceReader {
    @NonNull Note[] getAllNotes();
    @Nullable Note getNoteById(String noteId);
}

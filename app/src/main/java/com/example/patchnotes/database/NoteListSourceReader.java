package com.example.patchnotes.database;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.NoteList;

public interface NoteListSourceReader {
    @NonNull NoteList[] getAllNoteList();
    NoteList getNoteListByUniqueId(String uniqueId);
}

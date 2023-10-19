package com.example.patchnotes.contentdatarelated;

import androidx.annotation.NonNull;

public interface ParsableData<T> {
    @NonNull String getEncodeTypeIdentifier();
}

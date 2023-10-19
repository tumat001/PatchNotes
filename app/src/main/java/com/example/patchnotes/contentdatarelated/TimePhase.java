package com.example.patchnotes.contentdatarelated;

import androidx.annotation.NonNull;

public enum TimePhase {
    BEFORE,
    DURING_DAY,
    AFTER;

    @NonNull
    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }
}
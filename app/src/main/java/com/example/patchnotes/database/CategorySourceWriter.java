package com.example.patchnotes.database;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.Category;

import java.util.Calendar;

public interface CategorySourceWriter {
    void clear();
    @NonNull Category addCategory(@NonNull Category.Builder categoryBuilder);
    void deleteCategory(@NonNull String uniqueId);
    @NonNull Category[] deleteAllExpiredCategory(@NonNull Calendar time);
    void updateCategory(@NonNull String uniqueId, @NonNull Category.Builder categoryBuilder);

}

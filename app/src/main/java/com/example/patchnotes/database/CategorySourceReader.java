package com.example.patchnotes.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentrelated.Category;

public interface CategorySourceReader {
    @NonNull Category[] getAllCategories();
    @Nullable Category getCategoryByUniqueId(String uniqueId);
}

package com.example.patchnotes.contentfilter;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.Content;

import java.io.Serializable;
import java.util.List;

public interface ContentFilterAlgorithm extends Serializable {
    @NonNull List<Content> getFilteredContents(@NonNull List<Content> toFilter);
    @NonNull String getFilterDescription();
    @NonNull String getFilterName();
}


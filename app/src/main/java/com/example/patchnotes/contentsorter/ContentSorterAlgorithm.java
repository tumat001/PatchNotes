package com.example.patchnotes.contentsorter;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.Content;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

public interface ContentSorterAlgorithm extends Serializable {
    @NonNull List<Content> getArrangedNonPriorityContent(@NonNull List<Content> contentToArrange, @NonNull Calendar currentTime);
    @NonNull List<Content> getArrangedPriorityContent(@NonNull List<Content> contentToArrange, @NonNull Calendar currentTime);
    int getOrdinalId();
}

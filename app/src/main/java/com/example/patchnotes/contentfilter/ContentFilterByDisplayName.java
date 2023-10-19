package com.example.patchnotes.contentfilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentrelated.Note;

import java.util.ArrayList;
import java.util.List;

//Display, meaning the toString() value of a note
public class ContentFilterByDisplayName implements ContentFilterAlgorithmRequiringText {

    private String displayFilter;

    /**
     * A class that filters content's {@link Note#toString()} display name by the given argument
     * @param displayFilter the filter argument
     */
    public ContentFilterByDisplayName(@Nullable String displayFilter) {
        this.displayFilter = displayFilter;
    }

    @Override
    public void setArgumentText(String text) {
        this.displayFilter = text;
    }

    @Override
    public String getArgumentText() {
        return displayFilter;
    }

    @NonNull
    @Override
    public List<Content> getFilteredContents(@NonNull List<Content> toFilter) {
        if (displayFilter != null) {
            List<Content> filteredContents = new ArrayList<>();
            for (Content content: toFilter) {
                if (displayFilter == null || content.getDisplayName().contains(displayFilter)) {
                    filteredContents.add(content);
                }
            }
            return filteredContents;
        } else {
            return toFilter;
        }
    }

    @NonNull
    @Override
    public String getFilterDescription() {
        return new StringBuilder()
                .append("Has display name containing \"")
                .append(displayFilter)
                .append("\"")
                .toString();
    }

    @NonNull
    @Override
    public String getFilterName() {
        return "Filter by display name";
    }

    @NonNull
    public String toString() {
        return getFilterName();
    }
}

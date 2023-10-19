package com.example.patchnotes.contentsorter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentrelated.Content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public enum ContentSorterAlgorithmImp implements ContentSorterAlgorithm {

    LAST_VISITED_FIRST(
            "Recently visited first",
            (content1, content2) -> content2.getCreationPeriod().compareTo(content1.getCreationPeriod())
    ),
    NAME(
            "Name",
            (content1, content2) -> content1.toString().compareTo(content2.toString())
    ),
    FIRST_TO_EXPIRE(
            "First to expire",
            (content1, content2) -> {
                if (content1.getExpirationPeriod() != null && content2.getExpirationPeriod() != null) {
                    return content1.getExpirationPeriod().compareTo(content2.getExpirationPeriod());
                } else if (content1.getExpirationPeriod() == null && content2.getExpirationPeriod() == null) {
                    return 0;
                } else if (content1.getExpirationPeriod() == null) {
                    return 1;
                } else {
                    return -1;
                }
            }
    );

    private String displayRepresentation;
    private Comparator<Content> sortType;

    ContentSorterAlgorithmImp(String displayRepresentation, Comparator<Content> sortType) {
        this.displayRepresentation = displayRepresentation;
        this.sortType = sortType;
    }

    @NonNull
    @Override
    public List<Content> getArrangedNonPriorityContent(@NonNull List<Content> toArrange, @NonNull Calendar currentTime) {
        List<Content> nonPriorityContent = new ArrayList<>();
        for (Content content: toArrange) {
            if (!content.isPriority(currentTime)) {
                nonPriorityContent.add(content);
            }
        }
        return getArrangedNotesByAlgorithmTypeAndCreationTime(nonPriorityContent);
    }

    @NonNull
    @Override
    public List<Content> getArrangedPriorityContent(@NonNull List<Content> toArrange, @NonNull Calendar currentTime) {
        List<Content> priorityContent = new ArrayList<>();
        for (Content content: toArrange) {
            if (content.isPriority(currentTime)) {
                priorityContent.add(content);
            }
        }
        return getArrangedNotesByAlgorithmTypeAndCreationTime(priorityContent);
    }

    private List<Content> getArrangedNotesByAlgorithmTypeAndCreationTime(List<Content> contentList) {
        Content[] returnVal = new Content[contentList.size()];
        for (int i = 0; i < returnVal.length; i++) {
            returnVal[i] = contentList.get(i);
        }
        Arrays.sort(returnVal, sortType);
        return Arrays.asList(returnVal);
    }

    @Override
    public int getOrdinalId() {
        return ordinal();
    }

    @NonNull
    public String getDisplayRepresentation() {
        return displayRepresentation;
    }

    @NonNull
    @Override
    public String toString() {
        return getDisplayRepresentation();
    }

    //

    @NonNull
    public static String[] getAllSorterImp() {
        String[] noteTypeName = new String[ContentSorterAlgorithmImp.values().length];
        for (int i = 0; i < noteTypeName.length; i++) {
            noteTypeName[i] = ContentSorterAlgorithmImp.values()[i].displayRepresentation;
        }
        return noteTypeName;
    }

    @Nullable
    public static ContentSorterAlgorithm getSorterImpByDisplayRepresentation(@NonNull String displayRepresentation) {
        for (ContentSorterAlgorithmImp imp: ContentSorterAlgorithmImp.values()) {
            if (imp.displayRepresentation.equalsIgnoreCase(displayRepresentation)) {
                return imp;
            }
        }
        return null;
    }
}

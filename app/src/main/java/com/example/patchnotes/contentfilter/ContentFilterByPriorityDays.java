package com.example.patchnotes.contentfilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.util.ArrayList;
import java.util.List;

public class ContentFilterByPriorityDays implements ContentFilterAlgorithmRequiringDaysOfWeek {

    private DaysOfWeek daysOfWeek[];

    public ContentFilterByPriorityDays(@Nullable DaysOfWeek[] daysOfWeekFilter) {
        this.daysOfWeek = daysOfWeekFilter;
    }

    @Override
    public void setArgumentDaysOfWeek(DaysOfWeek[] argumentDaysOfWeek) {
        this.daysOfWeek = argumentDaysOfWeek;
    }

    @Override
    public DaysOfWeek[] getArgumentDaysOfWeek() {
        return daysOfWeek;
    }

    @NonNull
    @Override
    public List<Content> getFilteredContents(@NonNull List<Content> toFilter) {
        if (daysOfWeek != null) {
            List<Content> listOfContent = new ArrayList<>();
            for (Content content: toFilter) {
                if (daysOfWeek != null) {
                    for (DaysOfWeek dayOfWeek : daysOfWeek) {
                        if (content.isPriorityInDay(dayOfWeek)) {
                            listOfContent.add(content);
                            break;
                        }
                    }
                } else {
                    listOfContent.add(content);
                }
            }

            return listOfContent;
        } else {
            return toFilter;
        }
    }

    @NonNull
    @Override
    public String getFilterDescription() {
        return "Has priority day in " + getDayList(daysOfWeek);
    }

    private String getDayList(DaysOfWeek[] daysOfWeek) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < daysOfWeek.length; i++) {
            builder.append(daysOfWeek[i].toString());
            if (i == daysOfWeek.length - 2) {
                builder.append(" or ");
            } else if (i != daysOfWeek.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    @NonNull
    @Override
    public String getFilterName() {
        return "Filter by notes containing priority day";
    }

    @NonNull
    @Override
    public String toString() {
        return getFilterName();
    }

}

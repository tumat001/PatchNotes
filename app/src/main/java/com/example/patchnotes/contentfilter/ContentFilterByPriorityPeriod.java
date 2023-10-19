package com.example.patchnotes.contentfilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentdatarelated.TimePhase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

public class ContentFilterByPriorityPeriod implements ContentFilterAlgorithmRequiringTimePeriodAndTimePhase {

    private Calendar timePeriod;
    private TimePhase timePhase;

    public ContentFilterByPriorityPeriod(@NonNull Calendar timePeriod, @Nullable TimePhase timePhase) {
        this.timePeriod = timePeriod;
        this.timePhase = timePhase;
    }

    @Override
    public void setArgumentTimePeriodAndTimePhase(@NonNull Calendar timePeriod, @Nullable TimePhase timePhase) {
        this.timePeriod = timePeriod;
        this.timePhase = timePhase;
    }

    @Override
    public Calendar getTimePeriod() {
        return timePeriod;
    }

    @Override
    public TimePhase getTimePhase() {
        return timePhase;
    }

    /**
     *
     * @param toFilter The note array to filter
     * @return <p>Array of notes filtered depending on dayPeriod and timePhase provided.</p>
     *         <p>If no dayPeriod is specified, then the argument is returned as is</p>
     *         <p>Note: filter only takes the day, month and year of provided day period in consideration, not its hours, minutes, and seconds</p>
     */
    @NonNull
    @Override
    public List<Content> getFilteredContents(@NonNull List<Content> toFilter) {
        if (timePeriod != null) {
            if (timePhase == TimePhase.BEFORE) {
                return getNotesBeforeTimePeriod(toFilter);
            } else if (timePhase == TimePhase.DURING_DAY) {
                return getNotesDuringDayPeriod(toFilter);
            } else if (timePhase == TimePhase.AFTER) {
                return getNotesAfterTimePeriod(toFilter);
            } else {
                return toFilter;
            }
        } else {
            return toFilter;
        }
    }

    private List<Content> getNotesBeforeTimePeriod(List<Content> toFilter) {
        List<Content> filteredContent = new ArrayList<>();
        for (Content content: toFilter) {
            if (content.getPriorityPeriod() != null && content.isPriorityAfterTime(timePeriod)) {
                filteredContent.add(content);
            }
        }

        return filteredContent;
    }

    private List<Content> getNotesDuringDayPeriod(List<Content> toFilter) {
        List<Content> filteredContent = new ArrayList<>();
        for (Content content: toFilter) {
            if (content.getPriorityPeriod() != null && noteIsOfSameYMDasDayPeriod(content)) {
                filteredContent.add(content);
            }
        }

        return filteredContent;
    }

    private boolean noteIsOfSameYMDasDayPeriod(Content content) {
        return content.getPriorityPeriod().get(Calendar.YEAR) == timePeriod.get(Calendar.YEAR)
                && content.getPriorityPeriod().get(Calendar.MONTH) == timePeriod.get(Calendar.MONTH)
                && content.getPriorityPeriod().get(Calendar.DAY_OF_MONTH) == timePeriod.get(Calendar.DAY_OF_MONTH);
    }

    private List<Content> getNotesAfterTimePeriod(List<Content> toFilter) {
        List<Content> filteredContent = new ArrayList<>();
        for (Content content: toFilter) {
            if (content.getPriorityPeriod() != null && !content.isPriorityAfterTime(timePeriod)) {
                filteredContent.add(content);
            }
        }

        return filteredContent;
    }

    @NonNull
    @Override
    public String getFilterDescription() {
        StringBuilder builder = new StringBuilder()
                .append("Has priority period ")
                .append(timePhase.toString())
                .append(" ")
                .append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.MONTH))).append("/")
                .append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.DAY_OF_MONTH))).append("/")
                .append(timePeriod.get(Calendar.YEAR));
        if (timePhase != TimePhase.DURING_DAY) {
            builder.append(" at ")
                    .append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.HOUR_OF_DAY))).append(":")
                    .append(getTimeTwoDigitDisplay(timePeriod.get(Calendar.MINUTE)));
        }

        return builder.toString();
    }

    private String getTimeTwoDigitDisplay(int time) {
        Formatter formatter = new Formatter();
        formatter.format("%02d", time);
        return formatter.toString();
    }

    @NonNull
    @Override
    public String getFilterName() {
        return "Filter by priority time period";
    }

    @NonNull
    @Override
    public String toString() {
        return getFilterName();
    }
}

package com.example.patchnotes.contentrelated;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.io.Serializable;
import java.util.Calendar;

abstract public class Content implements Serializable {

    protected static final int MAX_DISPLAY_NAME_LENGTH = 20;

    @NonNull public abstract Calendar getCreationPeriod();
    @NonNull public abstract String getDisplayName();
    @Nullable public abstract String getDescription();
    @Nullable public abstract Calendar getExpirationPeriod();
    @Nullable public abstract Calendar getPriorityPeriod();
    @Nullable public abstract DaysOfWeek[] getPriorityDaysOfWeek();
    @Nullable public abstract String getCategoryUniqueId();
    @NonNull public abstract String getUniqueId();
    public abstract boolean isEmpty();

    /**
     *
     * @param time the time and day that will be tested against this content's priority period AND priority day
     * @return <p>true if content's priority period is after the time period provided, or if the day of the time period given is
     * among one of this content's priority days. Returns false otherwise</p>
     */
    public boolean isPriority(Calendar time) {
        if (getPriorityPeriod() != null || getPriorityDaysOfWeek() != null) {
            if (isPriorityAfterTime(time)) {
                return true;
            } else if (getPriorityDaysOfWeek() != null) {
                for (DaysOfWeek day: getPriorityDaysOfWeek()) {
                    if (time.get(Calendar.DAY_OF_WEEK) == day.getCalendarConstantEquivalent()) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    /**
     *
     * @param time the time period that will be compared to this content's expiration time
     * @return true if argument calendar is after this content's expiration time. false otherwise
     */
    public boolean isExpiredAfterTime(Calendar time) {
        if (getExpirationPeriod() != null) {
            return time.after(getExpirationPeriod());
        } else {
            return false;
        }
    }

    /**
     *
     * @param daysOfWeek the day of the week that will be tested against this content's priority day ONLY
     * @return true if argument is among the content's priority days of week. false otherwise
     */
    public boolean isPriorityInDay(DaysOfWeek daysOfWeek) {
        if (getPriorityDaysOfWeek() != null) {
            for (DaysOfWeek day : getPriorityDaysOfWeek()) {
                if (day == daysOfWeek) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param time the time period that will be tested against this content's priority time ONLY
     * @return true if argument calendar is after this content's priority time. false otherwise
     */
    public boolean isPriorityAfterTime(Calendar time) {
        if (getPriorityPeriod() != null) {
            return time.after(getPriorityPeriod());
        } else {
            return false;
        }
    }
}

package com.example.patchnotes.database;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.GregorianCalendar;

class CalendarValueParser {

    private static final int NO_VALUE = -1;

    private CalendarValueParser() {

    }

    static String parseCalendarMilisToString(@Nullable Calendar calendar) {
        if (calendar == null) {
            return String.valueOf(NO_VALUE);
        } else {
            return String.valueOf(calendar.getTimeInMillis());
        }
    }

    @Nullable
    static Calendar parseMilisAsStringIntoCalendar(@Nullable String calendarMilisAsString) {
        if (calendarMilisAsString == null) {
            return null;
        } else {
            long milis = Long.parseLong(calendarMilisAsString);
            if (milis == -1) {
                return null;
            } else {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(milis);
                return calendar;
            }
        }
    }


}

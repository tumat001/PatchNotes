package com.example.patchnotes.contentdatarelated;

import java.io.Serializable;
import java.util.Calendar;

public enum DaysOfWeek implements Serializable {
    SUNDAY(Calendar.SUNDAY, "Su"),
    MONDAY(Calendar.MONDAY, "M"),
    TUESDAY(Calendar.TUESDAY, "Tu"),
    WEDNESDAY(Calendar.WEDNESDAY, "W"),
    THURSDAY(Calendar.THURSDAY, "Th"),
    FRIDAY(Calendar.FRIDAY, "F"),
    SATURDAY(Calendar.SATURDAY, "Sa");

    private int calendarConstantEquivalent;
    private String representation;

    DaysOfWeek(int calendarConstantEquivalent, String representation) {
        this.calendarConstantEquivalent = calendarConstantEquivalent;
        this.representation = representation;
    }

    public int getCalendarConstantEquivalent() {
        return calendarConstantEquivalent;
    }

    public String getRepresentation() {
        return representation;
    }

    @Override
    public String toString() {
        String name = this.name();
        String firstLetter = name.substring(0, 1);
        String restOfName = name.substring(1).toLowerCase();
        return firstLetter + restOfName;
    }
}
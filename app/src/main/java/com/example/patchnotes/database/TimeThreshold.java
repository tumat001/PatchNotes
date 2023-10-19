package com.example.patchnotes.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Calendar;

public class TimeThreshold implements Serializable {

    public static final int NEVER_DELETE = -1;

    public enum UnitThreshold implements Serializable {
        HOUR("H", Calendar.HOUR_OF_DAY),
        DAY("D", Calendar.DAY_OF_MONTH),
        MONTH("M", Calendar.MONTH),
        YEAR("Y", Calendar.YEAR);

        private String singleCharRep;
        private int calendarConstantEquivalent;

        UnitThreshold(String singleCharRep, int calendarConstantEquivalent) {
            this.singleCharRep = singleCharRep;
            this.calendarConstantEquivalent = calendarConstantEquivalent;
        }

        public String getStringRepresentation() {
            return singleCharRep;
        }

        public int getCalendarConstantEquivalent() {
            return calendarConstantEquivalent;
        }

        @Nullable
        public static UnitThreshold getUnitThresholdWithRepresentation(@NonNull String representation) {
            for (UnitThreshold u : values()) {
                if (u.singleCharRep.equalsIgnoreCase(representation)) {
                    return u;
                }
            }
            return null;
        }
    }

    private UnitThreshold unitThreshold;
    private int number;

    public TimeThreshold(UnitThreshold unit, int numberThreshold) {
        this.unitThreshold = unit;
        this.number = numberThreshold;
    }

    public UnitThreshold getUnitThreshold() {
        return unitThreshold;
    }

    /**
     * @return <p>the number of units</p>
     * -1 means note will never be deleted.
     * 0 means note will be deleted right after, regardless of unit.
     * x > 0 means note will be deleted after x units of time.
     */
    public int getNumberThreshold() {
        return number;
    }

}

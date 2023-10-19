package com.example.patchnotes.database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.util.ArrayList;
import java.util.List;

class DaysOfWeekParser {

    private DaysOfWeekParser() {

    }

    @NonNull
    static String parseDaysOfWeek(@Nullable DaysOfWeek[] daysOfWeeks) {
        if (daysOfWeeks != null) {
            StringBuilder builder = new StringBuilder();
            for (DaysOfWeek day : daysOfWeeks) {
                builder.append(day.getRepresentation());
            }
            return builder.toString();
        } else {
            return AppDatabase.NoteDatabase.NULL_VALUE;
        }
    }

    @Nullable
    static DaysOfWeek[] parseStringToDaysOfWeek(@NonNull String toParse) {
        if (!toParse.equals(AppDatabase.NoteDatabase.NULL_VALUE)) {
            List<DaysOfWeek> list = new ArrayList<>();
            for (DaysOfWeek daysOfWeek : DaysOfWeek.values()) {
                if (toParse.contains(daysOfWeek.getRepresentation())) {
                    list.add(daysOfWeek);
                }
            }
            return list.toArray(new DaysOfWeek[0]);
        } else {
            return null;
        }
    }

}

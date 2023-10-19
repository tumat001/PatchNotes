package com.example.patchnotes.contentfilter;

import com.example.patchnotes.contentdatarelated.DaysOfWeek;

public interface ContentFilterAlgorithmRequiringDaysOfWeek extends ContentFilterAlgorithm {
    void setArgumentDaysOfWeek(DaysOfWeek[] daysOfWeek);
    DaysOfWeek[] getArgumentDaysOfWeek();
}

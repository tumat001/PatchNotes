package com.example.patchnotes.contentfilter;

import com.example.patchnotes.contentdatarelated.TimePhase;

import java.util.Calendar;

public interface ContentFilterAlgorithmRequiringTimePeriodAndTimePhase extends ContentFilterAlgorithm {
    void setArgumentTimePeriodAndTimePhase(Calendar timePeriod, TimePhase timePhase);
    Calendar getTimePeriod();
    TimePhase getTimePhase();
}

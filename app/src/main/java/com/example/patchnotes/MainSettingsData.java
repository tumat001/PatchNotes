package com.example.patchnotes;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithm;
import com.example.patchnotes.contentsorter.ContentSorterAlgorithm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainSettingsData implements Serializable {

    private ContentSorterAlgorithm sorterAlgorithm;
    private ArrayList<ContentFilterAlgorithm> filterAlgorithms = new ArrayList<>();

    MainSettingsData(ContentSorterAlgorithm sorterAlgorithm, List<ContentFilterAlgorithm> filterAlgorithms) {
        this.sorterAlgorithm = sorterAlgorithm;
        this.filterAlgorithms.addAll(filterAlgorithms);
    }

    public ContentSorterAlgorithm getContentSorterAlgorithm() {
        return sorterAlgorithm;
    }

    public List<ContentFilterAlgorithm> getFilterAlgorithms() {
        return filterAlgorithms;
    }

}
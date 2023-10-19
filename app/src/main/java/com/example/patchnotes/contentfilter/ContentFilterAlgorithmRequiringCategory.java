package com.example.patchnotes.contentfilter;

import com.example.patchnotes.contentrelated.Category;

public interface ContentFilterAlgorithmRequiringCategory extends ContentFilterAlgorithm {
    void setArgumentCategory(Category category);
    Category getArgumentCategory();
}

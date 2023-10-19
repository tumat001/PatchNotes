package com.example.patchnotes.contentfilter;

public interface ContentFilterAlgorithmRequiringText extends ContentFilterAlgorithm {
    void setArgumentText(String text);
    String getArgumentText();
}

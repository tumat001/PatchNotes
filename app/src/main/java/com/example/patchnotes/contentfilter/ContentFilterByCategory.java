package com.example.patchnotes.contentfilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.contentrelated.Content;

import java.util.ArrayList;
import java.util.List;

public class ContentFilterByCategory implements ContentFilterAlgorithmRequiringCategory {

    private Category category;

    public ContentFilterByCategory(@Nullable Category category) {
        this.category = category;
    }

    @Override
    public void setArgumentCategory(Category category) {
        this.category = category;
    }

    @Override
    public Category getArgumentCategory() {
        return category;
    }

    @NonNull
    @Override
    public List<Content> getFilteredContents(List<Content> contentList) {
        List<Content> filteredList = new ArrayList<>();
        for (Content content: contentList) {
            if (content.getCategoryUniqueId() != null && content.getCategoryUniqueId().equals(category.getUniqueId())) {
                filteredList.add(content);
            }
        }
        return filteredList;
    }

    @NonNull
    @Override
    public String getFilterDescription() {
        return "Is in \"" + category.getDisplayName() + "\" category";
    }

    @NonNull
    @Override
    public String getFilterName() {
        return "Filter by category";
    }

}

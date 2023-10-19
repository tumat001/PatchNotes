package com.example.patchnotes;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.patchnotes.contentfilter.ContentFilterByCategory;
import com.example.patchnotes.contentrelated.Category;
import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.database.AppDatabase;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

public class CategoryListDisplayActivity extends AbstractContentListDisplayActivity {

    private AppDatabase appDatabase;
    private AppDatabase.CategoryDatabase categoryDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected AppDatabase getAppDatabase() {
        if (appDatabase == null) {
            appDatabase = new AppDatabase(this);
        }
        return appDatabase;
    }

    @NonNull
    private AppDatabase.CategoryDatabase getCategoryDatabase() {
        if (categoryDatabase == null) {
            categoryDatabase = getAppDatabase().getCategoryDatabase();
        }
        return categoryDatabase;
    }

    @Override
    protected void deleteAllExpiredContentOnStartUp(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getCategoryDatabase().deleteAllExpiredCategory(GregorianCalendar.getInstance());
        }
    }

    @Override
    protected List<Content> retrieveContentListFromSourceReader() {
        return Arrays.asList(getCategoryDatabase().getAllCategories());
    }

    @Override
    protected void deleteContent(@NonNull Content content) {
        getCategoryDatabase().deleteCategory(content.getUniqueId());
    }

    @Override
    public void onContentSelected(Content content) {
        Intent intent = new Intent(this, CategoryManagerActivity.class);
        intent.putExtra(CategoryManagerActivity.INTENT_CATEGORY_TO_MANAGE,
                content);
        startActivity(intent);
    }

    @Override
    public void onContentLongSelected(Content content) {
        Intent intent = new Intent(this, MainActivity.class);
        addFilterAlgorithmInList(new ContentFilterByCategory((Category) content));
        startActivity(intent);
    }

    @Override
    public boolean onNavigationMenuItemSelected(MenuItem item) {
        boolean successful = super.onNavigationMenuItemSelected(item);
        if (!successful) {
            if (item.getItemId() == R.id.cat_navItem_createCategory) {
                Intent intent = new Intent(this, CategoryManagerActivity.class);
                intent.putExtra(CategoryManagerActivity.INTENT_CATEGORY_TO_MANAGE,
                        new Category.Builder("").constructCategory());
                startActivity(intent);
            }
        }
        return false;
    }

    @Override
    protected void exitThisActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_category_display;
    }

    @Override
    protected int getToolbarId() {
        return R.id.categoryDisplay_toolbar;
    }

    @Override
    protected int getDrawerId() {
        return R.id.categoryDisplay_drawer;
    }

    @Override
    protected int getNavigationViewId() {
        return R.id.categoryDisplay_navRight;
    }

    @Override
    protected int getContentListFragmentContainer() {
        return R.id.categoryDisplay_categoryList;
    }

    @Override
    protected int getCreateContentTextViewWhenEmpty() {
        return R.id.categoryDisplay_createCategoryHint;
    }

    @Override
    protected int getToolbarMenuId() {
        return R.menu.main_toolbar_menu_items;
    }

    @Override
    protected int getNavigationMenuId() {
        return R.menu.category_display_nav_right;
    }

    @Override
    protected boolean ignoreFilterList() {
        return true;
    }

}


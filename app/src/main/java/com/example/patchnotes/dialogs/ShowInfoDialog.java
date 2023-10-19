package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class ShowInfoDialog extends DialogFragment {

    private static final String SAVED_TITLE = "savedTitle";
    private static final String SAVED_CONTENT = "savedContent";

    private String title, content;
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(SAVED_TITLE);
            content = savedInstanceState.getString(SAVED_CONTENT);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setPositiveButton("ok", null);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putString(SAVED_CONTENT, content);
        toSave.putString(SAVED_TITLE, title);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

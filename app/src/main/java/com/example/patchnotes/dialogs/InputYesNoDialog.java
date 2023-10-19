package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class InputYesNoDialog extends DialogFragment {

    public interface OnResponseListener {
        void onYesSelected(String tag);
        void onNoSelected(String tag);
    }

    private static final String SAVED_TAG = "savedTag";
    private static final String SAVED_TITLE = "savedTitle";
    private static final String SAVED_TEXT = "savedText";

    private static final String DIALOG_YES = "Yes";
    private static final String DIALOG_NO = "No";

    private String tag, title, text;
    private OnResponseListener responseListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            responseListener = (OnResponseListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity does not implement OnResponseListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadSavedAttributes(savedInstanceState);
        return constructThisDialog();
    }

    private void loadSavedAttributes(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.title = savedInstanceState.getString(SAVED_TITLE);
            this.text = savedInstanceState.getString(SAVED_TEXT);
            this.tag = savedInstanceState.getString(SAVED_TAG);
        }
    }

    private Dialog constructThisDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(text);
        builder.setPositiveButton(DIALOG_YES, (dI, which) -> {
            responseListener.onYesSelected(this.tag);
            dismiss();
        });
        builder.setNegativeButton(DIALOG_NO, (dI, which) -> {
            responseListener.onNoSelected(this.tag);
            dismiss();
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putString(SAVED_TITLE, title);
        toSave.putString(SAVED_TEXT, text);
        toSave.putString(SAVED_TAG, tag);
    }

    //

    public void setTag(@Nullable String tag) {
        this.tag = tag;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    public void setText(@Nullable String text) {
        this.text = text;
    }

}

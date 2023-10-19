package com.example.patchnotes.dialogs.manage_autovalue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.nio.BufferUnderflowException;

public class RemoveOrEditManageDialog extends DialogFragment implements DialogInterface.OnClickListener {

    public interface OnOptionSelectedListener {
        void onRemoveSelected(String tag);
        void onEditSelected(String tag);
    }

    private static final String SAVED_TAG = "savedTag";
    private static final String SAVED_TITLE = "savedTitle";
    private static final String SAVED_TO_MANAGE_TEXT = "savedToManageText";

    private OnOptionSelectedListener listener;
    private String tag, title, toManageText;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (OnOptionSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnOptionSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.tag = savedInstanceState.getString(SAVED_TAG);
            this.title = savedInstanceState.getString(SAVED_TITLE);
            this.toManageText = savedInstanceState.getString(SAVED_TO_MANAGE_TEXT);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(getChoices(), -1, this);
        builder.setTitle(title);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface i, int pos) {
        if (pos == 0) {
            listener.onRemoveSelected(tag);
        } else {
            listener.onEditSelected(tag);
        }
        dismiss();
    }

    private String[] getChoices() {
        return new String[] {
                "Remove " + toManageText,
                "Edit " + toManageText
        };
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putString(SAVED_TAG, tag);
        toSave.putString(SAVED_TITLE, title);
        toSave.putString(SAVED_TO_MANAGE_TEXT, toManageText);
    }

    //

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @param toManage the text to append to the two options that this dialog provides. The first option is "Remove ", while
     *                 the second one is "Edit ". The param text is then appended to the two options.
     */
    public void setSavedToManageText(String toManage) {
        this.toManageText = toManage;
    }

}

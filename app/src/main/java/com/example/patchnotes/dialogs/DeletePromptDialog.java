package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.R;

public class DeletePromptDialog extends DialogFragment {

    public interface OnDeletePressedListener {
        void onDeletePressed();
    }

    private OnDeletePressedListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnDeletePressedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnDeletePressedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.deleteDialog_promptMessage);
        builder.setPositiveButton(R.string.deleteDialog_deleteButton, (dialog, which) -> {
            listener.onDeletePressed();
            dismiss();
        });
        builder.setNegativeButton(R.string.deleteDialog_cancelButton, (i, j) -> {
            dismiss();
        });
        return builder.create();
    }



}

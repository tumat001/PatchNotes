package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.contentfilter.ContentFilterAlgorithmRequiringText;

public class InputTextDialog extends DialogFragment {

    private static final String SAVED_INPUT_TEXT = "savedInputText";
    private static final String SAVED_TEXT_CARAT_POS = "savedTextCaratPos";
    private static final String SAVED_FILTER_ALGORITHM = "savedFilterAlgorithm";

    private static final String DIALOG_TITLE = "Enter text to find";
    private static final String DIALOG_OK_TEXT = "Ok";
    private static final String DIALOG_CANCEL_TEXT = "Cancel";
    private static final String DIALOG_INPUT_HINT = "text...";
    private static final String DIALOG_EMPTY_INPUT_ERROR = "Please enter text to find";

    private FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener listener;
    private ContentFilterAlgorithmRequiringText filterAlgorithm;

    private EditText inputEditText;

    private Toast toastShown;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FilterAlgorithmPromptDialog.OnFilterAlgorithmSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnFilterAlgorithmSelectedListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inputEditText = new EditText(getContext());
        if (savedInstanceState != null) {
            inputEditText.setText(savedInstanceState.getString(SAVED_INPUT_TEXT));
            filterAlgorithm = (ContentFilterAlgorithmRequiringText) savedInstanceState.getSerializable(SAVED_FILTER_ALGORITHM);
        }

        inputEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        inputEditText.setHint(DIALOG_INPUT_HINT);

        FrameLayout custom = new FrameLayout(getContext());
        custom.addView(inputEditText, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        custom.setPaddingRelative(20, 0, 20, 0);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(custom);
        builder.setTitle(DIALOG_TITLE);
        builder.setPositiveButton(DIALOG_OK_TEXT, (dI, which) -> {
            okPressed();
        });
        builder.setNegativeButton(DIALOG_CANCEL_TEXT, (dI, which) -> {
            dismiss();
        });

        AlertDialog dialog = builder.create();
        if (savedInstanceState != null) {
            inputEditText.setSelection(savedInstanceState.getInt(SAVED_TEXT_CARAT_POS));
        }
        return dialog;
    }

    private void okPressed() {
        String input = inputEditText.getText().toString();
        if (!input.isEmpty()) {
            filterAlgorithm.setArgumentText(input);
            listener.onFilterAlgorithmSelected(filterAlgorithm);
            dismiss();
        } else {
            removeShowingToast();
            toastShown = Toast.makeText(getContext(), DIALOG_EMPTY_INPUT_ERROR, Toast.LENGTH_SHORT);
            toastShown.show();
        }
    }

    private void removeShowingToast() {
        if (toastShown != null) {
            toastShown.cancel();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putString(SAVED_INPUT_TEXT, inputEditText.getText().toString());
        toSave.putSerializable(SAVED_FILTER_ALGORITHM, filterAlgorithm);
        toSave.putInt(SAVED_TEXT_CARAT_POS, inputEditText.getSelectionStart());
    }

    public void setContentFilterAlgorithmRequiringText(@NonNull ContentFilterAlgorithmRequiringText filterAlgorithm) {
        this.filterAlgorithm = filterAlgorithm;
    }
}
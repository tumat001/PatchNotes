package com.example.patchnotes.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.patchnotes.R;

import java.util.ArrayList;
import java.util.List;


public class ListChooserDialog<T> extends DialogFragment {

    private static final String SAVED_TAG = "savedTag";
    private static final String SAVED_TITLE = "savedTitle";
    private static final String SAVED_MESSAGE = "savedMessage";
    private static final String SAVED_ENTRY_LIST = "savedEntryList";
    private static final String SAVED_PICTURE_RES_ID_LIST = "savedPictureResIdList";
    private static final String SAVED_HIGHLIGHTED_ENTRIES = "savedHighlightedEntries";

    public static final int NO_IMAGE = -1;

    public interface OnListEntryChosenListener {
        void onListEntryChosen(Object entryChosen, int index, String tag);
    }

    private OnListEntryChosenListener listener;
    private String tag, title, message;
    private List<T> entryList = new ArrayList<>();
    private List<T> highlightedEntries = new ArrayList<>();
    private List<Integer> pictureResourceIdList = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnListEntryChosenListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnListEntryChosenListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        restoreInstanceState(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(createCustomListView());
        builder.setTitle(title);
        builder.setMessage(message);

        return builder.create();
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            tag = savedInstanceState.getString(SAVED_TAG);
            title = savedInstanceState.getString(SAVED_TITLE);
            message = savedInstanceState.getString(SAVED_MESSAGE);
            entryList.addAll((List<T>) savedInstanceState.getSerializable(SAVED_ENTRY_LIST));
            pictureResourceIdList.addAll((List<Integer>) savedInstanceState.getSerializable(SAVED_PICTURE_RES_ID_LIST));
            highlightedEntries.addAll((List<T>) savedInstanceState.getSerializable(SAVED_HIGHLIGHTED_ENTRIES));
        }
    }

    private ListView createCustomListView() {
        ListView listView = (ListView) View.inflate(getContext(), R.layout.generic_list_view, null);
        listView.setAdapter(new PictureDescriptionListAdapter<>(getContext(), pictureResourceIdList, entryList, highlightedEntries));
        listView.setOnItemClickListener(((parent, view, position, id) -> {
            dismiss();
            listener.onListEntryChosen(entryList.get(position), position, tag);
        }));
        listView.setPaddingRelative(0, 0, 0, 15);

        return listView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putSerializable(SAVED_ENTRY_LIST, new ArrayList<>(entryList));
        toSave.putSerializable(SAVED_PICTURE_RES_ID_LIST, new ArrayList<>(pictureResourceIdList));
        toSave.putSerializable(SAVED_HIGHLIGHTED_ENTRIES, new ArrayList<>(highlightedEntries));
        toSave.putString(SAVED_TAG, tag);
        toSave.putString(SAVED_TITLE, title);
        toSave.putString(SAVED_MESSAGE, message);
    }

    //

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setEntryList(List<T> entryList) {
        this.entryList.clear();
        this.entryList.addAll(entryList);
    }

    public void setPictureResourceIdList(List<Integer> picList) {
        this.pictureResourceIdList.clear();
        this.pictureResourceIdList.addAll(picList);
    }

    public void setHighlightedEntries(List<T> highlightedEntries) {
        this.highlightedEntries.clear();
        this.highlightedEntries.addAll(highlightedEntries);
    }

    //

    private static class PictureDescriptionListAdapter<T> implements ListAdapter {

        private List<Integer> picResList = new ArrayList<>();
        private List<T> entryList = new ArrayList<>();
        private List<T> highlightedEntries = new ArrayList<>();
        private Context context;

        PictureDescriptionListAdapter(Context context, List<Integer> picResList, List<T> entryList, List<T> highlightedEntries) {
            this.context = context;
            this.picResList.addAll(picResList);
            this.entryList.addAll(entryList);
            this.highlightedEntries.addAll(highlightedEntries);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return entryList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View entryItem = constructView(convertView, position);
            entryItem = convertViewToAppropriateView(position, entryItem);

            return entryItem;
        }

        private View constructView(View convertView, int pos) {
            if (convertView == null) {
                convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.dialog_entry_item_list, null);
            }
            convertView = highlightViewIfEntryIsHighlighted(convertView, pos);

            return convertView;
        }

        private View highlightViewIfEntryIsHighlighted(View toHighlightView, int pos) {
            T entry = entryList.get(pos);

            if (highlightedEntries.contains(entry)) {
                toHighlightView.setBackgroundResource(R.color.listChooserDialogHighlightColor);
            } else {
                toHighlightView.setBackgroundResource(R.color.generic_transparent);
            }

            return toHighlightView;
        }

        private View convertViewToAppropriateView(int position, View entryItem) {
            TextView descView = entryItem.findViewById(R.id.dialog_entry_item_description);
            ImageView imageView = entryItem.findViewById(R.id.dialog_entry_item_picture);

            descView.setText(entryList.get(position).toString());
            setImageViewResourceIfValid(imageView ,position);

            return entryItem;
        }

        private void setImageViewResourceIfValid(ImageView view, int position) {
            try {
                int res = picResList.get(position);
                if (res != NO_IMAGE) {
                    view.setImageResource(picResList.get(position));
                    view.setVisibility(View.VISIBLE);
                } else {
                    view.setVisibility(View.INVISIBLE);
                }
            } catch (IndexOutOfBoundsException e) {
                view.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

}
package com.example.patchnotes.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.patchnotes.R;
import com.example.patchnotes.contentrelated.NoteList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteListEntryManager extends Fragment implements OnEntryLongClickedListener {

    public interface OnToggleMultipleSelectionListener {
        void onEnteringMultipleSelectionMode();
        void onLeavingMultipleSelectionMode();
    }

    private static final String SAVED_HIGHLIGHTED_ENTRY_POS_LIST = "savedHighlightedNoteEntryPosList";
    private static final String SAVED_IS_IN_MULTIPLE_SELECTION_MODE = "savedIsInMultipleSelectionMode";

    private List<NoteList.Entry<?>> noteEntryList = new ArrayList<>();
    private NoteList.EntryType entryType;
    private Set<Integer> highlightedEntryPos = new HashSet<>();
    private boolean isInMultipleSelectionMode;
    private OnToggleMultipleSelectionListener toggleListener;

    private ListView listView;

    private EntryAdapter currentAdapter;

    public NoteListEntryManager() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            toggleListener = (OnToggleMultipleSelectionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity must implement OnToggleMultipleSelectionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        restoreInstanceState(savedInstanceState);
        return inflater.inflate(R.layout.fragment_note_list_entry_manager, container, false);
    }

    @SuppressWarnings("unchecked")
    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            highlightedEntryPos.addAll((HashSet<Integer>) savedInstanceState.getSerializable(SAVED_HIGHLIGHTED_ENTRY_POS_LIST));
            isInMultipleSelectionMode = savedInstanceState.getBoolean(SAVED_IS_IN_MULTIPLE_SELECTION_MODE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        listView = view.findViewById(R.id.noteListEntryManager_listView);
        buildAdapterAndAttachToList(listView, entryType, noteEntryList, highlightedEntryPos, isInMultipleSelectionMode);
    }

    private void buildAdapterAndAttachToList(@NonNull ListView listView, @NonNull NoteList.EntryType type,
                                             List<NoteList.Entry<?>> entries, @NonNull Set<Integer> highlightedEntryPos,
                                             boolean isInMultipleSelectionMode) {
        currentAdapter = null;
        if (type == NoteList.EntryType.BULLET_ENTRY) {
            currentAdapter = new BulletEntryAdapter(getContext(), this, entries,
                    listView, highlightedEntryPos, isInMultipleSelectionMode);
        } else if (type == NoteList.EntryType.CHECK_ENTRY) {
            currentAdapter = new CheckEntryAdapter(getContext(), this, entries,
                    listView, highlightedEntryPos, isInMultipleSelectionMode);
        } else if (type == NoteList.EntryType.NUMBER_ENTRY) {
            currentAdapter = new NumberEntryAdapter(getContext(), this, entries,
                    listView, highlightedEntryPos, isInMultipleSelectionMode);
        }

        listView.setAdapter(currentAdapter);
    }

    @Override
    public void onEntryLongClicked(int pos) {
        Set<Integer> newHighlighPosEntry = new HashSet<>();
        newHighlighPosEntry.add(pos);

        setHighlightedEntryPos(newHighlighPosEntry);
        setNoteEntryList(currentAdapter.getNoteEntryList());
        setIsInMultipleSelectionMode(true);

        refreshManager();
        toggleListener.onEnteringMultipleSelectionMode();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle toSave) {
        toSave.putSerializable(SAVED_HIGHLIGHTED_ENTRY_POS_LIST, new HashSet<>(currentAdapter.getHighlightedEntries()));
        toSave.putBoolean(SAVED_IS_IN_MULTIPLE_SELECTION_MODE, currentAdapter.isInSelectMultipleMode);
    }

    //

    public void setNoteEntryList(@NonNull List<NoteList.Entry<?>> entryList) {
        noteEntryList.clear();
        noteEntryList.addAll(entryList);
    }

    public void setNoteEntryType(@NonNull NoteList.EntryType entryType) {
        NoteList.EntryType originalType = this.entryType;
        this.entryType = entryType;

        if (listView != null) {
            if (currentAdapter != null && !entryType.equals(originalType)) {
                NoteList.Builder builder = new NoteList.Builder();
                builder.setListEntries(currentAdapter.getNoteEntryList());
                builder.setEntryType(entryType);

                setNoteEntryList(builder.getListEntries());
            }
        }

    }

    public NoteList.EntryType getEntryType() {
        return entryType;
    }

    public void setHighlightedEntryPos(@NonNull Set<Integer> highlightedEntryPos) {
        this.highlightedEntryPos.clear();
        this.highlightedEntryPos.addAll(highlightedEntryPos);
    }

    public Set<Integer> getHighlightedEntryPos() {
        return currentAdapter.getHighlightedEntries();
    }

    public boolean isInMultipleSelectionMode() {
        return currentAdapter.isInSelectMultipleMode;
    }

    public void setIsInMultipleSelectionMode(boolean isInMultipleSelectionMode) {
        this.isInMultipleSelectionMode = isInMultipleSelectionMode;
        if (!isInMultipleSelectionMode) {
            setHighlightedEntryPos(new HashSet<>());
        }
    }

    public void refreshManager() {
        if (listView != null) {
            buildAdapterAndAttachToList(listView, entryType, noteEntryList,
                    highlightedEntryPos, isInMultipleSelectionMode);
        }
    }

    @NonNull
    public List<NoteList.Entry<?>> getCopyOfNoteEntryList() {
        return new ArrayList<>(currentAdapter.getNoteEntryList());
    }

    @NonNull
    public Set<Integer> getCopyOfHighlightedNoteEntryPosition() {
        return new HashSet<>(currentAdapter.getHighlightedEntries());
    }

    public void deleteEntriesAt(int[] indices) {
        Arrays.sort(indices);
        List<NoteList.Entry<?>> entryList = new ArrayList<>(currentAdapter.getNoteEntryList());
        for (int i = 0; i < indices.length; i++) {
            entryList.remove(indices[i] - i);
        }

        setNoteEntryList(entryList);
        setHighlightedEntryPos(new HashSet<>());
        refreshManager();
    }

    //

    abstract private static class EntryAdapter extends BaseAdapter {

        private static final int ENTRY_LIST_ITEM_TYPE = 0;
        private static final int ADD_ENTRY_LIST = 1;

        private Set<Integer> highlightedEntries = new HashSet<>();
        private boolean isInSelectMultipleMode;
        private Context context;
        private int layoutRes;
        private OnEntryLongClickedListener longClickedListener;

        EntryAdapter(@NonNull Context context, OnEntryLongClickedListener listener, @LayoutRes int layoutRes,
                     boolean isInSelectMultipleMode, Set<Integer> initiallyHighlightedEntryIndices) {
            this.context = context;
            this.layoutRes = layoutRes;
            this.isInSelectMultipleMode = isInSelectMultipleMode;
            this.highlightedEntries.addAll(initiallyHighlightedEntryIndices);
            this.longClickedListener = listener;
        }

        @NonNull
        abstract public List<NoteList.Entry<?>> getNoteEntryList();

        public Set<Integer> getHighlightedEntries() {
            return highlightedEntries;
        }

        public boolean isInSelectMultipleMode() {
            return isInSelectMultipleMode;
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
            if (!isInSelectMultipleMode) {
                return getNoteEntryList().size() + 1; //the add button at the bottom of the list
            } else {
                return getNoteEntryList().size();
            }
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
            return true;
        }

        @CallSuper
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (isAnEntryListItem(position)) {
                convertView = constructEntryListView(position, convertView, layoutRes);
            } else {
                convertView = constructAddNewEntryView(convertView);
            }

            return convertView;
        }

        boolean isAnEntryListItem(int position) {
            return position < getNoteEntryList().size();
        }

        @SuppressLint("CutPasteId")
        protected View constructEntryListView(int pos, View entryListView, int layout) {
            int initialSelection = -1;
            if (isNotAnEntryListView(entryListView)) {
                entryListView = View.inflate(context, layout, null);
            } else {
                EditText textArea = entryListView.
                        findViewById(R.id.noteListEntry_genericTextArea).findViewById(R.id.noteListEntry_mainText);

                String initialText = textArea.getText().toString();
                String recordedEntryText = getNoteEntryList().get(pos).getEntryText();
                if (initialText.equals(recordedEntryText)) {
                    initialSelection = textArea.getSelectionStart();

                } else {
                    initialSelection = recordedEntryText.length();
                }
            }

            EditText editView = entryListView.findViewById(R.id.noteListEntry_genericTextArea).findViewById(R.id.noteListEntry_mainText);

            if (!isInSelectMultipleMode) {
                entryListView.setOnLongClickListener(tV -> {
                    longClickedListener.onEntryLongClicked(pos);
                    return true;
                });

                editView.setOnLongClickListener(tV -> {
                    longClickedListener.onEntryLongClicked(pos);
                    return true;
                });

                editView.setFocusable(true);
            } else {
                if (highlightedEntries.contains(pos)) {
                    entryListView.setBackgroundResource(R.color.listChooserDialogHighlightColor);
                } else {
                    entryListView.setBackgroundResource(R.color.generic_transparent);
                }

                entryListView.setOnClickListener(tV -> updateHighlightedEntriesAndUpdateSelfDisplay(tV, pos));

                View finalEntryListView = entryListView;
                editView.setOnClickListener(tV -> updateHighlightedEntriesAndUpdateSelfDisplay(finalEntryListView, pos));

                editView.setFocusable(false);
            }

            editView.removeTextChangedListener(new TextPosWatcher(0));
            editView.setText(getNoteEntryList().get(pos).getEntryText());
            editView.addTextChangedListener(new TextPosWatcher(pos));
            if (initialSelection != -1) {
                editView.setSelection(initialSelection);
            }

            return entryListView;
        }

        private boolean isNotAnEntryListView(View view) {
            if (view != null) {
                View genericTextArea = view.findViewById(R.id.noteListEntry_genericTextArea);
                return genericTextArea == null;
            } else {
                return true;
            }
        }

        private void updateHighlightedEntriesAndUpdateSelfDisplay(View view, int pos) {
            if (highlightedEntries.contains(pos)) {
                highlightedEntries.remove(pos);
                view.setBackgroundResource(R.color.generic_transparent);
            } else {
                highlightedEntries.add(pos);
                view.setBackgroundResource(R.color.listChooserDialogHighlightColor);
            }
        }


        protected View constructAddNewEntryView(View addEntryView) {
            if (isNotAnAddNewEntryView(addEntryView)) {
                addEntryView = View.inflate(context, R.layout.note_list_entry_add_new_entry, null);
                addEntryView.setOnClickListener(triggerView -> onAddNewEntryPressed());
            }

            return addEntryView;
        }

        abstract void onEntryTextUpdated(int pos, String newText);

        private boolean isNotAnAddNewEntryView(View view) {
            if (view != null) {
                View genericTextArea = view.findViewById(R.id.noteListEntry_genericTextArea);
                return genericTextArea == null;
            } else {
                return true;
            }
        }

        abstract void onAddNewEntryPressed();


        @Override
        public int getItemViewType(int position) {
            if (isAnEntryListItem(position)) {
                return ENTRY_LIST_ITEM_TYPE;
            } else {
                return ADD_ENTRY_LIST;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        private class TextPosWatcher implements TextWatcher {

            private int pos;

            TextPosWatcher(int pos) {
                this.pos = pos;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                onEntryTextUpdated(pos, s.toString());
            }

            @Override
            public boolean equals(Object o) {
                return o instanceof TextPosWatcher;
            }

            @Override
            public int hashCode() {
                return 10101010;
            }
        }

    }

    private static class BulletEntryAdapter extends EntryAdapter {

        private List<NoteList.Entry<?>> noteEntryList = new ArrayList<>();
        private ListView listView;

        BulletEntryAdapter(@NonNull Context context, @NonNull OnEntryLongClickedListener listener,
                           @NonNull List<NoteList.Entry<?>> noteEntryList, @NonNull ListView listView,
                           @NonNull Set<Integer> highlightedEntryPos, boolean isInMultipleSelectionMode) {
            super(context, listener, R.layout.note_list_entry_picture, isInMultipleSelectionMode, highlightedEntryPos);
            this.noteEntryList.addAll(noteEntryList);
            this.listView = listView;
        }

        @NonNull
        @Override
        public List<NoteList.Entry<?>> getNoteEntryList() {
            return noteEntryList;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            View entryView = super.getView(pos, view, parent);

            ImageView imageView = entryView.findViewById(R.id.noteListEntry_picture);
            if (imageView != null) {
                imageView.setImageResource(R.drawable.baseline_fiber_circle_shaded_black_48dp);
            }

            return entryView;
        }

        @Override
        void onAddNewEntryPressed() {
            noteEntryList.add(new NoteList.BulletEntry("", null));

            listView.invalidateViews();
            notifyDataSetChanged();
        }

        @Override
        public void onEntryTextUpdated(int pos, String newText) {
            noteEntryList.set(pos, new NoteList.BulletEntry(newText, null));
            notifyDataSetChanged();
        }

    }

    private static class CheckEntryAdapter extends EntryAdapter {

        private List<NoteList.Entry<?>> noteEntryList = new ArrayList<>();
        private ListView listView;

        CheckEntryAdapter(@NonNull Context context, @NonNull OnEntryLongClickedListener listener,
                          @NonNull List<? extends NoteList.Entry<?>> noteEntryList,
                          @NonNull ListView listView, @NonNull Set<Integer> highlighetedEntryPos, boolean isInMultipleSelectionMode) {
            super(context, listener, R.layout.note_list_entry_checkbox, isInMultipleSelectionMode, highlighetedEntryPos);
            this.noteEntryList.addAll(noteEntryList);
            this.listView = listView;
        }

        @NonNull
        @Override
        public List<NoteList.Entry<?>> getNoteEntryList() {
            return noteEntryList;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            View entryView = super.getView(pos, view, parent);

            CheckBox checkBox = entryView.findViewById(R.id.noteListEntry_checkBox);
            if (checkBox != null) {
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked((Boolean) noteEntryList.get(pos).getMetadata());
                checkBox.setOnCheckedChangeListener((triggerView, isChecked) -> {
                    noteEntryList.set(pos, new NoteList.CheckEntry(noteEntryList.get(pos).getEntryText(), isChecked));
                });
            }
            return entryView;
        }

        @Override
        void onAddNewEntryPressed() {
            noteEntryList.add(new NoteList.CheckEntry("", false));

            listView.invalidateViews();
            notifyDataSetChanged();
        }

        @Override
        public void onEntryTextUpdated(int pos, String newText) {
            noteEntryList.set(pos, new NoteList.CheckEntry(newText, (Boolean) noteEntryList.get(pos).getMetadata()));
            notifyDataSetChanged();
        }

    }

    private static class NumberEntryAdapter extends EntryAdapter {

        private List<NoteList.Entry<?>> noteEntryList = new ArrayList<>();
        private ListView listView;

        NumberEntryAdapter(@NonNull Context context, @NonNull OnEntryLongClickedListener listener,
                           @NonNull List<NoteList.Entry<?>> noteEntryList,
                           @NonNull ListView listView, @NonNull Set<Integer> highlightedEntryPos, boolean isInMultipleSelectionMode) {
            super(context, listener, R.layout.note_list_entry_text_at_left, isInMultipleSelectionMode, highlightedEntryPos);
            this.noteEntryList.addAll(noteEntryList);
            this.listView = listView;
        }

        @NonNull
        @Override
        public List<NoteList.Entry<?>> getNoteEntryList() {
            return noteEntryList;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {
            View entryView = super.getView(pos, view, parent);

            TextView textView = entryView.findViewById(R.id.noteListEntry_leftText);
            if (textView != null) {
                String leftText = (pos + 1) + ")";
                textView.setText(leftText);
            }

            return entryView;
        }

        @Override
        public void onEntryTextUpdated(int pos, String newText) {
            noteEntryList.set(pos, new NoteList.NumberEntry(newText, pos));
            notifyDataSetChanged();
        }

        @Override
        public void onAddNewEntryPressed() {
            noteEntryList.add(new NoteList.NumberEntry("", noteEntryList.size() + 1));

            listView.invalidateViews();
            notifyDataSetChanged();
        }

    }
}

interface OnEntryLongClickedListener {
    void onEntryLongClicked(int pos);
}

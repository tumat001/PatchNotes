package com.example.patchnotes.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.patchnotes.R;
import com.example.patchnotes.contentrelated.Content;
import com.example.patchnotes.contentrelated.Note;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * <p>A fragment that is responsible for displaying Content in a list, as well as notifying the parent activity
 * of any selection to it's list entries. Parent activity must implement ContentSelectedListener.</p>
 * <p>Call this fragment's {@link #setContentList} to make this display the arguments, while
 * calling {@link #setTitle(String)} sets this fragment's header to the argument</p>
 *
 */
public class ContentListDisplayer extends Fragment {

    private static final String SAVED_TITLE = "savedTitle";
    private static final String SAVED_CONTENT_LIST = "savedContentList";
    private static final String SAVED_BACKGROUND_COLOR = "savedBackgroundColor";
    private static final String SAVED_TIME_PERIOD = "savedTimePeriod";
    private static final String SAVED_SELECTION_MODE = "savedSelectionMode";
    private static final String SAVED_SELECTED_CONTENT = "savedSelectedContent";
    private static final String SAVED_HIGHLIGHTED_CONTENT = "savedHighlightedContent";
    private static final String SAVED_HIGHLIGHT_COLOR = "savedHighlightColor";

    private ContentSelectedListener listener;

    private String initialTitle = null;
    private TextView titleDisplayView;
    private int initialVisibility = View.VISIBLE;
    private int initialBackgroundColor = -1;
    private Calendar timePeriod;

    private ListView contentListDisplayView;
    private ArrayList<Content> contentList = new ArrayList<>();
    private ArrayList<Content> selectedContentInList = new ArrayList<>();
    private ArrayList<Content> highlightedContent = new ArrayList<>();
    @ColorInt private int contentHighlightColor;

    private boolean isInSingleSelectionMode = true;

    public interface ContentSelectedListener {
        void onContentSelected(Content contentSelected);
        void onContentLongSelected(Content contentSelected);
    }

    public ContentListDisplayer() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ContentSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent activity does not implement ContentSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note_list_displayer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        initializeTimePeriod(savedInstanceState);
        initializeTitleDisplayView(savedInstanceState);
        initializeSelectionMode(savedInstanceState);
        initializeHighlightedContent(savedInstanceState);
        initializeContentDisplayList(savedInstanceState);
        makeViewHaveVisibility(initialVisibility);
        initializeBackgroundColor(savedInstanceState);
    }

    private void initializeTimePeriod(Bundle savedInstanceState) {
        if (savedInstanceState != null && timePeriod == null) {
            timePeriod = (Calendar) savedInstanceState.getSerializable(SAVED_TIME_PERIOD);
        }
    }

    private void initializeTitleDisplayView(Bundle savedInstanceState) {
        titleDisplayView = getView().findViewById(R.id.fragmentNoteList_titleDisplay);
        if (initialTitle != null) {
            setTitle(initialTitle);
            titleDisplayView.setVisibility(View.VISIBLE);
        } else if (savedInstanceState != null) {
            String toDisplay = savedInstanceState.getString(SAVED_TITLE);
            setTitle(toDisplay);
            if (!toDisplay.isEmpty()) {
                titleDisplayView.setVisibility(View.VISIBLE);
            } else {
                titleDisplayView.setVisibility(View.GONE);
            }
        } else {
            titleDisplayView.setVisibility(View.GONE);
        }
    }

    private void initializeSelectionMode(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            isInSingleSelectionMode = true;
        } else {
            isInSingleSelectionMode = savedInstanceState.getBoolean(SAVED_SELECTION_MODE);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeHighlightedContent(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            highlightedContent = (ArrayList<Content>) savedInstanceState.getSerializable(SAVED_HIGHLIGHTED_CONTENT);
            contentHighlightColor = savedInstanceState.getInt(SAVED_HIGHLIGHT_COLOR);
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeContentDisplayList(Bundle savedInstanceState) {
        contentListDisplayView = getView().findViewById(R.id.fragmentNoteList_noteDisplayList);
        if (savedInstanceState != null) {
            contentList = (ArrayList<Content>) savedInstanceState.getSerializable(SAVED_CONTENT_LIST);
            selectedContentInList = (ArrayList<Content>) savedInstanceState.getSerializable(SAVED_SELECTED_CONTENT);
        }
        setContentList(contentList);
    }

    private void initializeBackgroundColor(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            int savedColor = savedInstanceState.getInt(SAVED_BACKGROUND_COLOR);
            if (savedColor != -1) {
                getView().setBackgroundColor(savedColor);
            }
            initialBackgroundColor = savedColor;
        } else if (initialBackgroundColor != -1) {
            getView().setBackgroundColor(initialBackgroundColor);
        }
    }

    private void displayContentListAsSingleSelection(List<Content> contentList) {
        contentListDisplayView.setAdapter(new ArrayAdapter<Content>(getContext(), android.R.layout.simple_list_item_1, contentList.toArray(new Content[0])) {
            @NonNull
            @Override
            public View getView(int pos, @Nullable View view, @NonNull ViewGroup viewGroup) {
                Content contentInPos = contentList.get(pos);
                View layoutView;
                TextView contentDisplayNameView;
                TextView contentDescriptionView;
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    layoutView = inflater.inflate(R.layout.note_entry_in_list, null);
                    contentDisplayNameView = layoutView.findViewById(R.id.content_display_name);
                    contentDescriptionView = layoutView.findViewById(R.id.content_description);
                } else {
                    contentDisplayNameView = view.findViewById(R.id.content_display_name);
                    layoutView = view;
                    contentDescriptionView = view.findViewById(R.id.content_description);
                }

                contentDisplayNameView.setText(contentInPos.getDisplayName());

                if (contentInPos.isPriority(timePeriod)) {
                    layoutView.setBackgroundResource(R.color.colorPriority);
                } else {
                    layoutView.setBackgroundResource(R.color.colorNoPriority);
                }

                if (highlightedContent.contains(contentInPos)) {
                    layoutView.setBackgroundColor(contentHighlightColor);
                }

                if (contentInPos.getDescription() == null || contentInPos.getDescription().isEmpty()) {
                    contentDescriptionView.setVisibility(View.GONE);
                    contentDescriptionView.setText("");
                } else {
                    contentDescriptionView.setVisibility(View.VISIBLE);
                    contentDescriptionView.setText(contentInPos.getDescription());
                }

                return layoutView;
            }
        });
        contentListDisplayView.setOnItemClickListener((parent, view, pos, id) -> {
            contentFromListSelected(contentList.get(pos));
        });
        contentListDisplayView.setOnItemLongClickListener((parent, view, pos, id) -> {
            contentFromListLongSelected(contentList.get(pos));
            return true;
        });
        selectedContentInList.clear();
        isInSingleSelectionMode = true;
    }

    private void contentFromListSelected(Content content) {
        listener.onContentSelected(content);
    }

    private void contentFromListLongSelected(Content content) {
        listener.onContentLongSelected(content);
    }

    private void displayContentListAsCheckListDisplay(List<Content> contentList) {
        contentListDisplayView.setAdapter(new ArrayAdapter<Content>(getContext(), android.R.layout.simple_list_item_1, contentList.toArray(new Content[0])) {
            @NonNull
            @Override
            public View getView(int pos, @Nullable View view, @NonNull ViewGroup viewGroup) {
                Content contentInPos = contentList.get(pos);
                if (view == null) {
                    //create new view
                    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.note_checkable_entry_in_list, null);
                }

                ImageView noteStatusDisplay = view.findViewById(R.id.noteCheckable_statusDisplay);
                noteStatusDisplay.setAdjustViewBounds(true);
                noteStatusDisplay.setScaleType(ImageView.ScaleType.CENTER_CROP);
                noteStatusDisplay.setMaxHeight(30);
                noteStatusDisplay.setMaxWidth(30);
                if (selectedContentInList.contains(contentInPos)) {
                    noteStatusDisplay.setImageResource(R.drawable.baseline_check_box_black_18);
                } else {
                    noteStatusDisplay.setImageResource(R.drawable.baseline_check_box_outline_blank_black_18);
                }

                ((TextView) view.findViewById(R.id.noteCheckable_display)).setText(contentInPos.getDisplayName());

                if (contentInPos.isPriority(timePeriod)) {
                    view.setBackgroundResource(R.color.colorPriority);
                } else {
                    view.setBackgroundResource(R.color.colorNoPriority);
                }

                if (highlightedContent.contains(contentInPos)) {
                    view.setBackgroundColor(contentHighlightColor);
                }

                return view;
            }
        });
        contentListDisplayView.setOnItemClickListener((parent, view, pos, id) -> {
            Content selectedContent = contentList.get(pos);
            ImageView image = view.findViewById(R.id.noteCheckable_statusDisplay);

            if (selectedContentInList.contains(selectedContent)) {
                selectedContentInList.remove(selectedContent);
                image.setImageResource(R.drawable.baseline_check_box_outline_blank_black_18);
            } else {
                selectedContentInList.add(selectedContent); //t odo continue this later
                image.setImageResource(R.drawable.baseline_check_box_black_18);
            }
        });
        isInSingleSelectionMode = false;
    }

    @Override
    public void onSaveInstanceState(Bundle toSave) {
        toSave.putString(SAVED_TITLE, titleDisplayView.getText().toString());
        toSave.putSerializable(SAVED_CONTENT_LIST, contentList);
        toSave.putInt(SAVED_BACKGROUND_COLOR, initialBackgroundColor);
        toSave.putSerializable(SAVED_TIME_PERIOD, timePeriod);
        toSave.putBoolean(SAVED_SELECTION_MODE, isInSingleSelectionMode);
        toSave.putSerializable(SAVED_SELECTED_CONTENT, selectedContentInList);
        toSave.putSerializable(SAVED_HIGHLIGHTED_CONTENT, highlightedContent);
        toSave.putInt(SAVED_HIGHLIGHT_COLOR, contentHighlightColor);
    }

    /**
     * <p>Set's the title display's text to the specified parameter. If view is still not created, the param is stored</p>
     * <p>and will set the title display's text once view is created</p>
     * @param title the text to display
     */
    public void setTitle(String title) {
        if (isViewCreated()) {
            titleDisplayView.setText(title);
        } else {
            initialTitle = title;
        }
    }

    /**
     * <p>Set's the list of notes to display into a list. If view is still not created, the param is stored</p>
     * <p>and will make the list display the param once the view is created. Array is not rearranged</p>
     * @param contentList the array of Notes to display
     */
    public void setContentList(List<Content> contentList) {
        this.contentList = new ArrayList<>(contentList);
        refreshListDisplay();
    }

    private void refreshListDisplay() {
        if (isViewCreated()) {
            if (isInSingleSelectionMode) {
                displayContentListAsSingleSelection(contentList);
            } else {
                removeSelectedContentNotPresentInContentListParam(contentList);
                displayContentListAsCheckListDisplay(contentList);
            }
        }
    }

    private void removeSelectedContentNotPresentInContentListParam(List<Content> contentList) {
        List<Content> contentToRemove = new ArrayList<>();
        for (Content content: selectedContentInList) {
            if (!contentList.contains(content)) {
                contentToRemove.add(content);
            }
        }

        for (Content content: contentToRemove) {
            selectedContentInList.remove(content);
        }
    }

    public boolean hasEmptyNoteList() {
        return contentList.size() == 0;
    }

    /**
     * <p>Set's the fragment's background to specified color. If view is still not created, the param is stored</p>
     * <p>and will set the background color once this view is created</p>
     * @param color the color of this fragment's background
     */
    public void setBackgroundColor(int color) {
        if (isViewCreated()) {
            getView().setBackgroundColor(color);
        } else {
            initialBackgroundColor = color;
        }
    }

    public void setTime(Calendar time) {
        this.timePeriod = time;
    }

    /**
     * Makes view invisible and take no space. If view is already created, it will set it's visibility to View.GONE.
     * If the view was not created yet, then the view will initialize as gone.
     */
    public void makeViewGone() {
        if (isViewCreated()) {
            this.getView().setVisibility(View.GONE);
        } else {
            initialVisibility = View.GONE;
        }
    }

    /**
     * Makes view invisible and take no space. If view is already created, it will set it's visibility to View.VISIBLE.
     * If the view was not created yet, then the view will initialize as visible.
     */
    public void makeViewVisible() {
        if (isViewCreated()) {
            this.getView().setVisibility(View.VISIBLE);
        } else {
            initialVisibility = View.VISIBLE;
        }
    }

    private boolean isViewCreated() {
        return getView() != null;
    }

    private void makeViewHaveVisibility(int visibility) {
        if (getView() != null) {
            this.getView().setVisibility(visibility);
        }
    }

    public void setSingleSelectionMode() {
        if (getView() != null) {
            displayContentListAsSingleSelection(contentList);
        } else {
            isInSingleSelectionMode = true;
        }
    }

    public void setMultipleSelectionMode() {
        if (getView() != null) {
            displayContentListAsCheckListDisplay(contentList);
        } else {
            isInSingleSelectionMode = false;
        }
    }

    public boolean isInSingleSelectionMode() {
        return isInSingleSelectionMode;
    }

    public boolean hasContentSelectedInMultipleSelectionMode() {
        selectedContentInList.trimToSize();
        return !selectedContentInList.isEmpty();
    }

    public List<Content> getSelectedContentInMultipleSelectionMode() {
        if (isInSingleSelectionMode) {
            throw new UnsupportedOperationException("Must be in multiple selection mode to use!");
        } else {
            selectedContentInList.trimToSize();
            return new ArrayList<>(selectedContentInList);
        }
    }

    /**
     * Note: does not update display of list, if currently showing
     */
    public void unselectAllNotesInMultipleSelectionMode() {
        selectedContentInList.clear();
    }

    public void highlightContent(List<Content> contentToHighlight, @ColorInt int color) {
        highlightedContent.clear();
        highlightedContent.addAll(contentToHighlight);
        contentHighlightColor = color;

        refreshListDisplay();
    }
}

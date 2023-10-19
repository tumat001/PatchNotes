package com.example.patchnotes.contentrelated;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Note extends Content implements Serializable {

    private static final String EMPTY_DISPLAY_NAME = "Untitled Note";

    private String title, text;
    private Calendar expirationPeriod, priorityPeriod, creationPeriod;
    private DaysOfWeek[] priorityDaysOfWeek;
    private String incId;
    private String categoryUniqueId;

    private Note(@NonNull String title, @NonNull String text, Calendar expirationPeriod, Calendar priorityPeriod,
                 @NonNull Calendar creationPeriod, @Nullable DaysOfWeek[] priorityDayOfWeek, @Nullable String incId, @Nullable String categoryUniqueId) {
        this.title = title;
        this.text = text;
        this.expirationPeriod = expirationPeriod;
        this.priorityPeriod = priorityPeriod;
        this.creationPeriod = creationPeriod;
        this.priorityDaysOfWeek = priorityDayOfWeek;
        this.incId = incId;
        this.categoryUniqueId = categoryUniqueId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public Calendar getExpirationPeriod() {
        if (expirationPeriod != null) {
            return (Calendar) expirationPeriod.clone();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Calendar getPriorityPeriod() {
        if (priorityPeriod != null) {
            return (Calendar) priorityPeriod.clone();
        } else {
            return null;
        }
    }

    @NonNull
    public Calendar getCreationPeriod() {
        return (Calendar) creationPeriod.clone();
    }

    @Nullable
    @Override
    public DaysOfWeek[] getPriorityDaysOfWeek() {
        return priorityDaysOfWeek;
    }

    /**
     * @return a unique identifier which is given by the database.
     * If for some reason the database did not provide this a unique id, this will be null. This situation
     * is to be considered as an error.
     */
    @NonNull
    public String getIncId() {
        return incId;
    }

    /**
     *
     * @return value of {@link Note#getIncId()}
     */
    @NonNull
    @Override
    public String getUniqueId() {
        return getIncId();
    }

    @Nullable
    @Override
    public String getCategoryUniqueId() {
        return categoryUniqueId;
    }

    /**
     * Copies all data from argument, except for uniqueId.
     * @param toCopyFrom Note whose data should be copied from
     */
    public void updateDataToMatch(Note toCopyFrom) {
        this.title = toCopyFrom.title;
        this.text = toCopyFrom.text;
        this.expirationPeriod = toCopyFrom.expirationPeriod;
        this.priorityPeriod = toCopyFrom.priorityPeriod;
        this.priorityDaysOfWeek = toCopyFrom.priorityDaysOfWeek;
        this.creationPeriod = toCopyFrom.creationPeriod;
        this.categoryUniqueId = toCopyFrom.categoryUniqueId;
    }

    /**
     * Although creation period is the name, it's purpose is to tell the last visited time.
     * @param time The time this was "created", or for this program's intent: last visited time.
     */
    public void setCreationPeriod(Calendar time) {
        this.creationPeriod = time;
    }

    /**
     *
     * @param o Object to be compared with this Note
     * @return true if o is a Note instance and has the same uniqueId as this Note,
     *         false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Note) {
            return ((Note) o).incId.equals(this.incId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 505070183;
    }

    /**
     *
     * @return value of {@link Note#getDisplayName()}
     */
    @NonNull
    @Override
    public String toString() {
        return getDisplayName();
    }

    /**
     * @return <p>A representation of the note object. Normally returns the note's {@link Note#getTitle()}.</p>
     * <p>If the note object's {@link Note#getTitle()} is empty, then the note's {@link Note#getText()} is returned, but only a substring of it, up to {@link Note#MAX_DISPLAY_NAME_LENGTH} in length.
     * If the resulting length is {@link Note#MAX_DISPLAY_NAME_LENGTH}, an ellipsis (...) is appended.</p>
     * <p>If the note object's {@link Note#getText()} is also empty, then a constant {@link Note#EMPTY_DISPLAY_NAME} is returned instead.</p>
     */
    @NonNull
    @Override
    public String getDisplayName() {
        if (!getTitle().isEmpty()) {
            return getTitle();
        } else if (!getText().isEmpty()) {
            String returnVal = getText().substring(0, Math.min(getText().length(), MAX_DISPLAY_NAME_LENGTH));
            if (returnVal.length() == MAX_DISPLAY_NAME_LENGTH) {
                returnVal += "...";
            }
            return returnVal;
        } else {
            return EMPTY_DISPLAY_NAME;
        }
    }

    /**
     *
     * @return true if this note has an empty title and text, as well as no set prio period, expi period, prio days and category.
     */
    @Override
    public boolean isEmpty() {
        return getText().isEmpty() && getTitle().isEmpty()
                && getPriorityPeriod() == null
                && getExpirationPeriod() == null
                && getPriorityDaysOfWeek() == null
                && getCategoryUniqueId() == null;
    }

    /**
     *
     * @return a builder with the invoking note's values, except for id and creation period;
     */
    public Builder makeBuilderHaveNotesContents() {
        return new Builder()
                .setCategoryUniqueId(this.categoryUniqueId)
                .setExpirationPeriod(this.expirationPeriod)
                .setPriorityDaysOfWeek(this.priorityDaysOfWeek)
                .setPriorityPeriod(this.priorityPeriod)
                .setText(this.text)
                .setTitle(this.title);
    }


    public static class Builder implements Serializable {
        private String title, text;
        private Calendar expirationPeriod, priorityPeriod;
        private DaysOfWeek[] priorityDaysOfWeek;
        private String uniqueId;
        private String categoryUniqueId;

        public Builder() {
            title = "";
            text = "";
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public String getText() {
            return text;
        }

        public Builder setExpirationPeriod(Calendar expirationPeriod) {
            this.expirationPeriod = setSecondAndMillisecondToZero(expirationPeriod);
            return this;
        }

        private Calendar setSecondAndMillisecondToZero(Calendar calendar) {
            if (calendar == null) {
                return null;
            }
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar;
        }

        public Calendar getExpirationPeriod() {
            if (expirationPeriod != null) {
                return (Calendar) expirationPeriod.clone();
            } else {
                return null;
            }
        }

        public Builder setPriorityPeriod(Calendar priorityPeriod) {
            this.priorityPeriod = setSecondAndMillisecondToZero(priorityPeriod);
            return this;
        }

        public Calendar getPriorityPeriod() {
            if (priorityPeriod != null) {
                return (Calendar) priorityPeriod.clone();
            } else {
                return null;
            }
        }

        public Builder setPriorityDaysOfWeek(DaysOfWeek[] daysOfWeek) {
            this.priorityDaysOfWeek = daysOfWeek;
            return this;
        }

        public DaysOfWeek[] getPriorityDaysOfWeek() {
            return priorityDaysOfWeek;
        }

        /**
         * To be used only by the database.
         * @param uniqueId
         */
        public Builder setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }

        public String getUniqueId() {
            return uniqueId;
        }

        public Builder setCategoryUniqueId(String categoryUniqueId) {
            this.categoryUniqueId = categoryUniqueId;
            return this;
        }

        public String getCategoryUniqueId() {
            return categoryUniqueId;
        }

        /**
         * Copies the contents of the specified note to this builder
         * @param noteToCopyFrom note whose details are to be copied, except for creation period, and uniqueId/incId
         * @return this builder, to allow chain calling
         */
        public Builder copyContentsFromNote(Note noteToCopyFrom) {
            this.setTitle(noteToCopyFrom.getTitle());
            this.setText(noteToCopyFrom.getText());
            this.setPriorityPeriod(noteToCopyFrom.getPriorityPeriod());
            this.setPriorityDaysOfWeek(noteToCopyFrom.getPriorityDaysOfWeek());
            this.setExpirationPeriod(noteToCopyFrom.getExpirationPeriod());
            this.setCategoryUniqueId(noteToCopyFrom.getCategoryUniqueId());
            return this;
        }

        public Note constructNote() {
            Calendar currentPeriod = GregorianCalendar.getInstance();
            return new Note(title, text, expirationPeriod, priorityPeriod, currentPeriod, priorityDaysOfWeek, uniqueId, categoryUniqueId);
        }

    }
}

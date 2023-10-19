package com.example.patchnotes.contentrelated;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.contentdatarelated.DaysOfWeek;

import java.io.Serializable;
import java.util.Calendar;

public class Category extends Content implements Serializable {

    private static final String EMPTY_DISPLAY_NAME = "No Title Category";
    private static final int NO_IMAGE_SELECTED = -1;

    private String displayName, description, uniqueId;

    private Calendar autoFillPriorityPeriod, autoFillExpirationPeriod;
    private DaysOfWeek[] autoFillPriorityDaysOfWeek;
    private boolean autoFillEnabled;

    private Calendar selfDeletionPeriod, selfPriorityPeriod;
    private DaysOfWeek[] selfDaysOfWeek;
    private Calendar creationPeriod;

    private String categoryUniqueId;

    private int pictureResourceId;

    private Category(@NonNull String displayName, @NonNull String description, @NonNull String uniqueId, @Nullable Calendar autoFillPriorityPeriod,
                     @Nullable Calendar autoFillExpirationPeriod, @Nullable DaysOfWeek[] autoFillDaysOfWeek, boolean autoFillEnabled,
                     @Nullable Calendar selfDeletionPeriod, @NonNull Calendar creationPeriod, @Nullable DaysOfWeek[] selfDaysOfWeek,
                     @Nullable Calendar selfPriorityPeriod, @Nullable String categoryUniqueId, int pictureResourceId) {
        this.displayName = displayName;
        this.description = description;
        this.uniqueId = uniqueId;

        this.autoFillPriorityDaysOfWeek = autoFillDaysOfWeek;
        this.autoFillPriorityPeriod = autoFillPriorityPeriod;
        this.autoFillExpirationPeriod = autoFillExpirationPeriod;
        this.autoFillEnabled = autoFillEnabled;

        this.selfPriorityPeriod = selfPriorityPeriod;
        this.selfDeletionPeriod = selfDeletionPeriod;
        this.creationPeriod = creationPeriod;
        this.selfDaysOfWeek = selfDaysOfWeek;

        this.categoryUniqueId = categoryUniqueId;

        this.pictureResourceId = pictureResourceId;
    }

    /**
     *
     * @param o Object to be compared with this Note
     * @return true if o is a Note instance and has the same uniqueId as this Note,
     *         false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Category) {
            return ((Category) o).uniqueId.equals(this.uniqueId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 707070183;
    }

    @NonNull
    @Override
    public Calendar getCreationPeriod() {
        return creationPeriod;
    }

    /**
     *
     * @return the display name of this category, or {@link Category#EMPTY_DISPLAY_NAME} if no category name is assigned (or is empty)
     */
    @NonNull
    @Override
    public String getDisplayName() {
        return getDisplayNameOfThisCategory(displayName);
    }

    private String getDisplayNameOfThisCategory(String initialDisplayName) {
        if (initialDisplayName.isEmpty()) {
            return EMPTY_DISPLAY_NAME;
        } else {
            return initialDisplayName;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return getDisplayName();
    }

    @NonNull
    @Override
    public String getDescription() {
        return description;
    }

    /**
     *
     * @return the unique id corresponding to this category. This can be null if the database is not functioning. This
     * case is to be considered an error.
     */
    @NonNull
    public String getUniqueId() {
        return uniqueId;
    }

    @Nullable
    @Override
    public Calendar getExpirationPeriod() {
        if (selfDeletionPeriod != null) {
            return (Calendar) selfDeletionPeriod.clone();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Calendar getPriorityPeriod() {
        if (selfPriorityPeriod != null) {
            return (Calendar) selfPriorityPeriod.clone();
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public DaysOfWeek[] getPriorityDaysOfWeek() {
        return selfDaysOfWeek;
    }

    @Nullable
    @Override
    public String getCategoryUniqueId() {
        return categoryUniqueId;
    }

    @Override
    public boolean isEmpty() {
        return displayName.isEmpty() && description.isEmpty()
                && selfPriorityPeriod == null
                && selfDaysOfWeek == null
                && selfDeletionPeriod == null
                && categoryUniqueId == null
                && autoFillExpirationPeriod == null
                && autoFillPriorityPeriod == null
                && autoFillPriorityDaysOfWeek == null
                && pictureResourceId == NO_IMAGE_SELECTED;
    }

    @Nullable
    public Calendar getAutoFillPriorityPeriod() {
        if (autoFillPriorityPeriod != null) {
            return (Calendar) autoFillPriorityPeriod.clone();
        } else {
            return null;
        }
    }

    @Nullable
    public Calendar getAutoFillExpirationPeriod() {
        if (autoFillExpirationPeriod != null) {
            return (Calendar) autoFillExpirationPeriod.clone();
        } else {
            return null;
        }
    }

    @Nullable
    public DaysOfWeek[] getAutoFillPriorityDaysOfWeek() {
        return autoFillPriorityDaysOfWeek;
    }

    public boolean isAutoFillEnabled() {
        return autoFillEnabled;
    }

    @DrawableRes
    public int getPictureResourceId() {
        return pictureResourceId;
    }

    public Builder getBuilderWithValuesOfThis() {
        return new Builder(displayName)
                .setDescription(description)
                .setCategoryUniqueId(categoryUniqueId)
                .setUniqueId(uniqueId)
                .setCreationPeriod(creationPeriod)
                .setSelfPriorityPeriod(selfPriorityPeriod)
                .setSelfPriorityDaysOfWeek(selfDaysOfWeek)
                .setSelfDeletionPeriod(selfDeletionPeriod)
                .setAutoFillEnabled(autoFillEnabled)
                .setAutoFillPriorityPeriod(autoFillPriorityPeriod)
                .setAutoFillPriorityDaysOfWeek(autoFillPriorityDaysOfWeek)
                .setAutoFillExpirationPeriod(autoFillExpirationPeriod)
                .setPictureResourceId(pictureResourceId);
    }

    //

    public static class Builder extends Content {
        private String displayName, description, uniqueId;

        private Calendar autoFillPriorityPeriod, autoFillExpirationPeriod;
        private DaysOfWeek[] autoFillPriorityDaysOfWeek;
        private boolean autoFillEnabled;

        private Calendar selfDeletionPeriod, selfPriorityPeriod;
        private DaysOfWeek[] selfDaysOfWeek;

        private Calendar creationPeriod;

        private String categoryUniqueId;

        private int pictureResourceId = -1;

        public Builder(@NonNull String displayName) {
            this.displayName = displayName;
            this.description = "";
        }

        public Builder setDisplayName(@NonNull String displayName) {
            this.displayName = displayName;
            return this;
        }
        @NonNull
        public String getDisplayName() {
            return displayName;
        }

        public Builder setDescription(@NonNull String description) {
            this.description = description;
            return this;
        }
        @NonNull
        public String getDescription() {
            return description;
        }

        public Builder setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }
        public String getUniqueId() {
            return uniqueId;
        }

        public Builder setAutoFillPriorityPeriod(@Nullable Calendar time) {
            this.autoFillPriorityPeriod = setSecondAndMillisecondToZero(time);
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
        @Nullable
        public Calendar getAutoFillPriorityPeriod() {
            return autoFillPriorityPeriod;
        }

        public Builder setAutoFillExpirationPeriod(@Nullable Calendar time) {
            this.autoFillExpirationPeriod = setSecondAndMillisecondToZero(time);
            return this;
        }
        @Nullable
        public Calendar getAutoFillExpirationPeriod() {
            return autoFillExpirationPeriod;
        }

        public Builder setAutoFillPriorityDaysOfWeek(@Nullable DaysOfWeek[] daysOfWeek) {
            this.autoFillPriorityDaysOfWeek = daysOfWeek;
            return this;
        }
        @Nullable
        public DaysOfWeek[] getAutoFillPriorityDaysOfWeek() {
            return autoFillPriorityDaysOfWeek;
        }

        public Builder setSelfPriorityPeriod(@Nullable Calendar time) {
            this.selfPriorityPeriod = setSecondAndMillisecondToZero(time);
            return this;
        }
        @Nullable
        @Override
        public Calendar getPriorityPeriod() {
            return selfPriorityPeriod;
        }
        @Nullable
        public Calendar getSelfPriorityPeriod() { return getPriorityPeriod(); }

        public Builder setSelfDeletionPeriod(@Nullable Calendar time) {
            this.selfDeletionPeriod = setSecondAndMillisecondToZero(time);
            return this;
        }
        @Nullable
        public Calendar getSelfDeletionPeriod() {
            return selfDeletionPeriod;
        }

        @Nullable
        @Override
        public Calendar getExpirationPeriod() { return getSelfDeletionPeriod(); }

        public Builder setSelfPriorityDaysOfWeek(@Nullable DaysOfWeek[] daysOfWeek) {
            this.selfDaysOfWeek = daysOfWeek;
            return this;
        }
        @Nullable
        public DaysOfWeek[] getSelfPriorityDaysOfWeek() { return selfDaysOfWeek; }
        @Nullable
        @Override
        public DaysOfWeek[] getPriorityDaysOfWeek() {
            return selfDaysOfWeek;
        }

        public Builder setAutoFillEnabled(boolean autoFillEnabled) {
            this.autoFillEnabled = autoFillEnabled;
            return this;
        }
        public boolean getAutoFillEnable() {
            return autoFillEnabled;
        }

        public Builder setCategoryUniqueId(@Nullable String categoryUniqueId) {
            this.categoryUniqueId = categoryUniqueId;
            return this;
        }
        @Nullable
        public String getCategoryUniqueId() {
            return categoryUniqueId;
        }

        public Builder setPictureResourceId(@DrawableRes int pictureResourceId) {
            this.pictureResourceId = pictureResourceId;
            return this;
        }
        @DrawableRes
        public int getPictureResourceId() {
            return pictureResourceId;
        }

        public Builder setCreationPeriod(@Nullable Calendar time) {
            this.creationPeriod = time;
            return this;
        }
        @NonNull
        public Calendar getCreationPeriod() {
            return creationPeriod;
        }

        public boolean isEmpty() {
            return displayName.isEmpty() && description.isEmpty()
                    && selfPriorityPeriod == null
                    && selfDaysOfWeek == null
                    && selfDeletionPeriod == null
                    && categoryUniqueId == null
                    && autoFillExpirationPeriod == null
                    && autoFillPriorityPeriod == null
                    && autoFillPriorityDaysOfWeek == null
                    && pictureResourceId == -1;
        }

        public Category constructCategory() {
            return new Category(displayName, description, uniqueId, autoFillPriorityPeriod, autoFillExpirationPeriod,
                    autoFillPriorityDaysOfWeek, autoFillEnabled, selfDeletionPeriod, getCreationPeriod(),
                    selfDaysOfWeek, selfPriorityPeriod, categoryUniqueId, pictureResourceId);
        }

    }

}

package com.example.patchnotes.contentrelated;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.patchnotes.R;
import com.example.patchnotes.contentdatarelated.DaysOfWeek;
import com.example.patchnotes.contentdatarelated.ParsableData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class NoteList extends Content implements Serializable {

    public enum EntryType implements Serializable {
        CHECK_ENTRY("CHECK", R.drawable.baseline_check_box_black_48dp),
        NUMBER_ENTRY("NUMBER", R.drawable.baseline_filter_1_black_48dp),
        //PICTURE_ENTRY("PICTURE", R.drawable.baseline_image_black_48dp),
        BULLET_ENTRY("BULLET", R.drawable.baseline_fiber_circle_shaded_black_48dp);
        
        private String internalId;
        private int picResId;

        EntryType(String internalId, int picResId) {
            this.internalId = internalId;
            this.picResId = picResId;
        }

        Entry<?> convertToThisType(Entry<?> toConvert, Object metadata) {
            if (this == CHECK_ENTRY) {
                return new CheckEntry(toConvert.getEntryText(), false);
            } else if (this == NUMBER_ENTRY) {
                return new NumberEntry(toConvert.getEntryText(), NumberEntry.NO_VALUE);
            } else if (this == BULLET_ENTRY) {
                return new BulletEntry(toConvert.getEntryText(), null);
            }

            return null; //should not reach here
        }

        public static EntryType getEntryTypeByInternalId(@NonNull String id) {
            for (EntryType type: values()) {
                if (type.getInternalId().equalsIgnoreCase(id)) {
                    return type;
                }
            }
            return null;
        }

        public static List<Integer> getImageResourcesOfTypes() {
            List<Integer> picRes = new ArrayList<>();
            for (EntryType type: values()) {
                picRes.add(type.picResId);
            }
            return picRes;
        }

        @NonNull
        @Override
        public String toString() {
            String origText = name();

            String[] words = origText.split("_");
            StringBuilder builder = new StringBuilder();
            for (String word: words) {
                builder.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
            return builder.toString();
        }

        /*
        This is used by the database. DO NOT CHANGE
         */
        public String getInternalId() {
            return internalId;
        }

    }

    private static final String EMPTY_DISPLAY_NAME = "Untitled Note List";

    private Calendar creationPeriod, priorityPeriod, expirationPeriod;
    private DaysOfWeek[] priorityDaysOfWeek;
    private String displayName, subText, uniqueId, categoryUniqueId;
    private List<Entry<?>> listEntries = new ArrayList<>();
    private EntryType entryType;


    private NoteList(@NonNull String displayName, @NonNull String subText, @NonNull String uniqueId, @Nullable String categoryUniqueId,
                     @NonNull Calendar creationPeriod, @Nullable Calendar priorityPeriod, @Nullable Calendar expirationPeriod,
                     @Nullable DaysOfWeek[] priorityDaysOfWeek, @NonNull List<Entry<?>> listEntries, @NonNull EntryType entryType) {
        this.displayName = displayName;
        this.subText = subText;
        this.uniqueId = uniqueId;
        this.categoryUniqueId = categoryUniqueId;
        this.creationPeriod = creationPeriod;
        this.priorityPeriod = priorityPeriod;
        this.priorityDaysOfWeek = priorityDaysOfWeek;
        this.expirationPeriod = expirationPeriod;
        this.listEntries.addAll(listEntries);
        this.entryType = entryType;
    }

    /**
     *
     * @param o Object to be compared with this Note
     * @return true if o is a Note instance and has the same uniqueId as this Note,
     *         false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof NoteList) {
            return ((NoteList) o).getUniqueId().equals(this.uniqueId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 606070183;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        if (displayName.isEmpty()) {
            return EMPTY_DISPLAY_NAME;
        } else {
            String returnVal = displayName.substring(0, Math.min(displayName.length(), MAX_DISPLAY_NAME_LENGTH));
            if (returnVal.length() == MAX_DISPLAY_NAME_LENGTH) {
                returnVal += "...";
            }
            return returnVal;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @NonNull
    public String getSubText() {
        return subText;
    }

    @NonNull
    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Nullable
    @Override
    public String getCategoryUniqueId() {
        return categoryUniqueId;
    }

    @NonNull
    @Override
    public Calendar getCreationPeriod() {
        return (Calendar) creationPeriod.clone();
    }

    @Nullable
    @Override
    public Calendar getPriorityPeriod() {
        return getCloneOrNullOf(priorityPeriod);
    }

    @Nullable
    private Calendar getCloneOrNullOf(@Nullable Calendar time) {
        if (time == null) {
            return null;
        } else {
            return (Calendar) time.clone();
        }
    }

    @Nullable
    @Override
    public DaysOfWeek[] getPriorityDaysOfWeek() {
        return priorityDaysOfWeek;
    }

    @Nullable
    @Override
    public Calendar getExpirationPeriod() {
        return getCloneOrNullOf(expirationPeriod);
    }

    @NonNull
    public List<Entry<?>> getListEntries() {
        return new ArrayList<>(listEntries);
    }

    @NonNull
    public EntryType getEntryType() {
        return entryType;
    }

    @Override
    public boolean isEmpty() {
        return displayName.isEmpty() && subText.isEmpty()
                && categoryUniqueId == null
                && priorityPeriod == null
                && priorityDaysOfWeek == null
                && expirationPeriod == null
                && listEntries.isEmpty();
    }

    public Builder createBuilderWithThisNotesContents() {
        Builder builder = new Builder();
        builder.setDisplayName(displayName);
        builder.setSubText(subText);
        builder.setUniqueId(uniqueId);
        builder.setCategoryUniqueId(categoryUniqueId);
        builder.setPriorityPeriod(priorityPeriod);
        builder.setPriorityDaysOfWeek(priorityDaysOfWeek);
        builder.setExpirationPeriod(expirationPeriod);
        builder.setEntryType(entryType);
        builder.setListEntries(listEntries);

        return builder;
    }

    public static class Builder extends Content implements Serializable {

        private Calendar creationPeriod, priorityPeriod, expirationPeriod;
        private DaysOfWeek[] priorityDaysOfWeek;
        private String displayName, subText, uniqueId, categoryUniqueId;
        private List<Entry<?>> listEntries = new ArrayList<>();
        private EntryType entryType;

        public Builder() {
            this.displayName = "";
            this.subText = "";
            entryType = EntryType.BULLET_ENTRY;
        }

        public Builder setDisplayName(@NonNull String displayName) {
            this.displayName = displayName;
            return this;
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getDescription() {
            return null;
        }

        public Builder setSubText(@NonNull String subText) {
            this.subText = subText;
            return this;
        }

        @NonNull
        public String getSubText() {
            return subText;
        }

        public Builder setUniqueId(@NonNull String uniqueId) {
            this.uniqueId = uniqueId;

            return this;
        }

        @NonNull
        @Override
        public String getUniqueId() {
            return uniqueId;
        }

        public Builder setCategoryUniqueId(@Nullable String categoryUniqueId) {
            this.categoryUniqueId = categoryUniqueId;
            return this;
        }

        @Nullable
        @Override
        public String getCategoryUniqueId() {
            return categoryUniqueId;
        }

        public Builder setCreationPeriod(@NonNull Calendar creationPeriod) {
            this.creationPeriod = creationPeriod;
            return this;
        }

        @NonNull
        @Override
        public Calendar getCreationPeriod() {
            return creationPeriod;
        }

        public Builder setPriorityPeriod(@Nullable Calendar priorityPeriod) {
            this.priorityPeriod = priorityPeriod;
            return this;
        }

        @Nullable
        @Override
        public Calendar getPriorityPeriod() {
            return priorityPeriod;
        }

        public Builder setPriorityDaysOfWeek(@Nullable DaysOfWeek[] daysOfWeek) {
            this.priorityDaysOfWeek = daysOfWeek;
            return this;
        }

        @Nullable
        @Override
        public DaysOfWeek[] getPriorityDaysOfWeek() {
            return priorityDaysOfWeek;
        }

        public Builder setExpirationPeriod(@Nullable Calendar expirationPeriod) {
            this.expirationPeriod = expirationPeriod;
            return this;
        }

        @Nullable
        @Override
        public Calendar getExpirationPeriod() {
            return expirationPeriod;
        }

        @Override
        public boolean isEmpty() {
            return displayName.isEmpty()
                    && subText.isEmpty()
                    && categoryUniqueId == null
                    && priorityPeriod == null
                    && priorityDaysOfWeek == null
                    && expirationPeriod == null
                    && listEntries.isEmpty();
        }

        public Builder setListEntries(List<Entry<?>> listEntries) {
            this.listEntries.clear();
            this.listEntries.addAll(listEntries);
            return this;
        }

        public Builder addToListEntries(Entry<?> listEntry) {
            this.listEntries.add(listEntry);
            return this;
        }

        public Builder removeFromListEntries(int entryPosToRemove) {
            this.listEntries.remove(entryPosToRemove);
            return this;
        }

        public Builder setListEntryAt(int pos, Entry<?> entry) {
            this.listEntries.set(pos, entry);
            return this;
        }

        public List<Entry<?>> getListEntries() {
            return listEntries;
        }

        /**
         * Sets the type of this list entries to the param. Also converts all current note list entries to this type.
         * All entries will be reverted to their default values however
         * @param type the type that will be used
         * @return this instance to allow chain calling
         */
        public Builder setEntryType(EntryType type) {
            if (!this.entryType.equals(type)) {
                this.entryType = type;

                List<Entry<?>> bucketEntryList = new ArrayList<>();
                for (int i = 0; i < getListEntries().size(); i++) {
                    Object metadata = null;
                    if (type == EntryType.NUMBER_ENTRY) {
                        metadata = i;
                    }
                    bucketEntryList.add(type.convertToThisType(getListEntries().get(i), metadata));
                }

                setListEntries(bucketEntryList);
            }

            return this;
        }

        public EntryType getEntryType() {
            return entryType;
        }

        public NoteList constructNoteList() {
            if (creationPeriod == null) {
                creationPeriod = GregorianCalendar.getInstance();
            }

            return new NoteList(displayName, subText, uniqueId, categoryUniqueId, creationPeriod,
                    priorityPeriod, expirationPeriod, priorityDaysOfWeek, listEntries, entryType);
        }

    }

    //

    abstract public static class Entry<T> implements Serializable, ParsableData<Entry<T>> {

        public enum TypeIdentifier {
            $CHECK_ENTRY$,
            $PICTURE_ENTRY$,
            $NUMBER_ENTRY$,
            $BULLET_ENTRY$;
        }

        protected String entryText;

        protected Entry(String entryText) {
            this.entryText = entryText;
        }

        @NonNull
        public String getEntryText() {
            return entryText;
        }

        @Nullable abstract public T getMetadata();

        @Override
        @NonNull
        abstract public String getEncodeTypeIdentifier();

        public static TypeIdentifier getIdentifierFromString(String identifierString) {
            for (TypeIdentifier identifier: TypeIdentifier.values()) {
                if (identifier.toString().equals(identifierString)) {
                    return identifier;
                }
            }
            return null;
        }
    }

    public static class CheckEntry extends Entry<Boolean> implements Serializable {

        private boolean isChecked;

        public CheckEntry(@NonNull String entryText, boolean isChecked) {
            super(entryText);
            this.isChecked = isChecked;
        }

        @NonNull
        @Override
        public Boolean getMetadata() {
            return isChecked;
        }

        @NonNull
        @Override
        public String getEncodeTypeIdentifier() {
            return TypeIdentifier.$CHECK_ENTRY$.toString();
        }
    }

    public static class PictureEntry extends Entry<Integer> implements Serializable {

        public static final int NO_VALUE = -1;

        private int pictureDrawableRes;

        public PictureEntry(@NonNull String entryText, @DrawableRes int pictureDrawableRes) {
            super(entryText);
            this.pictureDrawableRes = pictureDrawableRes;
        }

        @DrawableRes
        @NonNull
        @Override
        public Integer getMetadata() {
            return pictureDrawableRes;
        }

        @NonNull
        @Override
        public String getEncodeTypeIdentifier() {
            return TypeIdentifier.$PICTURE_ENTRY$.toString();
        }

    }

    public static class NumberEntry extends Entry<Integer> implements Serializable {

        public static final int NO_VALUE = -1;

        private int numberIndex;

        public NumberEntry(@NonNull String entryText, int numberIndex) {
            super(entryText);
            this.numberIndex = numberIndex;
        }

        @NonNull
        @Override
        public Integer getMetadata() {
            return numberIndex;
        }

        @NonNull
        @Override
        public String getEncodeTypeIdentifier() {
            return TypeIdentifier.$NUMBER_ENTRY$.toString();
        }

    }

    public static class BulletEntry extends Entry<Object> implements Serializable {

        public BulletEntry(@NonNull String entryText, Object ignored) {
            super(entryText);
        }

        /**
         * Returns metadata of entry. For bullet entry, this is null
         * @return null
         */
        @Override
        public Object getMetadata() {
            return null;
        }

        @NonNull
        @Override
        public String getEntryText() {
            return entryText;
        }

        @NonNull
        @Override
        public String getEncodeTypeIdentifier() {
            return TypeIdentifier.$BULLET_ENTRY$.toString();
        }

    }

}

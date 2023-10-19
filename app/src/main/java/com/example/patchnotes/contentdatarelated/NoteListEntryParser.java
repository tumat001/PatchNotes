package com.example.patchnotes.contentdatarelated;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.NoteList;

public class NoteListEntryParser extends DataParser<NoteList.Entry<?>> {

    private static final String ENCODING_HEADER = "[NoteListEntry§¬";
    private static final String ENCODING_SEPARATOR = "±NoteListEntry÷±";
    private static final String ENCODING_FOOTER = "¬§NoteListEntry]";

    private static final int NUMBER_OF_MEMBER_DATA_PER_ENTRY = 3;

    @NonNull
    @Override
    public String getEncodingOfData(@NonNull NoteList.Entry<?> entry) {
        String typeIdentifier = entry.getEncodeTypeIdentifier();
        String mainText = entry.getEntryText();
        String metadataText;
        if (entry.getMetadata() != null) {
            metadataText = entry.getMetadata().toString();
        } else {
            metadataText = "noData";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(ENCODING_HEADER)
                .append(typeIdentifier)
                .append(ENCODING_SEPARATOR)
                .append(metadataText)
                .append(ENCODING_SEPARATOR)
                .append(mainText)
                .append(ENCODING_FOOTER);

        return builder.toString();
    }

    @NonNull
    @Override
    public NoteList.Entry<?> parseEncodingIntoData(@NonNull String toParse) throws ParseDataException {
        if (!isParsableIntoData(toParse)) {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_DATA_FORMAT);
        }

        String[] entryElements = getEntryElements(toParse);
        NoteList.Entry.TypeIdentifier typeIdentifier = getTypeFromIdentifier(entryElements[0]);

        return constructEntry(typeIdentifier, entryElements[1], entryElements[2]);
    }

    @Override
    public boolean isParsableIntoData(@NonNull String toParse) {
        try {
            return toParse.substring(0, ENCODING_HEADER.length()).equals(ENCODING_HEADER)
                    && toParse.substring(toParse.length() - ENCODING_FOOTER.length()).equals(ENCODING_FOOTER)
                    && toParse.split(ENCODING_SEPARATOR).length == NUMBER_OF_MEMBER_DATA_PER_ENTRY;
        } catch (StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    private String[] getEntryElements(String toParse) {
        toParse = toParse.substring(ENCODING_HEADER.length(), toParse.length() - ENCODING_FOOTER.length());

        String[] entryElements = new String[] {"", "", ""};
        String[] extractedElements = toParse.split(ENCODING_SEPARATOR);
        System.arraycopy(extractedElements, 0, entryElements, 0, extractedElements.length);

        return entryElements;
    }

    private NoteList.Entry.TypeIdentifier getTypeFromIdentifier(String identifierString) throws ParseDataException {
        if (identifierString.isEmpty()) {
            throw new ParseDataException(ParseDataException.ErrorType.MISSING_TYPE_IDENTIFIER);
        }

        NoteList.Entry.TypeIdentifier identifier = NoteList.Entry.getIdentifierFromString(identifierString);
        if (identifier == null) {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_TYPE_IDENTIFIER);
        }
        return identifier;
    }

    @NonNull
    private NoteList.Entry<?> constructEntry(NoteList.Entry.TypeIdentifier identifier, String metadata, String mainText)
            throws ParseDataException {
        if (identifier == NoteList.Entry.TypeIdentifier.$CHECK_ENTRY$) {
            return constructCheckEntry(metadata, mainText);
        } else if (identifier == NoteList.Entry.TypeIdentifier.$PICTURE_ENTRY$) {
            return constructPictureEntry(metadata, mainText);
        } else if (identifier == NoteList.Entry.TypeIdentifier.$NUMBER_ENTRY$) {
            return constructNumberEntry(metadata, mainText);
        } else if (identifier == NoteList.Entry.TypeIdentifier.$BULLET_ENTRY$) {
            return constructBulletEntry(mainText);
        } else {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_TYPE_IDENTIFIER,
                    "Offending value: " + identifier.toString());
        }
    }

    private NoteList.CheckEntry constructCheckEntry(String metadata, String mainText) throws ParseDataException {
        if (!metadata.equalsIgnoreCase("true") && !metadata.equalsIgnoreCase("false")) {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_METADATA, "Offending value: " + metadata);
        }

        boolean isChecked = Boolean.parseBoolean(metadata);
        return new NoteList.CheckEntry(mainText, isChecked);
    }

    private NoteList.PictureEntry constructPictureEntry(String metadata, String mainText) throws ParseDataException {
        int pictureRes;
        try {
            pictureRes = Integer.parseInt(metadata);
        } catch (NumberFormatException e) {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_METADATA, "Offending value: " + metadata);
        }

        return new NoteList.PictureEntry(mainText, pictureRes);
    }

    private NoteList.NumberEntry constructNumberEntry(String metadata, String mainText) throws ParseDataException {
        int numberIndex;
        try {
            numberIndex = Integer.parseInt(metadata);
        } catch (NumberFormatException e) {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_METADATA, "Offending value: " + metadata);
        }

        return new NoteList.NumberEntry(mainText, numberIndex);
    }

    private NoteList.BulletEntry constructBulletEntry(String mainText) {
        return new NoteList.BulletEntry(mainText, null);
    }

}

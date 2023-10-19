package com.example.patchnotes.contentdatarelated;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.patchnotes.contentrelated.NoteList;

import java.util.ArrayList;
import java.util.List;

public class DataListParser<T, U extends DataParser<T>> extends DataParser<List<T>> {

    private static final String ENCODING_HEADER = "[LDP§¬";
    private static final String ENCODING_LIST_DATA_SEPARATOR = "±LDP÷±";
    private static final String ENCODING_FOOTER = "¬§LDP]";

    private U dataParserForElements;

    public DataListParser(@NonNull U dataParserForElements) {
        this.dataParserForElements = dataParserForElements;
    }

    @NonNull
    @Override
    public String getEncodingOfData(@NonNull List<T> list) {
        StringBuilder builder = new StringBuilder();
        builder.append(ENCODING_HEADER);

        for (int i = 0; i < list.size(); i++) {
            builder.append(getEncodingOfListElement(list.get(i)));
            if (!isLastIndex(i, list)) {
                builder.append(ENCODING_LIST_DATA_SEPARATOR);
            }
        }

        builder.append(ENCODING_FOOTER);

        return builder.toString();
    }

    private String getEncodingOfListElement(T element) {
        return dataParserForElements.getEncodingOfData(element);
    }

    private boolean isLastIndex(int index, List<?> list) {
        return list.size() - 1 == index;
    }


    @NonNull
    @Override
    public List<T> parseEncodingIntoData(@NonNull String toParse) throws ParseDataException {
        if (!isParsableIntoData(toParse)) {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_DATA_FORMAT);
        }

        String strippedParse = getStrippedHeaderAndFooterOf(toParse);
        String[] listElements = strippedParse.split(ENCODING_LIST_DATA_SEPARATOR);

        List<T> bucket = new ArrayList<>();
        for (String toParseElement: listElements) {
            if (!toParseElement.isEmpty()) {
                bucket.add(parseElement(toParseElement));
            }
        }
        return bucket;
    }

    /**
     * Checks string to see if it contains the specific header and footer at the appropriate location. This method
     * does not check the validity of the contents to be converted however therefore
     * {@link DataListParser#parseEncodingIntoData(String)} may still fail
     * @param toParse the string to parse
     * @return true if string passed is a valid list of data.
     */
    @Override
    public boolean isParsableIntoData(@NonNull String toParse) {
        return toParse.substring(0, ENCODING_HEADER.length()).equals(ENCODING_HEADER) &&
                toParse.substring(toParse.length() - ENCODING_FOOTER.length()).equals(ENCODING_FOOTER);
    }

    private String getStrippedHeaderAndFooterOf(String toParse) {
        return toParse.substring(ENCODING_HEADER.length(), toParse.length() - ENCODING_FOOTER.length());
    }

    private T parseElement(String toParseElement) throws ParseDataException {
        try {
            return dataParserForElements.parseEncodingIntoData(toParseElement);
        } catch (ParseDataException e) {
            throw new ParseDataException(ParseDataException.ErrorType.INVALID_CHILD_DATA_FORMAT, "Child exception: " + e.toString());
        }
    }

}

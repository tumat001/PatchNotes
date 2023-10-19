package com.example.patchnotes.contentdatarelated;

import androidx.annotation.NonNull;

public class ParseDataException extends RuntimeException {

    enum ErrorType {
        INVALID_DATA_FORMAT("String not parsable into data. Has no encoding characters. "),
        MISSING_TYPE_IDENTIFIER("String not parsable into entry. Has no type identifier. "),
        INVALID_TYPE_IDENTIFIER("String not parsable into entry. Type identifier specified does not correspond to any type. "),
        MISSING_METADATA("String not parsable into entry. Has no metadata. "),
        INVALID_METADATA("String not parsable into entry. Metadata is invalid. "),

        INVALID_CHILD_DATA_FORMAT("String not parsable into list of data. Child data is not valid. ");

        String errorMessage;

        ErrorType(String msg) {
            this.errorMessage = msg;
        }
    }

    private ErrorType errorType;
    private String extraMessage;

    ParseDataException(ErrorType errorType) {
        this(errorType, "");
    }

    ParseDataException(ErrorType errorType, String extraMessage) {
        this.errorType = errorType;
        this.extraMessage = extraMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return errorType.errorMessage + extraMessage;
    }

}

package com.example.patchnotes.contentdatarelated;

import androidx.annotation.NonNull;

public abstract class DataParser<T> {
    @NonNull abstract public String getEncodingOfData(@NonNull T data);
    abstract public T parseEncodingIntoData(@NonNull String toParse);
    abstract public boolean isParsableIntoData(@NonNull String toParse);
}

package com.arash.basemodule.contracts;

public interface Consumer<T> {
    void accept(T t);
}

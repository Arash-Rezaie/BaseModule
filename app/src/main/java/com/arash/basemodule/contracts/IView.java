package com.arash.basemodule.contracts;

public interface IView<T> {
    void setValue(T t);

    T getValue();
}

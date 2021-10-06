package com.arash.basemodule.contracts;

public interface Observable<T extends Object> {
    void setValue(T t);

    T getValue();

    void observe(Observer<T> observer);

    void removeObserver(Observer<T> observer);

    void notifyObservers();
}

package com.arash.basemodule.tools;

import com.arash.basemodule.contracts.Observable;
import com.arash.basemodule.contracts.Observer;

import java.util.LinkedList;

public class ObservableImp<T> implements Observable<T> {
    private T t;
    private LinkedList<Observer<T>> listeners = new LinkedList<>();

    public ObservableImp() {
    }

    public ObservableImp(T t) {
        this.t = t;
    }

    @Override
    public synchronized void setValue(T t) {
        this.t = t;
        notifyObservers();
    }

    @Override
    public T getValue() {
        return t;
    }

    @Override
    public synchronized void observe(Observer<T> observer) {
        listeners.add((Observer<T>) observer);
    }

    @Override
    public synchronized void removeObserver(Observer<T> observer) {
        listeners.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (int i = listeners.size() - 1; i >= 0; i--) {
            try {
                listeners.get(i).accept(t);
            } catch (Exception e) {
                Utils.log(e);
                listeners.remove(i);
            }
        }
    }
}

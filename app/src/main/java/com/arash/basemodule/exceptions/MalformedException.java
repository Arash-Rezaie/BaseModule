package com.arash.basemodule.exceptions;

import android.view.View;

public class MalformedException extends RuntimeException {
    private View targetView;
    public MalformedException() {
    }

    public MalformedException(String message) {
        super(message);
    }

    public MalformedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedException(Throwable cause) {
        super(cause);
    }


    public MalformedException(View targetView) {
        this.targetView = targetView;
    }

    public View getTargetView() {
        return targetView;
    }
}

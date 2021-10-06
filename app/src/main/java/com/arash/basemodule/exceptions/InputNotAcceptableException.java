package com.arash.basemodule.exceptions;

import android.view.View;

public class InputNotAcceptableException extends RuntimeException {
    private View targetView;

    public InputNotAcceptableException() {
    }

    public InputNotAcceptableException(String message) {
        super(message);
    }

    public InputNotAcceptableException(String message, Throwable cause) {
        super(message, cause);
    }

    public InputNotAcceptableException(Throwable cause) {
        super(cause);
    }

    public View getTargetView() {
        return targetView;
    }

    public InputNotAcceptableException(View targetView) {


        this.targetView = targetView;
    }
}

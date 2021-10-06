package com.arash.basemodule.exceptions;

import android.view.View;

public class EmptyException extends RuntimeException {
    private View targetView;

    public EmptyException() {
    }

    public EmptyException(String message) {
        super(message);
    }

    public EmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyException(Throwable cause) {
        super(cause);
    }

    public EmptyException(View targetView) {
        this.targetView = targetView;
    }

    public View getTargetView() {
        return targetView;
    }
}

package com.arash.basemodule.constants;

public enum RequestCode {
    REQUEST_PERMISSION(100);
    public final int codeValue;

    RequestCode(int code) {
        codeValue = code;
    }
}

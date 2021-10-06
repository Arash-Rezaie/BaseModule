package com.arash.basemodule.contracts;

public interface Function<INPUT, OUTPUT> {
    OUTPUT apply(INPUT input);
}

package com.arash.basemodule.tools.vmvglue.contracts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * bind an xml id to the mentioned variable
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XmlBindInfo {
    /**
     * @return exp: R.id.viewId or R.string.test
     */
    int value();
}

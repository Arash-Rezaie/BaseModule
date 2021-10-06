package com.arash.basemodule.tools.vmvglue.contracts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VmBindInfo {
    /**
     * @return setter method of view element such as setText() method of EditText class
     */
    String elmSetter() default "";

    /**
     * @return parameters type of element setter method to pick the method accurately
     */
    Class<?> elmSetterParam() default Any.class;

    /**
     * @return method name by which view element value can be read
     */
    String elmGetter() default "";

    /**
     * You must provide a named ViewListener<T> instance via Feather at first.
     * It is up to register desired event listener in the taken view. then pass its name here
     *
     * @return ViewListener name through Feather
     */
    String elmEventRegistererName() default "";

    /**
     * @return setter method of view-model such as setName() method of MyViewModel class
     */
    String vmSetter() default "";

    /**
     * @return parameters type of view-model setter method to pick the method accurately
     */
    Class<?> vmSetterParam() default Any.class;

    /**
     * @return method in view-model to catch the variable
     */
    String vmGetter() default "";

    /**
     * @return determine if view must register for view-model changes. By this way, vmGetter() must return a method with Observable<T> as output
     */
    boolean registerForVmChanges() default false;
}

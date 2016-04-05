package com.kentchiu.spring.base.domain;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE})
public @interface ResourceDoc {

    String title();

    String include() default "";

    Class type();

}
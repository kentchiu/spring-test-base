package com.kentchiu.spring.base.domain;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, TYPE})
public @interface ApiDoc {

    int order() default Integer.MAX_VALUE;

    String title();

    String postSnippet() default "";

}
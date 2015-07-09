package com.kentchiu.spring.base.web;

public abstract class MockMvcResultMatchers {
    private MockMvcResultMatchers() {
    }

//    public static FieldResultMatchers field(String name) {
//        return new FieldResultMatchers(name);
//    }

    public static FieldErrorResultMatchers field(String name) {
        return new FieldErrorResultMatchers(name);
    }

    public static RestErrorResultMatchers error() {
        return new RestErrorResultMatchers();
    }
}

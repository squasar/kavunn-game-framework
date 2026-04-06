<<<<<<< HEAD
package com.example.core;
=======
package core;
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)

import java.util.HashMap;
import java.util.Map;

//Intended to manage or manipulate the parameters, variables or valuable attributes of a given object which extends Entity in real time

public class Params {
    private Map<String, ParamValue> kwargs = new HashMap<>();

    public Params(String key, ParamValue value) {
        this.add(key, value);
    }

    public void add(String key, ParamValue value) {
        this.kwargs.put(key, value);
    }

    public static Params of(String key, int value) {
        return new Params(key, PrimaryTypeValue.integer(value));
    }

    public static Params of(String key, double value) {
        return new Params(key, PrimaryTypeValue.doubleVal(value));
    }

    public static Params of(String key, String value) {
        return new Params(key, PrimaryTypeValue.string(value));
    }

    public static Params of(String key, boolean value) {
        return new Params(key, PrimaryTypeValue.bool(value));
    }

    public static <T> Params of(String key, T value) {
        return new Params(key, PrimaryTypeValue.of(value));
    }

    public ParamValue get(String key) {
        return this.kwargs.get(key);
    }

    // for debugging
    @Override
    public String toString() {
        return "Params{" + "kwargs=" + kwargs + '}';
    }
}

<<<<<<< HEAD
package com.example.core;
=======
package core;
>>>>>>> d667dbd (expand Kavunn engine scope with Ashwake and engine subsystems)

//specialized for managing primary data types as Params object.

public class PrimaryTypeValue<T> implements ParamValue {
    private final T value;

    public PrimaryTypeValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public static PrimaryTypeValue<Integer> integer(int value) {
        return new PrimaryTypeValue<>(value);
    }

    public static PrimaryTypeValue<Double> doubleVal(double value) {
        return new PrimaryTypeValue<>(value);
    }

    public static PrimaryTypeValue<String> string(String value) {
        return new PrimaryTypeValue<>(value);
    }

    public static PrimaryTypeValue<Boolean> bool(boolean value) {
        return new PrimaryTypeValue<>(value);
    }

    public static <T> PrimaryTypeValue<T> of(T value) {
        return new PrimaryTypeValue<>(value);
    }
}


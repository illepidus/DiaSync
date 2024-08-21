package ru.krotarnya.diasync.common.util;

public class Functions {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object s, Class<T> clazz) {
        return (T) s;
    }
}

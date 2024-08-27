package ru.krotarnya.diasync.common.util;

import java.util.Queue;

public class Functions {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object s, Class<T> clazz) {
        return (T) s;
    }

    public static <T> Queue<T> union(Queue<T> q1, Queue<T> q2) {
        q1.addAll(q2);
        return q1;
    }
}

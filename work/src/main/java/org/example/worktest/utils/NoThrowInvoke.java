package org.example.worktest.utils;

public class NoThrowInvoke {
    public interface Func<R, E extends Throwable> {
        R invoke() throws E;
    }

    @SuppressWarnings("unchecked")
    public static <R> R invoke(Func<R, ? extends Throwable> f) {
        return ((Func<R, RuntimeException>) f).invoke();
    }
}

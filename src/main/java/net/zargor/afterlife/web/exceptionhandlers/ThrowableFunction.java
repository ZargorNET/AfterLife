package net.zargor.afterlife.web.exceptionhandlers;

import java.util.function.*;

@FunctionalInterface
public interface ThrowableFunction<T, R> extends Function<T, R> {

    @Override
    default R apply(T t) {
        try {
            return applyThrows(t);
        } catch (final Exception exe) {
            exe.printStackTrace(); //TODO
        }
        return null;
    }

    R applyThrows(T elem) throws Exception;
}
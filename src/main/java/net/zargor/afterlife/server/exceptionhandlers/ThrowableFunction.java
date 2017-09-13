package net.zargor.afterlife.server.exceptionhandlers;

import java.util.function.Function;

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
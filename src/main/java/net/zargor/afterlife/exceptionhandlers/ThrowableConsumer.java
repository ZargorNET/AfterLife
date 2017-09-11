package net.zargor.afterlife.exceptionhandlers;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableConsumer<T> extends Consumer<T> {

	@Override
	default void accept(T t) {
		try {
			acceptThrows(t);
		} catch (Exception exe) {
			exe.printStackTrace();
		}
	}

	void acceptThrows(T t) throws Exception;
}

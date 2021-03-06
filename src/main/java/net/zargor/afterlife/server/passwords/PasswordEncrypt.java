package net.zargor.afterlife.server.passwords;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import lombok.Getter;
import net.zargor.afterlife.server.WebServer;
import net.zargor.afterlife.server.exceptionhandlers.ThrowableFunction;
import net.zargor.afterlife.server.passwords.algorithms.PasswordHashingAlgorithm;

/**
 * Encryption: SHA-512
 */
public class PasswordEncrypt {

	@Getter
	private String salt;
	@Getter
	private PasswordHashingAlgorithm algorithm;

	public PasswordEncrypt() {
		salt = WebServer.getInstance().getConfig().getValue("password_salt");
		try {
			algorithm = ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.zargor.afterlife.server.passwords.algorithms").stream()
					.map((ThrowableFunction<? super ClassPath.ClassInfo, Class>) classInfo -> Class.forName(classInfo.getName()))
					.filter(clazz -> clazz.getSuperclass().equals(PasswordHashingAlgorithm.class))
					.map((ThrowableFunction<? super Class, PasswordHashingAlgorithm>) clazz -> (PasswordHashingAlgorithm) clazz.getDeclaredConstructor(String.class).newInstance(salt))
					.filter(pwa -> pwa.getName().equalsIgnoreCase(WebServer.getInstance().getConfig().getValue("password_method")))
					.findFirst().orElse(null);
			if (algorithm == null) {
				System.err.println("Couldnt find the class for the hashing method (\"" + ((String) WebServer.getInstance().getConfig().getValue("password_method")).toLowerCase() + "\")! Did you use a valid method?");
				System.exit(-1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

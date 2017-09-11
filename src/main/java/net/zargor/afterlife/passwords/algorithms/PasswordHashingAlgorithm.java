package net.zargor.afterlife.passwords.algorithms;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public abstract class PasswordHashingAlgorithm {

	private final String name;
	private String salt;

	/**
	 * Hashes the given passwords and returns it
	 *
	 * @param password The unhashed password
	 * @param salt     The salt from the config
	 * @return The hashed password
	 */
	public abstract String hashPassword(String password);

	/**
	 * Checks if an unhashed password matches the hashed one
	 *
	 * @param unhashed The unhashed password
	 * @param hashed   The hashed password
	 * @param salt     The salt from the config
	 * @return True if they're matching else false
	 */
	public abstract boolean checkPassword(String unhashed, String hashed);
}

package net.zargor.afterlife.server.objects;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simple user
 */
@Data
@AllArgsConstructor
public class User {

	private String name;
	private String email;
	private byte[] avatar;
	private Map<String, String> informations;
	private Group group;
}
package net.zargor.afterlife.server.objects;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simple session
 */
@Data
@AllArgsConstructor
public class Session {

	private String id;
	private String name;
	private Group group;
	private Long expireDate;
}
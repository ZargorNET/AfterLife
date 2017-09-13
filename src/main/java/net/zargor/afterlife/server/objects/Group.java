package net.zargor.afterlife.server.objects;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.zargor.afterlife.server.permissionssystem.GroupPermissions;

/**
 * A simple group
 */
@Data
@AllArgsConstructor
public class Group {

	@NonNull
	private String name;
	private List<GroupPermissions> permissions;

	public boolean hasPermission(GroupPermissions perm) {
		return permissions.contains(perm);
	}
}
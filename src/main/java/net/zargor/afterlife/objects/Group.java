package net.zargor.afterlife.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import net.zargor.afterlife.permissionssystem.GroupPermissions;

import java.util.*;

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
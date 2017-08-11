package net.zargor.afterlife.web.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.zargor.afterlife.web.GroupPermissions;

import java.util.*;

/**
 * A simple group
 */
@Data
@AllArgsConstructor
public class Group {

    private String name;
    private List<GroupPermissions> permissions;

    public boolean hasPermission(GroupPermissions perm) {
        return permissions.contains(perm);
    }
}
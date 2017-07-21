package net.zargor.afterlife.web.objects

/**
 * A simple group
 */
data class Group(val name : String, val permissions : MutableList<GroupPermissions>) {
    fun hasPermission(perm : GroupPermissions) : Boolean = permissions.any { it == perm }
}
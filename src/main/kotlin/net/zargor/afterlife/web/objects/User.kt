package net.zargor.afterlife.web.objects

import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.EqualsBuilder

/**
 * Simple user
 */
data class User(val name : String, var email : String, var avatar : Array<Byte>, val informations : MutableMap<String, String>, var group : Group) {
    override fun equals(other : Any?) : Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as User

        return EqualsBuilder()
                .append(name, other.name)
                .append(email, other.email)
                .append(avatar, other.avatar)
                .append(informations, other.informations)
                .append(group, other.group)
                .isEquals
    }
    override fun hashCode() : Int {
        return HashCodeBuilder(17, 37)
                .append(name)
                .append(email)
                .append(avatar)
                .append(informations)
                .append(group)
                .toHashCode()
    }
}
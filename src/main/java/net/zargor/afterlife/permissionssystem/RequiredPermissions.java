package net.zargor.afterlife.permissionssystem;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets needed permission for a {@link net.zargor.afterlife.requests.Module} or {@link net.zargor.afterlife.requests.PageRequest}
 * Handled by {@link net.zargor.afterlife.handlers.ClassHandler}
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredPermissions {

    GroupPermissions[] neededPermissions();
}

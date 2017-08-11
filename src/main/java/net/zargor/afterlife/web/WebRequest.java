package net.zargor.afterlife.web;

/**
 * Needed by the [HttpHandler]. Annotation defines the route
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebRequest {

    String route();

    boolean needToLogged();

    GroupPermissions[] groupNeededRights();
}
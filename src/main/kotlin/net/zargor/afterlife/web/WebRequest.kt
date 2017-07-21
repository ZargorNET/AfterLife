package net.zargor.afterlife.web

import net.zargor.afterlife.web.objects.GroupPermissions

/**
 * Needed by the [HttpHandler]. Annotation defines the route
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class WebRequest(val route: String, val permission: GroupPermissions)
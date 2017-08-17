package net.zargor.afterlife.handlers;

import com.google.common.reflect.ClassPath;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import lombok.EqualsAndHashCode;
import net.zargor.afterlife.exceptionhandlers.ThrowableConsumer;
import net.zargor.afterlife.exceptionhandlers.ThrowableFunction;
import net.zargor.afterlife.objects.FullHttpReq;
import net.zargor.afterlife.permissionssystem.GroupPermissions;
import net.zargor.afterlife.permissionssystem.RequiredPermissions;
import net.zargor.afterlife.requests.WebRequest;

import java.io.*;
import java.util.*;
import java.util.stream.*;


@EqualsAndHashCode
abstract class ClassHandler<T extends WebRequest> {

    private List<T> list;

    public ClassHandler(List<T> list) {
        this.list = list;
    }

    public void addAllClasses(String packagePath, Class<T> tClass) throws IOException {
        ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive(packagePath).stream()
                .map((ThrowableFunction<ClassPath.ClassInfo, Class>) classInfo -> Class.forName(classInfo.getName()))
                .filter(clazz -> clazz.getSuperclass().equals(tClass))
                .map((ThrowableFunction<Class, T>) clazz -> (T) clazz.newInstance())
                .forEach((ThrowableConsumer<? super T>) t -> addClass(t, tClass));
    }

    public void addClass(T t, Class<T> tClass) throws IllegalAccessException, InstantiationException {
        if (list.stream().noneMatch(t1 -> t1.getClass().getName().equals(tClass.getName())))
            list.add(t);
    }

    public void removeAllClasses(String packagePath) {
        list.removeAll(list.stream().filter(t -> t.getClass().getPackage().getName().equalsIgnoreCase(packagePath)).collect(Collectors.toList()));
    }

    public void removeClass(T t) throws IllegalAccessException, InstantiationException {

        list.removeAll(list.stream().filter(t1 -> t1.getClass().getName().equals(t.getClass().getName())).collect(Collectors.toList()));
    }

    public List<T> getList() {
        return list;
    }


    /**
     * Called from {@link NettyHttpRequestHandler}
     *
     * @return A full {@link DefaultFullHttpResponse}
     */
    abstract DefaultFullHttpResponse onRequest(ChannelHandlerContext ctx, FullHttpReq req);

    /**
     * Called when the user reaches the request limit. See also {@link ClassHandler}
     *
     * @return Returns a full {@link DefaultFullHttpResponse}
     */
    abstract DefaultFullHttpResponse onTooManyRequests(ChannelHandlerContext ctx, FullHttpReq req, TooManyRequestHandler.ConnectionRequestAmount cra);

    /**
     * @param t   The object
     * @param req Request
     * @return The needed permissions. If its null it means that the user has enough permissions
     * @throws Exception All kind of exceptions
     */
    GroupPermissions[] permissionFailure(T t, FullHttpReq req) throws Exception {
        RequiredPermissions requiredPermissions = t.getClass().getDeclaredAnnotation(RequiredPermissions.class);
        if (requiredPermissions != null) {
            if (req.getGroup() == null)
                return requiredPermissions.neededPermissions();
            if (!Arrays.stream(requiredPermissions.neededPermissions()).allMatch(permission -> req.getGroup().hasPermission(permission)))
                return requiredPermissions.neededPermissions();
        }
        return null;
    }
}

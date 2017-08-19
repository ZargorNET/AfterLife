package net.zargor.afterlife.permissionssystem;


import net.zargor.afterlife.WebServer;
import net.zargor.afterlife.objects.Group;
import net.zargor.afterlife.objects.Session;
import org.apache.commons.lang3.time.DateUtils;

import java.util.*;
import java.util.stream.*;

/**
 * Session management for the http server
 */
public class SessionManagement {

    private final List<Session> sessions = new ArrayList<>();


    public SessionManagement() {
        new Thread(() -> {
            while (true) {
                try {
                    synchronized (sessions) {
                        Date date = new Date();
                        sessions.removeAll(sessions.stream().filter(session -> session.getExpireDate() <= date.getTime()).collect(Collectors.toList()));
                    }
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Sessionthread").start();
    }

    public synchronized String addSession(String name, Group group) {
        Optional<Session> sessionOptional = sessions.stream().filter(s -> s.getName().equals(name)).findFirst();
        if (sessionOptional.isPresent())
            return null;
        String sID = generateSessionID();
        sessions.add(new Session(sID, name, group, DateUtils.addDays(new Date(), 1).getTime()));
        return sID;
    }

    public synchronized void remSession(String id) {
        Optional<Session> sessionOptional = sessions.stream().filter(s -> s.getId().equals(id)).findFirst();
        sessionOptional.ifPresent(sessions::remove);
    }

    public synchronized Session getSessionByName(String name) {
        Optional<Session> sessionOptional = sessions.stream().filter(s -> s.getName().equals(name)).findFirst();
        return sessionOptional.orElse(null);
    }

    public synchronized Session getSessionByID(String id) {
        Optional<Session> sessionOptional = sessions.stream().filter(s -> s.getId().equals(id)).findFirst();
        return sessionOptional.orElse(null);

    }

    public String createCookieString(String name, Group group, boolean secure) {
        return String.format("z-sID=%s; Expires=%s; HttpOnly; %s", addSession(name, group), DateUtils.addDays(new Date(), 1).toGMTString(), secure ? "Secure;" : "");
    }

    public String createCookieString(String name, String groupName, boolean secure) {
        return String.format("z-sID=%s; Expires=%s; HttpOnly; %s", addSession(name, WebServer.getInstance().getGroupManagement().getGroup(groupName)), DateUtils.addDays(new Date(), 1).toGMTString(), secure ? "Secure;" : "");
    }

    private String generateSessionID() {
        String s = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replace("-", "");
        if (sessions.stream().anyMatch(session -> session.getId().equals(s)))
            return generateSessionID();
        return s;
    }
}
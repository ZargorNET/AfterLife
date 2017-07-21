package net.zargor.afterlife.web

import net.zargor.afterlife.web.objects.Group
import net.zargor.afterlife.web.objects.Session
import org.apache.commons.lang3.time.DateUtils
import java.util.*

/**
 * Session management for the http server
 */
class SessionManagement(val main: WebServer) {
    private val sessions : MutableMap<String, Session> = mutableMapOf()

    @Synchronized
    fun addSession(id : String, group : Group) : String?{
        if (sessions.containsKey(id))
            return null
        val sID = generateSessionID()
        sessions.put(sID, Session(id, group))
        return sID
    }

    @Synchronized
    fun remSession(id : String) {
        if (sessions.containsKey(id)) {
            sessions.remove(id)
        }
    }

    @Synchronized
    fun getSessionsByName(name : String) : Session? {
        for((_,v) in sessions){
            if(v.name == name){
                return v
            }
        }
        return null
    }

    @Synchronized
    fun getSessionsByID(id : String) : Session? {
        if(sessions.containsKey(id)){
            return sessions[id]
        }
        return null
    }

    private fun generateSessionID() : String {
        val s = UUID.randomUUID().toString().replace("-","")
        if (sessions.containsKey(s)) {
            return generateSessionID()
        }
        return s
    }

    @Synchronized
    fun createCookieString(name : String, group: Group, secure : Boolean) : String =
            "z-sID=${addSession(name
            , group)}" +
            "; Expires=${DateUtils.addDays(Date(), 1).toGMTString()}" +
            "; HttpOnly" +
            "; ${if (secure) "Secure;" else ""}"

    @Synchronized
    fun createCookieString(name : String,groupName: String, secure : Boolean) : String {
        val group = main.handler.groupM.getGroup(groupName) ?: throw IllegalAccessException("Group not found!")
        return "z-sID=${addSession(name
                , group)}" +
                "; Expires=${DateUtils.addDays(Date(), 1).toGMTString()}" +
                "; HttpOnly" +
                "; ${if (secure) "Secure;" else ""}"
    }

}

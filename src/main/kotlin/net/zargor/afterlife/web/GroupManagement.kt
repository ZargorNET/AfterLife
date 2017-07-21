package net.zargor.afterlife.web

import com.mongodb.util.JSON
import net.zargor.afterlife.web.objects.Group
import net.zargor.afterlife.web.objects.Session
import org.bson.Document

/**
 * Created by Zargor on 20.07.2017.
 */
class GroupManagement(val main : WebServer) {
    private val groups : MutableList<Group> = mutableListOf()

    init {
        val found : List<Document>? = main.mongoDB.groupColl?.find()?.toList()
        if (found != null) {
            if (!found.isEmpty()) {
                found.forEach { groups.add(main.gson.fromJson(it.toJson(), Group::class.java)) }
            }
        }
    }

    @Synchronized
    fun getGroup(name : String) : Group? = groups.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }

    @Synchronized
    fun getGroups() : MutableList<Group> = groups

    @Synchronized
    fun createGroup(group : Group) {
        if(!groups.contains(group)){
            groups.add(group)
            main.mongoDB.groupColl?.insertOne(JSON.parse(main.gson.toJson(group)) as Document?)
        }
    }
    @Synchronized
    fun deleteGroup(group : Group){
        if(groups.contains(group)){
            groups.remove(group)
            main.mongoDB.groupColl?.deleteOne(Document("name",group.name))
        }
    }
}

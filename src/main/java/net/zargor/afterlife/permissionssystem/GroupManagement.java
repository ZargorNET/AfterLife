package net.zargor.afterlife.permissionssystem;


import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.zargor.afterlife.WebServer;
import net.zargor.afterlife.objects.Group;
import org.bson.Document;

/**
 * Created by Zargor on 20.07.2017.
 */
public class GroupManagement {

	private List<Group> groups = new ArrayList<>();

	public GroupManagement() {
		List<Document> foundDocs = new ArrayList<>();
		WebServer.getInstance().getMongoDB().getGroupColl().find().forEach((Consumer<? super Document>) foundDocs::add);
		if (!foundDocs.isEmpty()) {
			foundDocs.forEach(d -> groups.add(WebServer.getInstance().getGson().fromJson(d.toJson(), Group.class)));
		}
		//TODO Defaultgruppe in die DB
		groups.add(new Group("default", new ArrayList<GroupPermissions>() {{
			add(GroupPermissions.DEFAULT);
		}}));
	}

	public synchronized Group getGroup(String name) {
		return groups.stream().filter(g -> Objects.equals(g.getName().toLowerCase(), name.toLowerCase())).findFirst().orElseGet(null);
	}

	public synchronized List<Group> getGroups() {
		return groups;
	}

	public synchronized void createGroup(Group group) {
		if (!groups.contains(group)) {
			groups.add(group);
			WebServer.getInstance().getMongoDB().getGroupColl().insertOne((Document) JSON.parse(WebServer.getInstance().getGson().toJson(group)));
		}
	}

	public synchronized void remGroup(Group group) {
		if (groups.contains(group)) {
			groups.remove(group);
			WebServer.getInstance().getMongoDB().getGroupColl().deleteOne(new Document("name", group.getName()));
		}
	}
}

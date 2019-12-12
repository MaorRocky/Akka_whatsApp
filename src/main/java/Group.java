import akka.actor.AbstractActor;
import akka.actor.Props;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class Group extends AbstractActor implements Serializable {
    private String groupName;
    private User admin;
    protected List<User> co_admins_list;
    protected HashMap<String, User> groupUsersMap;

    static public Props props(String groupName, User admin) {
        return Props.create(Group.class, () -> new Group(groupName, admin));
    }


    public Group(String groupName, User admin) {
        this.groupName = groupName;
        this.admin = admin;
        groupUsersMap = new HashMap<>();
        groupUsersMap.put(admin.getUserName(), admin);
        this.co_admins_list = new LinkedList<>();
    }


    /**************************GETTERS***********************/
    public String getGroupName() {
        return groupName;
    }

    public User getAdmin() {
        return admin;
    }

    public HashMap<String, User> getGroupUsersMap() {
        return groupUsersMap;
    }

    public void addUser(User user) {
        groupUsersMap.put(user.getUserName(), user);
    }

    public boolean remove(User user) {
        if (groupUsersMap.remove(user.getUserName(), user)) {
            return true;
        } else return false;
    }

    public boolean promot_co_admin(User user) {
        if (groupUsersMap.containsKey(user.getUserName())) {
            co_admins_list.add(user);
            return true;
        } else
            return false;
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupName='" + groupName + '\'' +
                ", admin=" + admin +
                ", co_admins_list=" + co_admins_list +
                ", groupUsersMap=" + groupUsersMap +
                '}';
    }
}

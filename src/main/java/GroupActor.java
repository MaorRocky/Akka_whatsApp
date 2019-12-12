import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.HashMap;


public class GroupActor extends AbstractActor {
    private String groupName;
    private User admin;
    protected User co_admin;


    protected HashMap<String, User> groupUsersMap;

    static public Props props(String groupName, User admin) {
        return Props.create(GroupActor.class, () -> new GroupActor(groupName, admin));
    }


    public GroupActor(String groupName, User admin) {
        this.groupName = groupName;
        this.admin = admin;
        groupUsersMap = new HashMap<>();
        groupUsersMap.put(admin.getUserName(), admin);

    }

    @Override
    public Receive createReceive() {
        return null;
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

}

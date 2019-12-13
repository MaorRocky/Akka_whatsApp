import akka.actor.AbstractActor;
import akka.actor.ActorRef;
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
    private Predicates predicates;

    static public Props props(String groupName, User admin) {
        return Props.create(Group.class, () -> new Group(groupName, admin));
    }


    public Group(String groupName, User admin) {
        this.groupName = groupName;
        this.admin = admin;
        groupUsersMap = new HashMap<>();
        groupUsersMap.put(admin.getUserName(), admin);
        this.co_admins_list = new LinkedList<>();
        predicates = new Predicates();


    }

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
        printFromGroupsConnection("groupUsersMap is :\n" + groupUsersMap.toString());
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

    private void checkValidInvitation(InviteGroup inviteGroup, ActorRef UserActor) {

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

    private void printFromGroupsConnection(String message) {
        getContext().parent().tell(message, getSelf());
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(InviteGroup.class, predicates.GroupInviteGroup, (invitation) ->
                        checkValidInvitation(invitation, sender()))
                .matchAny((cmd) -> printFromGroupsConnection(cmd.toString()))
                .build();
    }
}

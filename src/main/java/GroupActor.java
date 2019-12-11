import akka.actor.AbstractActor;
import akka.actor.Props;


public class GroupActor extends AbstractActor {
    private String groupName;
    private User admin;

    static public Props props(String groupName, User admin) {
        return Props.create(GroupActor.class, () -> new GroupActor(groupName, admin));
    }


    public GroupActor(String groupName, User admin) {
        this.groupName = groupName;
        this.admin = admin;

    }

    @Override
    public Receive createReceive() {
        return null;
    }

    public String getGroupName() {
        return groupName;
    }

    public User getAdmin() {
        return admin;
    }
}

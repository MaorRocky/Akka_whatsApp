import akka.actor.ActorRef;

import java.io.Serializable;


public class CreateGroupCommand extends Command implements Serializable {
    private User userAdmin;
    private String groupName;


    public CreateGroupCommand(String userName, String[] str, From from, Type type) {
        super(type, from);
        this.userAdmin = new User(userName);
        if (!type.equals(Type.Disconnect))
            this.groupName = str[1];
    }

    public User getUserAdmin() {
        return userAdmin;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setUserAdmin(User userAdmin) {
        this.userAdmin = userAdmin;
    }

    public void setUserRef(ActorRef ref) {
        this.userAdmin.setUserActorRef(ref);
    }
}

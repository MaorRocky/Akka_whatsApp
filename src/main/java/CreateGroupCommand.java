
import akka.actor.ActorRef;

import java.io.Serializable;


public class CreateGroupCommand extends GroupCommand implements Serializable {
    protected User userAdmin;
    protected String groupName;
    private ActorRef groupRef;


    public CreateGroupCommand(String[] str, From from, Type type,String userName) {
        super(type, from);
        userAdmin = new User(userName);
        if (!type.equals(Type.Disconnect))
            this.groupName  = str[0];
    }

    public CreateGroupCommand(Type type, From from) {
        super(type, from);
    }

    public ActorRef getGroupRef() {
        return groupRef;
    }

    public void setGroupRef(ActorRef groupRef) {
        this.groupRef = groupRef;
    }

    public User getSourceUser() {
        return userAdmin;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setSourceUser(User sourceUser) {
        this.userAdmin = sourceUser;
    }

}

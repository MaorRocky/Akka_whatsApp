import akka.actor.ActorRef;

import java.io.Serializable;


public class ConnectCommand extends Command implements Serializable {
    private User user;
    private String groupName;

    public ConnectCommand(String[] str, From from){
        super(Type.Connect, from);
        user = new User(str[0]);
    }

    public ConnectCommand(String userName ,String[] str, From from, Type type){
        super(type, from);
        this.user = new User(userName);
        if (!type.equals(Type.Disconnect))
            this.groupName  = str[1];
    }

    public User getUser() {
        return user;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserRef(ActorRef ref){
        this.user.setUserActorRef(ref);
    }
}

import akka.actor.ActorRef;

import java.io.Serializable;


public class ConnectCommand extends Command implements Serializable {
    private User user;

    public ConnectCommand(String[] str, From from) {
        super(Type.Connect, from);
        user = new User(str[0]);
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserRef(ActorRef ref) {
        this.user.setUserActorRef(ref);
    }
}

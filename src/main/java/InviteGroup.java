
import akka.actor.ActorRef;

import java.io.Serializable;


public class InviteGroup extends CreateGroupCommand implements Serializable {
    private User userAdmin;
    private String groupName;
    private String userToInvite;
    private ActorRef actorRefToInvite = null;


    public InviteGroup(String[] str, From from, Type type) {
        super(str, from, type);
        this.groupName = str[2];
        this.userToInvite = str[3];
    }

    public User getUserAdmin() {
        return userAdmin;
    }

    public String getUserToInvite() {
        return userToInvite;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setUserAdmin(User userAdmin) {
        this.userAdmin = userAdmin;
    }

    public void actorRefToInvite(ActorRef actorRefToInvite) {
        actorRefToInvite = actorRefToInvite;
    }

    @Override
    public String toString() {
        return "InviteGroup{" +
                "userAdmin=" + userAdmin +
                ", groupName='" + groupName + '\'' +
                ", userToInvite='" + userToInvite + '\'' +
                ", actorRefToInvite=" + actorRefToInvite +
                ", type=" + type +
                ", from=" + from +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                '}';
    }
}

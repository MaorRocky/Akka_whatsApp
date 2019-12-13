
import akka.actor.ActorRef;

import java.io.Serializable;


public class InviteGroup extends CreateGroupCommand implements Serializable {
    private User sourceUser;
    private User targerUser;
    private String target;
    private String groupName;
    private ActorRef targetActorRef = null;
    private String answer;


    public InviteGroup(String[] str, From from, Type type) {
        super(str, from, type);
        this.groupName = str[2];
        this.target = str[3];
    }

    public User getSourceUser() {
        return sourceUser;
    }

    public String getTarget() {
        return target;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setSourceUser(User sourceUser) {
        this.sourceUser = sourceUser;
    }

    public void actorRefToInvite(ActorRef actorRefToInvite) {
        actorRefToInvite = actorRefToInvite;
    }

    public ActorRef getTargetActorRef() {
        return targetActorRef;
    }

    public void setTargetActorRef(ActorRef targetActorRef) {
        this.targetActorRef = targetActorRef;
    }


    @Override
    public String toString() {
        return "InviteGroup{" +
                "sourceUser=" + sourceUser +
                ", targerUser=" + targerUser +
                ", target='" + target + '\'' +
                ", groupName='" + groupName + '\'' +
                ", targetActorRef=" + targetActorRef +
                ", answer='" + answer + '\'' +
                ", userAdmin=" + userAdmin +
                ", groupName='" + groupName + '\'' +
                ", type=" + type +
                ", from=" + from +
                ", isSucceeded=" + isSucceeded +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                '}';
    }
}


import akka.actor.ActorRef;

import java.io.Serializable;


public class InviteGroup extends CreateGroupCommand implements Serializable {
    private User sourceUser;
    private User targerUser;
    private String target;
    private String groupName;
    private ActorRef targetActorRef = null;
    private ActorRef groupActorRef = null;
    private String answer;
    boolean gaveAnswer = false;


    public InviteGroup(String[] str, From from, Type type, String userName) {
        super(str, from, type, userName);
        this.groupName = str[0];
        this.target = str[1];
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
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

    public ActorRef getGroupActorRef() {
        return groupActorRef;
    }

    public void setGroupActorRef(ActorRef groupActorRef) {
        this.groupActorRef = groupActorRef;
    }

    public boolean GaveAnswer() {
        return gaveAnswer;
    }

    public void setGaveAnswer(boolean gaveAnswer) {
        this.gaveAnswer = gaveAnswer;
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

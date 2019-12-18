import akka.actor.ActorRef;

import java.util.Arrays;

public class GroupCommand extends Command {
    private User sourceUser;
    private Group targetGroup;
    protected String groupName;
    private ActorRef targetGroupRef;

    public GroupCommand(Type type, From from) {
        super(type, from);
    }

    public GroupCommand(Type type, From from, ActorRef targetGroupRef, String groupName) {
        super(type, from);
        this.targetGroupRef = targetGroupRef;
        this.groupName = groupName;
    }

    public ActorRef getTargetGroupRef() {
        return targetGroupRef;
    }

    public User getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(User sourceUser) {
        this.sourceUser = sourceUser;
    }

    public Group getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(Group targetGroup) {
        this.targetGroup = targetGroup;
    }


    public String getGroupName() {
        return groupName;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "GroupCommand{" +
                "sourceUser=" + sourceUser +
                ", targerGroup=" + targetGroup +
                ", groupName='" + groupName + '\'' +
                ", type=" + type +
                ", from=" + from +
                ", isSucceeded=" + isSucceeded +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                '}';
    }
}

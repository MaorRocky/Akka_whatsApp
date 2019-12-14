import java.util.Arrays;
import java.util.stream.Collectors;

public class GroupTextMessage extends GroupConnection {

    private User sourceUser;
    private Group targetGroup;
    private String message;
    private String groupName;

    public GroupTextMessage(String groupName, String[] message, Type type, From from) {
        super(type, from);
        this.groupName = groupName;
        String[] messageArr = Arrays.copyOfRange(message, 4, message.length);
        this.message = String.join(" ", messageArr);
    }

    public void setSourceUser(User sourceUser) {
        this.sourceUser = sourceUser;
    }

    public User getSourceUser() {
        return sourceUser;
    }

    public Group getTargetGroup() {
        return targetGroup;
    }


    public String getMessage() {
        return message;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return "GroupTextMessage{" +
                "sourceUser=" + sourceUser +
                ", targerGroup=" + targetGroup +
                ", message=" + message +
                ", groupName='" + groupName + '\'' +
                ", type=" + type +
                ", from=" + from +
                ", isSucceeded=" + isSucceeded +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                '}';
    }
}

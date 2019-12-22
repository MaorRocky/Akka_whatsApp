import java.io.Serializable;
import java.util.Arrays;


public class GroupFileMessage extends GroupCommand implements Serializable {

    private User sourceUser;
    private Group targetGroup;
    private String message;
    private String groupName;
    private FileMessage fileMessage;
    private String targetUserName;
    private String sourceFilePath;
    private User targetUser;


    public GroupFileMessage(String groupName, String sourceFilePath, Type type, From from) {
        super(type, from);
        this.groupName = groupName;
        this.sourceFilePath = sourceFilePath;
        this.fileMessage = new FileMessage(this.sourceFilePath);

    }
/*
    public GroupFileMessage(String groupName, String message, Type type, From from) {
        super(type, from);
        this.groupName = groupName;
        this.message = message;
    }*/


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

    public FileMessage getFileMessage() {
        return fileMessage;
    }

    @Override
    public String toString() {
        return "GroupFileMessage{" +
                "sourceUser=" + sourceUser +
                ", targetGroup=" + targetGroup +
                ", message='" + message + '\'' +
                ", groupName='" + groupName + '\'' +
                ", fileMessage=" + fileMessage +
                ", targetUserName='" + targetUserName + '\'' +
                ", sourceFilePath='" + sourceFilePath + '\'' +
                ", targetUser=" + targetUser +
                ", groupName='" + groupName + '\'' +
                ", type=" + type +
                ", from=" + from +
                ", isSucceeded=" + isSucceeded +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                ", SourceUser=" + SourceUser +
                '}';
    }
}

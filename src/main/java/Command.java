import java.io.Serializable;

public class Command implements Serializable {

    /*Type and From are enum fields*/
    protected Type type;
    protected From from;
    protected boolean isSucceeded;
    protected String resultString;
    protected User userResult;
    protected User SourceUser;


    public Command(Type type, From from) {
        this.type = type;
        this.from = from;
        this.resultString = "";
    }

    public Command(Type type, From from, String str) {
        this.type = type;
        this.from = from;
        this.resultString = str;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public From getFrom() {
        return from;
    }

    public void setFrom(From from) {
        this.from = from;
    }

    public void setResult(boolean failOrSuccessRes, String resultString) {
        if (!failOrSuccessRes) {
            this.type = Type.Error;
        }
        this.isSucceeded = failOrSuccessRes;
        this.resultString = resultString;
    }

    public void setUserResult(boolean failOrSuccessRes, User userRes) {
        if (!failOrSuccessRes)
            this.type = Type.Error;
        this.isSucceeded = failOrSuccessRes;
        this.userResult = userRes;
    }

    public String getResultString() {
        return resultString;
    }

    public User getUserResult() {
        return userResult;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public enum Type implements Serializable {
        Connect, Disconnect, UserTextMessage, UserFileMessage,
        Create_Group, Group_Leave, Group_Text, Group_File,
        Invite_Group, Group_Remove, Group_Promote, Group_Demote, USER_DELETE_THIS_GROUP, Print,User_unMute,User_Mute,
        invitationAnswer, Invitation, Error, WelcomeMessage, Delete_Group
    }

    public enum From implements Serializable {
        Client, IO, Server, Group, UserConnection, GroupsConnection
    }

    public void setSourceUser(User sourceUser) {
        SourceUser = sourceUser;
    }

    @Override
    public String toString() {
        return "Command{" +
                "type=" + type +
                ", from=" + from +
                ", isSucceeded=" + isSucceeded +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                '}';
    }
}

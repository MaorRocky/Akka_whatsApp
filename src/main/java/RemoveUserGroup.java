public class RemoveUserGroup extends CreateGroupCommand {

    private String userToRemove;


    public RemoveUserGroup(String[] data, Type type, From from) {
        super(type, from);
        this.groupName = data[0];
        this.userToRemove = data[1];
    }


    public String getUserToRemove() {
        return userToRemove;
    }


    @Override
    public String toString() {
        return "RemoveUserGroup{" +
                "userToRemove='" + userToRemove + '\'' +
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

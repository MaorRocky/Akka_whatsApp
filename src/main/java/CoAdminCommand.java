public class CoAdminCommand extends CreateGroupCommand {
    private String targetUser;

    public CoAdminCommand(String[] data, Type type, From from) {
        super(type, from);
        this.groupName = data[0];
        this.targetUser = data[1];
    }

    public String getTargetUser() {
        return targetUser;
    }

    @Override
    public String toString() {
        return "CoAdminCommand{" +
                "targetUser='" + targetUser + '\'' +
                ", userAdmin=" + userAdmin +
                ", groupName='" + groupName + '\'' +
                ", groupName='" + groupName + '\'' +
                ", type=" + type +
                ", from=" + from +
                ", isSucceeded=" + isSucceeded +
                ", resultString='" + resultString + '\'' +
                ", userResult=" + userResult +
                '}';
    }
}

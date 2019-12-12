
import java.io.Serializable;


public class InviteGroup extends CreateGroupCommand implements Serializable {
    private User userAdmin;
    private String groupName;
    private String userToInvite;


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

}

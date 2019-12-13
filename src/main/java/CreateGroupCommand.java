
import java.io.Serializable;


public class CreateGroupCommand extends Command implements Serializable {
    protected User userAdmin;
    protected String groupName;


    public CreateGroupCommand(String[] str, From from, Type type) {
        super(type, from);
        userAdmin = new User(str[0]);
        if (!type.equals(Type.Disconnect))
            this.groupName  = str[1];
    }

    public User getSourceUser() {
        return userAdmin;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setSourceUser(User sourceUser) {
        this.userAdmin = sourceUser;
    }

}

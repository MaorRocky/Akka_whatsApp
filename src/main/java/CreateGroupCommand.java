
import java.io.Serializable;


public class CreateGroupCommand extends GroupConnection implements Serializable {
    protected User userAdmin;
    protected String groupName;


    public CreateGroupCommand(String[] str, From from, Type type,String userName) {
        super(type, from);
        userAdmin = new User(userName);
        if (!type.equals(Type.Disconnect))
            this.groupName  = str[0];
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

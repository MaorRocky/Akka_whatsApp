
import java.io.Serializable;
import java.util.Arrays;

public class TextMessage extends Command implements Serializable {

    private String message;
    private User sourceUser;
    private User targetUser;

    public TextMessage(String[] str, From from, String sourceUser){
        super(Type.User_Text, from);
        this.targetUser = new User(str[0]);
        String [] msgArr = Arrays.copyOfRange(str, 1, str.length);
        this.message = String.join(" ", msgArr);
        this.sourceUser = new User(sourceUser);
    }

    public TextMessage(String[] str, From from, String sourceUser, Type type){
        super(type, from);
        this.targetUser = new User(str[0]);
        String [] msgArr = Arrays.copyOfRange(str, 1, str.length);
        this.message = String.join(" ", msgArr);
        this.sourceUser = new User(sourceUser);
    }

    public TextMessage(User sourceUser, User targetUser, String message){
        super(Type.Group_Remove, From.Client);
        this.targetUser = targetUser;
        this.sourceUser = sourceUser;
        this.message = message;
    }

    public User getTargetUser(){
        return this.targetUser;
    }

    public String getMessage(){
        return this.message;
    }

    public User getSourceUser(){
        return this.sourceUser;
    }

}
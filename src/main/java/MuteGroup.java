import akka.actor.ActorRef;
import java.io.Serializable;
import java.time.Duration;

public class MuteGroup extends Command implements Serializable{
    private String targetUserName;
    private String sourceUserName;
    private Duration duration;
    private ActorRef targetActorRef = null;
    private User sourceUser;
    private String groupName;



    public MuteGroup(String[]str,From from, Type type,String userName,Boolean isMute){
        super(type,from);
        this.groupName = str[0];
        this.targetUserName = str[1];
        this.sourceUserName = userName;
        if(isMute)
            this.duration = Duration.ofSeconds(Integer.parseInt(str[2]));
        else
            this.duration = null;
    }

    public Duration getDuration(){
        return this.duration;
    }
    public String getTarget() {
        return targetUserName;
    }
    public String getSourceUserName() {
        return sourceUserName;
    }
    public void setTargetActorRef(ActorRef targetActorRef) {
        this.targetActorRef = targetActorRef;
    }
    public ActorRef getTargetActorRef() {
        return targetActorRef;
    }
    public void setSourceUser(User sourceUser) {
        this.sourceUser = sourceUser;
    }
    public User getSourceUser() {
        return sourceUser;
    }
    public String getGroupName(){return groupName;}

}

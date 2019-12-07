import java.io.Serializable;

public class Invitation implements Serializable {
    private User sourceUser;
    private User targetUser;
    private String groupName;
    private String invitation;
    private String resultAnswer;
    private boolean answer;
    private Command.Type type;

   /* public Invitation(GroupCommand cmd){
        this.sourceUser = cmd.getSource();
        this.targetUser = cmd.getUserResult();
        this.groupName = cmd.getGroupName();
        this.invitation = cmd.getResult();
        this.type = Command.Type.Invitation;
        this.answer = false;
    }*/

    public Invitation(String targetName, String resultAnswer){
        this.targetUser = new User(targetName);
        this.type = Command.Type.Invitation;
        this.resultAnswer = resultAnswer;
        this.answer = true;
    }

    public User getSourceUser() {
        return sourceUser;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getInvitation() {
        return invitation;
    }

    public boolean answer() {
        return answer;
    }

    public String getResultAnswer() {
        return resultAnswer;
    }

    public void setResultAnswer(String resultAnswer) {
        this.resultAnswer = resultAnswer;
        this.answer = true;
        this.type = Command.Type.invitationAnswer;
    }

    public Command.Type getType() {
        return type;
    }
}

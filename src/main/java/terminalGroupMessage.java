import java.util.Arrays;

public class terminalGroupMessage {
    public String groupMessageCommand;
    public String[] messageData;
    public String typeOfMessage;

    public terminalGroupMessage(String[] msg, String username) {
        this.groupMessageCommand = msg[1];
        /*small hack, we are copying the message command again, and putting in its place the username.*/
        this.messageData = Arrays.copyOfRange(msg, 1, msg.length);
        messageData[0] = username;
        typeOfMessage = messageData[1];

    }

    @Override
    public String toString() {
        return "terminalGroupMessage{" +
                "groupMessageCommand='" + groupMessageCommand + '\'' +
                ", messageData=" + Arrays.toString(messageData) +
                ", typeOfMessage='" + typeOfMessage + '\'' +
                '}';
    }


    //    private boolean isValid(String[] msg) {
//        return msg.length > 2;
//    }
}

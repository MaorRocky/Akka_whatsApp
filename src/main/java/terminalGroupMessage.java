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

//    private boolean isValid(String[] msg) {
//        return msg.length > 2;
//    }
}

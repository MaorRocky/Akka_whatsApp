import java.util.Arrays;
/*this is how we represent a message starting with a '/user' prefix.*/

public class terminalUserMessage {
    public String groupMessageCommand;
    public String[] messageData;

    public terminalUserMessage(String[] msg) {
        if (isValid(msg)) {
            this.groupMessageCommand = msg[1];
            this.messageData = Arrays.copyOfRange(msg, 2, msg.length);
        } else {
            groupMessageCommand = "error";
            messageData = null;
        }
    }

    private boolean isValid(String[] msg) {
        return msg.length > 2;
    }

}

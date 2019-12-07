import java.util.Arrays;

public class terminalGroupMessage {
    public String groupMessageCommand;
    public String[] messageData;

    public terminalGroupMessage(String[] msg) {
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

import java.util.Arrays;

public class terminalSendGroupMessage extends terminalGroupMessage {
    public String groupMessageCommand;
    public String textOrFile;
    public String[] messageData;
    public String typeOfMessage;

    public terminalSendGroupMessage(String[] msg, String username, String groupMessageCommand, String textOrFile, String[] messageData, String typeOfMessage) {
        super(msg, username);
        this.groupMessageCommand = groupMessageCommand;
        this.textOrFile = textOrFile;
        this.messageData = messageData;
        this.typeOfMessage = typeOfMessage;
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

import akka.japi.pf.FI;

public class Predicates {

    public FI.TypedPredicate<ConnectCommand> connectCommandPred;
    public FI.TypedPredicate<DisConnectCommand> disconnectCmd;
    public FI.TypedPredicate<TextMessage> sendTextToAnotherClient;
    public FI.TypedPredicate<TextMessage> receiveTextFromAnotherClient;
    public FI.TypedPredicate<FileMessage> sendFileToAnotherClient;
    public FI.TypedPredicate<FileMessage> receiveFileClient;
    public FI.TypedPredicate<CreateGroupCommand> createGroup;

    //Server predicate
    public FI.TypedPredicate<CreateGroupCommand> createGroupServer;


    public Predicates() {
        //Client predicates
        connectCommandPred = cmd -> cmd.getType().equals(Command.Type.Connect);
        disconnectCmd = cmd -> cmd.getType().equals(Command.Type.Disconnect);
        sendTextToAnotherClient = cmd -> cmd.getFrom().equals(Command.From.IO)
                && (cmd.getType().equals(Command.Type.UserTextMessage));
        receiveTextFromAnotherClient = cmd -> cmd.getFrom().equals(Command.From.Client);
        sendFileToAnotherClient = cmd -> cmd.getType().equals(Command.Type.UserFileMessage)
                && cmd.getFrom().equals(Command.From.IO);
        receiveFileClient = cmd -> cmd.getType().equals(Command.Type.UserFileMessage);
        createGroup = cmd -> cmd.getType().equals(Command.Type.Create_Group)
                && cmd.getFrom().equals(Command.From.IO);
        //Server predicate
        createGroupServer = cmd -> cmd.getType().equals(Command.Type.Create_Group);

    }
}
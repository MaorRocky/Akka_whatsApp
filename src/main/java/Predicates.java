import akka.japi.pf.FI;

public class Predicates {

    public FI.TypedPredicate<ConnectCommand> connectCommandPred;
    public FI.TypedPredicate<DisConnectCommand> disconnectCmd;
    public FI.TypedPredicate<TextMessage> sendTextToAnotherClient;
    public FI.TypedPredicate<TextMessage> receiveTextFromAnotherClient;
    public FI.TypedPredicate<FileMessage> sendFileToAnotherClient;
    public FI.TypedPredicate<FileMessage> receiveFileClient;

    //Server predicate


    public Predicates() {
        //Client predicates
        connectCommandPred = cmd -> cmd.getType().equals(Command.Type.Connect);
        disconnectCmd = cmd -> cmd.getType().equals(Command.Type.Disconnect);
        sendTextToAnotherClient = cmd -> cmd.getFrom().equals(Command.From.IO)
                && (cmd.getType().equals(Command.Type.User_Text));
        receiveTextFromAnotherClient = cmd -> cmd.getFrom().equals(Command.From.Client);
        sendFileToAnotherClient = cmd -> cmd.getType().equals(Command.Type.User_File)
                && cmd.getFrom().equals(Command.From.IO);
        receiveFileClient = cmd -> cmd.getType().equals(Command.Type.User_File);
        //Server predicate

    }
}
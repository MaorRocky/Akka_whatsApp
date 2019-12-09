import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.FI;

public class ParserActor extends AbstractActor {

    private final ActorRef UserActor; // each parserActor will be assigned a UserActor
    private String userName; // the UserName of the UserActor

    static public Props props(ActorRef acRef) {
        return Props.create(ParserActor.class, () -> new ParserActor(acRef));
    }

    public ParserActor(ActorRef acRef) {
        this.UserActor = acRef;
    }

    /*this is how we print to the terminal. the parser is the only one which prints,
     * he is the one which handles output*/
    private void print(String string) {
        System.out.println(string);
    }

    private void setUserName(String connectStr) {
        this.userName = connectStr.split(" ")[0];
    }

    /*TODO change it at the end to a better solution*/
    private boolean isValid(String[] msg) {
        return msg.length > 2;
    }

    private Command getErrorCmd() {
        Command cmd = new Command(Command.Type.Error, Command.From.IO);
        cmd.setResult(false, "Invalid command");
        return cmd;
    }

    /*TODO we need to add:*/
    /*groups
     * and invitations*/
    private void setCommand(String[] msg) {
        switch (msg[0]) {
            case "/user":
                sendToUserActor(userSwitch(new terminalUserMessage(msg)));
                break;
            case "/group":
                sendToUserActor(groupSwitch(new terminalGroupMessage(msg)));
                break;
            default:
                sendToUserActor(new Command(Command.Type.Error, Command.From.IO));
                break;
        }
    }

    /*if the message begins with /user this method will parse a relevant command.*/
    private Command userSwitch(terminalUserMessage userMessage) {
        Command command;
        switch (userMessage.groupMessageCommand) {
            case "connect":
                command = new ConnectCommand(userMessage.messageData, Command.From.IO);
                break;
            case "disconnect":
                command = new DisConnectCommand(new String[]{this.userName}, Command.From.IO);
                break;
            case "text":
                command = new TextMessage(userMessage.messageData, Command.From.IO, this.userName);
                break;
            /*TODO this need some testing*/
            case "file":
                command = new FileMessage(userMessage.messageData, Command.From.IO, this.userName);
                break;
            default:
                command = new Command(Command.Type.Error, Command.From.IO);
                command.setResult(false, "Invalid command");
                break;
        }
        return command;
    }

    private Command groupSwitch(terminalGroupMessage msg) {
        Command cmd;
        switch (msg.groupMessageCommand) {
            case "create":
                cmd = new CreateGroupCommand(userName, msg.messageData, Command.From.IO, Command.Type.Create_Group);
                break;
            /*case "leave":
                cmd = new CommunicationCommand(new String[]{userName, msg[2]}, Command.From.IO, Command.Type.Group_Leave);

                break;
            case "send":
                if (isValid(Arrays.copyOfRange(msg, 2, msg.length))) {
                    if (msg[2].equals("text"))
                        cmd = new TextCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_Text);
                    else if (msg[2].equals(("file")))
                        cmd = new FileCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_File);
                } else
                    cmd = getErrorCmd();
                break;
            case "user":
                if (isValid(Arrays.copyOfRange(msg, 2, msg.length))) {
                    if (msg[2].equals("invite"))
                        cmd = new GroupCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_Invite);
                    else if (msg[2].equals(("remove")))
                        cmd = new GroupCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_Remove);
                    else if (msg[2].equals(("mute")))
                        cmd = new GroupCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_Mute);
                    else if (msg[2].equals(("unmute")))
                        cmd = new GroupCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_UnMute);
                } else
                    cmd = getErrorCmd();
                break;
            case "coadmin":
                if (isValid(Arrays.copyOfRange(msg, 2, msg.length))) {
                    if (msg[2].equals("add"))
                        cmd = new GroupCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_Promote);
                    else if (msg[2].equals(("remove")))
                        cmd = new GroupCommand(Arrays.copyOfRange(msg, 3, msg.length), Command.From.IO, this.userName, Command.Type.Group_Demote);
                } else
                    cmd = getErrorCmd();
                break;*/
            default:
                cmd = new Command(Command.Type.Error, Command.From.IO);
                cmd.setResult(false, "Invalid command");
                break;
        }
        return cmd;
    }

    //send the relevant command to the client to handle, relevant client = toSend client
    private void sendToUserActor(Command command) {
        if (command.getType().equals(Command.Type.Error)) {
            print("Invalid command");
        } else {
            UserActor.tell(command, self());
        }
    }

    public Receive createReceive() {
        FI.TypedPredicate<Command> connectCommandPred = command -> command.getType().equals(Command.Type.Connect);

        return receiveBuilder()
                .match(String.class, (msg) -> setCommand(msg.split(" ")))
                .match(Command.class, connectCommandPred, (command) -> {
                    setUserName(command.getResultString());
                    print(command.getResultString());
                })
                .match(Command.class, (command) -> print(command.getResultString()))
                .matchAny((command) -> print("Invalid Command"))
                .build();
    }
}
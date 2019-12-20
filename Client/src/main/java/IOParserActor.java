import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.FI;

import java.util.Arrays;

public class IOParserActor extends AbstractActor {

    private final ActorRef UserActor; // each parserActor will be assigned a UserActor
    private String userName; // the UserName of the UserActor

    static public Props props(ActorRef userActorRef) {
        return Props.create(IOParserActor.class, () -> new IOParserActor(userActorRef));
    }

    public IOParserActor(ActorRef userActorRef) {
        this.UserActor = userActorRef;
    }

    /*this is how we print to the terminal. the parser is the only one which prints,
     * he is the one which handles output*/
    private void print(String string) {
        System.out.println(string);
    }

    private void setUserName(String connectStr) {
        this.userName = connectStr.split(" ")[0];
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
                sendToUserActor(groupSwitch(msg));
                break;
            case "Yes":
                sendToUserActor(new Command(Command.Type.invitationAnswer, Command.From.IO, "Yes"));
                break;
            case "No":
                sendToUserActor(new Command(Command.Type.invitationAnswer, Command.From.IO, "No"));
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

    private Command groupSwitch(String[] msg) {
        Command cmd = null;
        switch (msg[1]) {
            case "create":
                cmd = new CreateGroupCommand(Arrays.copyOfRange(msg, 2, msg.length),
                        Command.From.IO, Command.Type.Create_Group, userName);
                break;
            case "user":
                if ("invite".equals(msg[2])) {
                    cmd = new InviteGroup(Arrays.copyOfRange(msg, 3, msg.length)
                            , Command.From.IO, Command.Type.Invite_Group, userName);
                } else if ("remove".equals(msg[2])) {
                    cmd = new RemoveUserGroup(Arrays.copyOfRange(msg, 3, msg.length),
                            Command.Type.Group_Remove, Command.From.IO);
                }
                break;
            case "send":
                if ("text".equals(msg[2])) {
                    cmd = new GroupTextMessage(msg[3], msg, Command.Type.Group_Text, Command.From.IO);
                }
                break;
            case "coadmin":
                if ("add".equals(msg[2])) {
                    cmd = new CoAdminCommand(Arrays.copyOfRange(msg, 3, msg.length),
                            Command.Type.Group_Promote, Command.From.IO);
                }
                break;
            default:
                print("im in default \n\n");
                cmd = new Command(Command.Type.Error, Command.From.IO);
                cmd.setResult(false, "Invalid command");
                break;
        }
        return cmd;
    }


    //send the relevant command to the client to handle, relevant client = toSend client
    private void sendToUserActor(Command command) {
        if (command.getType().equals(Command.Type.Error)) {
            print("ERROR:");
            print("in IOParserActor:" + command.toString());
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
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.FI;

import java.util.Arrays;

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
        if ("/user".equals(msg[0])) {
            sendToUserActor(userSwitch(msg));
        } else {
            sendToUserActor(new Command(Command.Type.Error, Command.From.IO));
        }
    }

    /*if the message begins with /user this method will parse a relevant command.*/
    private Command userSwitch(String[] msg) {
        Command command = null;
        switch (msg[1]) {
            case "connect":
                if (isValid(msg))
                    command = new ConnectCommand(Arrays.copyOfRange(msg, 2, msg.length), Command.From.IO);
                break;
            case "disconnect":
                command = new DisConnectCommand(new String[]{this.userName}, Command.From.IO);
                break;
            case "text":
                if (isValid(msg)) {
                    command = new TextMessage(Arrays.copyOfRange(msg, 2, msg.length), Command.From.IO, this.userName);
                } else
                    command = getErrorCmd();
                break;
            case "file":
                if (isValid(msg))
                    command = new FileMessage(Arrays.copyOfRange(msg, 2, msg.length), Command.From.IO, this.userName);
                else
                    command = getErrorCmd();
                break;
            default:
                command = new Command(Command.Type.Error, Command.From.IO);
                command.setResult(false, "Invalid command");
                break;
        }
        return command;
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
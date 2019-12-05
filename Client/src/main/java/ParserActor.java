import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.FI;

import java.util.Arrays;

public class ParserActor extends AbstractActor {

    private final ActorRef UserActor;
    private String userName;

    static public Props props(ActorRef acRef) {
        return Props.create(ParserActor.class, () -> new ParserActor(acRef));
    }

    public ParserActor(ActorRef acRef) {
        this.UserActor = acRef;
    }

    private void print(String string) {
        System.out.println(string);
    }

    private void setUserName(String connectStr) {
        this.userName = connectStr.split(" ")[0];
    }

    private boolean isValid(String[] msg) {
        return msg.length > 2;
    }

    private Command getErrorCmd() {
        Command cmd = new Command(Command.Type.Error, Command.From.IO);
        cmd.setResult(false, "Invalid command");
        return cmd;
    }

    private void setCommand(String[] msg) {
        if ("/user".equals(msg[0])) {
            sendToUserActor(userSwitch(msg));
        } else {
            sendToUserActor(new Command(Command.Type.Error, Command.From.IO));
        }
    }

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

    //send the relevant command to the client to handle
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
                    setUserName(command.getResult());
                    print(command.getResult());
                })
                .match(Command.class, (command) -> print(command.getResult()))
                .matchAny((command) -> print("Invalid Command"))
                .build();
    }
}
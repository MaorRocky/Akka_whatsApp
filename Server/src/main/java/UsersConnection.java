import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;

public class UsersConnection extends AbstractActor {

    protected HashMap<String, User> UsersMap;
    private Predicates predicates;

    static public Props props() {
        return Props.create(UsersConnection.class, UsersConnection::new);
    }

    public UsersConnection() {
        this.UsersMap = new HashMap<>();
        predicates = new Predicates();
        getContext().parent().tell("userConnection created", getSelf());
    }

    private void printFromServer(String message) {
        getContext().parent().tell(message, getSelf());
    }

    private boolean addToMap(String name, User user) {
        if (UsersMap.containsKey(name)) return false;
        else {
            UsersMap.put(name, user);
            return true;
        }
    }

    protected void addUser(ConnectCommand cmd, ActorRef userRef) {
        printFromServer("sender path is : " + userRef.path().toString());
        String UserName = cmd.getUser().getUserName();
        if (addToMap(UserName, cmd.getUser())) {
            cmd.setResult(true, UserName + " has connected successfully!");
            printFromServer("USERCONNECTION: i have added " + UserName + " to the server");
        } else {
            cmd.setResult(false, UserName + " is in use!");
        }
        sendBack(cmd, userRef);
    }

    protected void removeUser(DisConnectCommand cmd, ActorRef userRef) {
        String UserName = cmd.getUser().getUserName();
        if (this.UsersMap.remove(UserName) != null) {
            cmd.setResult(true, UserName + " has been disconnected successfully!");
            printFromServer("USERCONNECTION: i have removed " + UserName + " from the server");

        } else {
            cmd.setResult(false, UserName + " does not exist!");
        }
        sendBack(cmd, userRef);
    }

    private void sendBack(Command command, ActorRef sender) {
        command.setFrom(Command.From.UserConnection);
        sender.tell(command, getSelf());
    }


    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(ConnectCommand.class, predicates.ConnectCommandUserConnection, (cmd) -> addUser(cmd, sender()))
                .match(DisConnectCommand.class, predicates.DisConnectCommandUsersConnection
                        , (cmd) -> {
                            printFromServer(cmd.toString());
                            removeUser(cmd, sender());
                        })
                .matchAny((cmd) -> printFromServer(cmd.toString()))
                .build();
    }
}

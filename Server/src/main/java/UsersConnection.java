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
        String UserName = cmd.getUser().getUserName();
        if (addToMap(UserName, cmd.getUser())) {
            cmd.setResult(true, UserName + " has connected successfully!");
        } else {
            cmd.setResult(false, UserName + " is in use!");
        }
        sendBack(cmd, userRef);
    }

    protected void removeUser(DisConnectCommand cmd, ActorRef userRef) {
        String UserName = cmd.getUser().getUserName();
        if (this.UsersMap.remove(UserName) != null) {
            cmd.setResult(true, UserName + " has been disconnected successfully!");
        } else {
            cmd.setResult(false, UserName + " does not exist!");
        }
        sendBack(cmd, userRef);
    }

    private void sendBack(Command command, ActorRef sender) {
        command.setFrom(Command.From.UserConnection);
        sender.tell(command, getSelf());
    }

    private Command getTargetUser(Command cmd, User target) {
        User targetUser = UsersMap.get(target.getUserName());
        if (targetUser != null) {
            cmd.setUserResult(true, targetUser);
        } else {
            cmd.setResult(false, target.getUserName() + " does not exist!");
        }
        return cmd;
    }

    private void getTargetActorRef(InviteGroup inviteGroup) {
        inviteGroup.setTargetUser(UsersMap.getOrDefault(inviteGroup.getTarget(), null));
        getSender().tell(inviteGroup, self());
    }

    private void userMessage(TextMessage textMessage, ActorRef sender) {
        sendBack(getTargetUser(textMessage, textMessage.getTargetUser()), sender);
    }

    private void fileMessage(FileMessage fileMessage, ActorRef sender) {
        sendBack(getTargetUser(fileMessage, fileMessage.getTargetUser()), sender);
    }


    @Override
    public String toString() {
        return "UsersConnection{" +
                "UsersMap=" + UsersMap + '}';
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(ConnectCommand.class, predicates.ConnectCommandUserConnection, (cmd) -> addUser(cmd, sender()))
                .match(DisConnectCommand.class, predicates.DisConnectCommandUsersConnection, (cmd) -> removeUser(cmd, sender()))
                .match(TextMessage.class, predicates.TextMessageUsersConnection, (msg) -> userMessage(msg, sender()))
                .match(FileMessage.class, predicates.sendFileToAnotherClient_usersConnection, (msg) -> fileMessage(msg, sender()))
                .match(InviteGroup.class, this::getTargetActorRef)
                .matchAny((cmd) -> printFromServer(cmd.toString()))
                .build();
    }
}

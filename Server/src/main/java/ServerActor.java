import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Scheduler;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.HashMap;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ServerActor extends AbstractActor {

    private Predicates predicates;
    private final ActorRef usersManager = getContext().actorOf(Props.create(UsersConnection.class), "UserConnection");
    private final ActorRef groupsManager = getContext().actorOf(Props.create(GroupsConnection.class), "GroupsConnection");
    private Timeout askTimeout;


    public ServerActor() {

        askTimeout = new Timeout(Duration.create(1, SECONDS));
        this.predicates = new Predicates();
        System.out.println("SERVER IS UP!\nWaiting for clients\n");

    }

    /*sets the command sender to "Server"
    and sends back the wanted command to se`nder*/
    private void sendBack(Command command, ActorRef sender) {
        command.setFrom(Command.From.Server);
        sender.tell(command, getSelf());
    }

    /*************************************CONNECT*******************************************/
    /*if the username is not yet in use we will add him, else send and error result*/
    private void connectUser(ConnectCommand cmd, ActorRef sender) {
        cmd.setFrom(Command.From.Server);
        this.usersManager.tell(cmd, sender);
    }

    /*************************************DISCONNECT*******************************************/
    /*if the user exist will remove him,else send an error result*/
    private void disconnectUser(DisConnectCommand cmd, ActorRef sender) {
        cmd.setFrom(Command.From.Server);
        disconnectUserFromGroups(cmd);
        this.usersManager.tell(cmd, sender);
    }

    private void disconnectUserFromGroups(DisConnectCommand disConnectCommand) {
        print("disconnectCommand user groups :\n" +
                disConnectCommand.getUser().getUsersGroups().toString());
        HashMap<String, ActorRef> groupsRef = disConnectCommand.getUser().getUsersGroups();
        groupsRef.forEach((key, val) -> val.tell(disConnectCommand, self()));


    }


    private void sendToGroupManager(GroupCommand groupCommand, ActorRef sender) {
        groupCommand.setFrom(Command.From.Server);
        groupsManager.tell(groupCommand, sender);

    }

    private void sendInviteGroupManager(InviteGroup inviteGroup, ActorRef sender) {
        inviteGroup.setFrom(Command.From.Server);
        InviteGroup newInv = getUser(inviteGroup);
        User targetUser = newInv.getTargetUser();
        if (targetUser != null) {
            newInv.setTargetActorRef(targetUser.getUserActorRef());
            newInv.setUserResult(true, targetUser);
            groupsManager.tell(newInv, sender);
        } else {
            newInv.setResult(false, newInv.getTarget() + " does not exist!");
            sender.tell(newInv, self());
        }

    }

    private InviteGroup getUser(InviteGroup inviteGroup) {
        Future<Object> future = Patterns.ask(usersManager, inviteGroup, askTimeout);
        try {
            return (InviteGroup) Await.result(future, askTimeout.duration());
        } catch (Exception e) {
            return null;
        }
    }

//    private ActorRef getUserActor()/

    //send TextMessage command with the wanted
    //target user back to the sender if exist
    //else, sends false command with relevant result message
    private void userMessage(TextMessage textMessage, ActorRef sender) {
        textMessage.setFrom(Command.From.Server);
        this.usersManager.tell(textMessage, sender);
    }

    //sends fileMessage command with the wanted
    //target user back to the sender if exist
    //else, sends false command with relevant result message
    /*TODO add file support*/
    private void userFile(FileMessage fileMessage, ActorRef sender) {
        fileMessage.setFrom(Command.From.Server);
        this.usersManager.tell(fileMessage, sender);
    }

    public void print(String string) {
        System.out.println(string);
    }

    public Receive createReceive() {

        return receiveBuilder() // need to decide how to create an actor for each group
                .match(ConnectCommand.class, predicates.connectCommandPred, (cmd) -> connectUser(cmd, sender()))
                .match(DisConnectCommand.class, predicates.disconnectCmd, (cmd) -> disconnectUser(cmd, sender()))
                .match(TextMessage.class, (cmd) -> userMessage(cmd, sender()))
                .match(FileMessage.class, (cmd) -> userFile(cmd, sender()))
                .match(CreateGroupCommand.class, predicates.createGroupServer, (cmd) -> sendToGroupManager(cmd, sender()))
                .match(GroupCommand.class, predicates.GroupLeave, (cmd) -> sendToGroupManager(cmd, sender()))
                .match(InviteGroup.class, predicates.InviteGroupServer, (cmd) -> sendInviteGroupManager(cmd, sender()))
                .match(RemoveUserGroup.class, predicates.removeUserFromGroup, (cmd) -> sendToGroupManager(cmd, sender()))
                .match(CoAdminCommand.class, predicates.PromoteCommand, (cmd) -> sendToGroupManager(cmd, sender()))
                .match(GroupTextMessage.class, (msg) -> sendToGroupManager(msg, sender()))
                .match(GroupFileMessage.class, (msg) -> sendToGroupManager(msg, sender()))
                .match(String.class, System.out::println)
                .matchAny((cmd) -> System.out.println("problem" + cmd + "problem"))
                .build();
    }
}
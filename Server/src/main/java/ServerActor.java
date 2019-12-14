import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Scheduler;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.sound.midi.SoundbankResource;
import java.util.HashSet;

import static java.util.concurrent.TimeUnit.SECONDS;


public class ServerActor extends AbstractActor {

    private Scheduler scheduler;
    private Predicates predicates;
    private final ActorRef usersManager = getContext().actorOf(Props.create(UsersConnection.class), "UserConnection");
    private final ActorRef groupsManager = getContext().actorOf(Props.create(GroupsConnection.class), "GroupsConnection");
    private Timeout askTimeout;


    public ServerActor() {

        askTimeout = new Timeout(Duration.create(1, SECONDS));
        this.scheduler = context().system().scheduler();
        this.predicates = new Predicates();
        System.out.println("SERVER IS UP!\nWaiting for clients\n");

    }

    /*sets the command sender to "Server"
    and sends back the wanted command to sender*/
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
        HashSet<ActorRef> groupsRef = disConnectCommand.getUser().getUsersGroups();
        for (ActorRef actorRef : groupsRef) {
            actorRef.tell(disConnectCommand, self());
        }
    }


    private void sendToGroupManager(GroupConnection groupConnection, ActorRef sender) {
        print(groupConnection.toString());
        groupConnection.setFrom(Command.From.Server);
        groupsManager.tell(groupConnection, sender);

    }

    private void sendInviteGroupManager(InviteGroup inviteGroup, ActorRef sender) {
        inviteGroup.setFrom(Command.From.Server);
        User targetUser = getUser(inviteGroup.getTarget());
        if (targetUser != null) {
            inviteGroup.setTargetActorRef(targetUser.getUserActorRef());
            inviteGroup.setUserResult(true, targetUser);
            groupsManager.tell(inviteGroup, sender);
        } else {
            inviteGroup.setResult(false, inviteGroup.getTarget() + " does not exist!");
            sender.tell(inviteGroup, self());
        }

    }

    private User getUser(String UserName) {
        Future<Object> future = Patterns.ask(usersManager, UserName, askTimeout);
        try {
            return (User) Await.result(future, askTimeout.duration());
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

    /*//send fileMessage command with the wanted
    //target user back to the sender if exist
    //else, sends false command with relevant result message
    private void userFile(FileCommand cmd, ActorRef sender){
        User target = cmd.getTarget();
        sendBack(getTargetUser(cmd, target), sender);
    }

    //set new Router includes the admin of the new group
    private Router setGroupRouter(User admin){
        List<Routee> routees = new ArrayList<Routee>();
        routees.add(new ActorRefRoutee(admin.getUserRef()));
        Router newrouter = new Router(new BroadcastRoutingLogic(), routees);
        return newrouter;
    }

    //create new group according to user request
    //includes new Router with the group admin
    //if there is no other group with the same name
    private boolean setGroup(CommunicationCommand cmd){
        if (this.groupsMap.get(cmd.getGroupName()) == null){
            Command acRefCmd = getTargetUser(cmd, cmd.getUser());
            User admin;
            if (acRefCmd.isSucceed())
                admin =  acRefCmd.getUserResult();
            else
                return false;
            Group newGroup = new Group(cmd, admin, setGroupRouter(admin));
            this.groupsMap.put(cmd.getGroupName(), newGroup);

            return true;
        }
        return false;
    }*/
/*
    //handle the user request to create new group
    //and sends back the server result to the user
    private void creatGroup(CommunicationCommand cmd, ActorRef sender){

        if (setGroup(cmd))
            cmd.setResult(true, cmd.getGroupName() + " created successfully!");
        else
            cmd.setResult(false, cmd.getGroupName() + " already exists!");

        sendBack(cmd, sender);
    }

    //remove target user from group if the source user have the
    //needed privileges and the target user exist in the group
    //else, return false command with the relevant error
    //then sends back the result to the source user
    private void leaveGroup(CommunicationCommand cmd, ActorRef sender){

        Group group = this.groupsMap.get(cmd.getGroupName());
        if (group != null) {
            CommunicationCommand result = group.leaveGroup(cmd);
            //close the group if the admin removed
            if (result.isSucceed() && result.getResult().equals("Admin")) {
                this.groupsMap.remove(cmd.getGroupName());
                result.setResult(true, cmd.getGroupName()
                        + " admin has closed " + cmd.getGroupName() + "!");
            }
            //broadcast the relevant message to the group members
            group.getRouter().route(result, self());
            sendBack(result, sender);
        }
        else {
            cmd.setResult(false, "Group " + cmd.getGroupName() + " does not exists!");
            sendBack(cmd, sender);
        }
    }

    //return succeed command if:
    //group exist, source user exist and source user is admin or co-admin
    //else, return false command with the relevant result message
    private GroupCommand validateCmd(GroupCommand cmd, Group group){
        if (group != null){
            Group.PType sourceType = group.getUserPType(cmd.getSource().getUserName());
            if (sourceType.equals(Group.PType.Admin) || sourceType.equals(Group.PType.Co_Admin)){
                GroupCommand withRefCmd = (GroupCommand) getTargetUser(cmd, cmd.getTarget());
                return withRefCmd;
            } else
                cmd.setResult(false, "You are neither an admin nor a co-admin of " + cmd.getGroupName() + "!");
        } else
            cmd.setResult(false, cmd.getGroupName() + " does not exist!");
        return cmd;
    }

    //validate the invitation GroupCommand
    //if the command has passed the validation,
    //then, if the target exits and not in the group
    //the server adds the target user to the group
    //and send back the invitation message or false message
    //back to the sender.
    private void inviteToGroup(GroupCommand cmd, ActorRef sender){
        Group group = this.groupsMap.get(cmd.getGroupName());
        GroupCommand validCmd = validateCmd(cmd, group);
        if (validCmd.isSucceed()){
            Group.PType targetType = group.getUserPType(cmd.getTarget().getUserName());
            if (targetType.equals(Group.PType.Not_User)){
                validCmd.setResult(true, "You have been invited to " + cmd.getGroupName() + ", Accept?");
            } else
                validCmd.setResult(false, cmd.getTarget().getUserName() +
                        " is already in " + cmd.getGroupName() + "!");
        }
        sendBack(validCmd, sender);
    }

    //validate the removeUser GroupCommand
    //if the command has passed the validation,
    //then, if the target exits in the group
    //the server removes the target user from the group
    //and send back the relevant message back to the sender.
    private void removeFromGroup(GroupCommand cmd, ActorRef sender){
        Group group = this.groupsMap.get(cmd.getGroupName());
        GroupCommand validCmd = validateCmd(cmd, group);
        if (validCmd.isSucceed()){
            Group.PType targetType = group.getUserPType(cmd.getTarget().getUserName());
            if (targetType.equals(Group.PType.Admin))
                validCmd.setResult(false, "You can not remove the admin of the group!");
            else if (targetType.equals(Group.PType.Not_User)){
                validCmd.setResult(false, validCmd.getTarget().getUserName() + " is not in " +
                        validCmd.getGroupName() + " group!");
            }else {
                validCmd.setTarget(validCmd.getUserResult());
                validCmd.setResult(true, "");
                group.removeUser(validCmd.getTarget());
            }
        }
        sendBack(validCmd, sender);
    }

    //validate the promote/demote GroupCommand
    //if the command has passed the validation,
    //then, if the target exits in the group
    //the server promote/demote the target user in the group
    //and send back the relevant message back to the source user.
    private void changePreviledges(GroupCommand cmd, ActorRef sender) {
        Group group = this.groupsMap.get(cmd.getGroupName());
        GroupCommand validCmd = validateCmd(cmd, group);
        if (validCmd.isSucceed()){
            Group.PType targetType = group.getUserPType(cmd.getTarget().getUserName());
            if (targetType.equals(Group.PType.Not_User)){
                validCmd.setResult(false, validCmd.getTarget().getUserName() + " is not in " +
                        validCmd.getGroupName() + " group!");
            }else {
                validCmd.setTarget(validCmd.getUserResult());
                validCmd.setResult(true, "");

                if (cmd.getType().equals(Command.Type.Group_Promote))
                    group.promoteUser(validCmd.getTarget());
                else if (cmd.getType().equals(Command.Type.Group_Demote))
                    group.demoteUser(validCmd.getTarget());

            }
        }
        sendBack(validCmd, sender);
    }

    //adds user to group while the invitation already
    //validate by the source user and the the server itself
    //and send the target "Welcome" message
    private void addUserToGroup(Invitation inv) {
        Group group = this.groupsMap.get(inv.getGroupName());
        if (group != null){
            User target = this.usersMap.get(inv.getTarget().getUserName());
            if (target != null) {
                group.addUser(inv.getTarget());
                inv.setAnswer("Welcome to " + inv.getGroupName() + "!");
                target.getUserRef().tell(inv, self());
            }
        }
    }

    //return the command group name depends on the command instance
    private String getGroupName(Command cmd){
        String name = "";

        if (cmd instanceof TextMessage)
            name = ((TextMessage) cmd).getTarget().getUserName();
        else if (cmd instanceof FileCommand)
            name =  ((FileCommand) cmd).getTarget().getUserName();

        return name;
    }

    //return the command source name depends on the command instance
    private String getsourceName(Command cmd){
        String name = "";

        if (cmd instanceof TextMessage)
            name = ((TextMessage) cmd).getSource().getUserName();
        else if (cmd instanceof FileCommand)
            name =  ((FileCommand) cmd).getSource().getUserName();

        return name;
    }

    //if the target user belongs to the group and
    //the source user isn't muted, the server
    //sends the file/text message to all the group
    //and the server send the relevant result to the sender
    private void groupMessage(Command cmd, ActorRef sender){
        String sourceName = getsourceName(cmd);
        String groupName = getGroupName(cmd);

        Group group = this.groupsMap.get(groupName);
        if (group != null){
            Group.PType sourceType = group.getUserPType(sourceName);
            if (!sourceType.equals(Group.PType.Not_User)){
                if (!sourceType.equals(Group.PType.Muted)) {
                    cmd.setResult(true, "");
                    group.getRouter().route(cmd, self());
                }else
                    cmd.setResult(false, "You are muted for " + group.getMutedTime(sourceName) +  " in " + groupName + "!");
            } else
                cmd.setResult(false, "You are not part of " + groupName + "!");
        }else
            cmd.setResult(false, groupName + " does not exist!");

        sendBack(cmd, sender);
    }

    //return "time is up!" TextMessage
    private TextMessage muteTimeIsUp(GroupCommand cmd){
        TextMessage textCmd =  new TextMessage(cmd.getSource(), cmd.getTarget(),
                "You have been unmuted! Muting time is up!");
        textCmd.setType(Command.Type.Group_UnMute);
        return textCmd;
    }

    //return muted message TextMessage
    private TextMessage mutedMessage(GroupCommand cmd, long mutedTime){
        TextMessage textCmd =  new TextMessage(cmd.getSource(), new User(cmd.getGroupName()),
                "You have been muted for " + mutedTime + " in "
                        + cmd.getGroupName() + " by " + cmd.getSource().getUserName() + "!");
        return setGroupTextProps(textCmd, cmd);
    }

    //return unmuted message TextMessage
    private TextMessage unMuteMessage(GroupCommand cmd){
        TextMessage textCmd =  new TextMessage(cmd.getSource(), new User(cmd.getGroupName()),
                "You have been unmuted in " + cmd.getGroupName() +
                        " by " +  cmd.getSource().getUserName() + "!");
        return setGroupTextProps(textCmd, cmd);
    }

    //set TextMessage User result to target User,
    //succeeded to true, and command type to Group_Text
    private TextMessage setGroupTextProps(TextMessage toSet, GroupCommand values) {
        toSet.setUserResult(true, values.getTarget());
        toSet.setResult(true, "");
        toSet.setType(Command.Type.Group_Text);
        return toSet;
    }

    //validate the mute user GroupCommand
    //if the command has passed the validation,
    //then, if the target exits in the group
    //the server mutes the target user in the group,
    //sets lambda to send unmute the target after duration time
    //and send back the relevant message back to the source user.
    private void muteUser(GroupCommand cmd, ActorRef sender){
        Group group = this.groupsMap.get(cmd.getGroupName());
        GroupCommand validCmd = validateCmd(cmd, group);
        if (validCmd.isSucceed()){
            validCmd.setTarget(validCmd.getUserResult());
            Group.PType targetType = group.getUserPType(cmd.getTarget().getUserName());
            if (targetType.equals(Group.PType.Not_User)) {
                validCmd.setResult(false, validCmd.getTarget().getUserName() + " is not in " +
                        validCmd.getGroupName() + " group!");
            } else {
                Cancellable cancel = scheduler.scheduleOnce(validCmd.getDuration(),() -> {
                    group.unMuteUser(validCmd.getTarget()); //Unmute user
                    sendBack(muteTimeIsUp(validCmd), validCmd.getTarget().getUserRef()); //inform the target about unmute
                },context().system().dispatcher());
                //mute target to duration time;
                group.muteUser(validCmd.getTarget(),validCmd.getDuration(),cancel);
                TextMessage textCmd = mutedMessage(validCmd, group.getMutedTime(validCmd.getTarget().getUserName()));
                sendBack(textCmd, sender);
                return;
            }
        }
        //send back to sender with false result
        sendBack(validCmd, sender);
    }

    //validate the unmute GroupCommand
    //if the command has passed the validation,
    //then, if the target exits in the group and muted
    //the server set back the target user privilege in the group
    //and send back the relevant message back to the source user.
    private void unmuteUser(GroupCommand cmd, ActorRef sender){
        Group group = this.groupsMap.get(cmd.getGroupName());
        GroupCommand validCmd = validateCmd(cmd, group);
        if (validCmd.isSucceed()){
            validCmd.setTarget(validCmd.getUserResult());
            Group.PType targetType = group.getUserPType(cmd.getTarget().getUserName());
            if (targetType.equals(Group.PType.Not_User)) {
                validCmd.setResult(false, validCmd.getTarget().getUserName() + " is not in " +
                        validCmd.getGroupName() + " group!");
            }else if (!targetType.equals(Group.PType.Muted)) {
                validCmd.setResult(false, validCmd.getTarget().getUserName() + " is not muted!");
            } else {
                //unmute target
                group.unMuteUser(validCmd.getTarget());
                TextMessage textCmd = unMuteMessage(validCmd);
                sendBack(textCmd, sender);
                return;
            }
        }
        //send back to sender with false result
        sendBack(validCmd, sender);
    }*/


    public void print(String string) {
        System.out.println(string);
    }

    public Receive createReceive() {

        return receiveBuilder() // need to decicde how to create an actor for each group
                .match(ConnectCommand.class, predicates.connectCommandPred, (cmd) -> {
                    print("user actor path is " + sender().path().toString());
                    connectUser(cmd, sender());
                })
                .match(DisConnectCommand.class, predicates.disconnectCmd, (cmd) -> disconnectUser(cmd, sender()))
                .match(TextMessage.class, (cmd) -> userMessage(cmd, sender()))
                .match(FileMessage.class, (cmd) -> userFile(cmd, sender()))
                .match(CreateGroupCommand.class, predicates.createGroupServer, (cmd) -> sendToGroupManager(cmd, sender()))
                .match(InviteGroup.class, predicates.InviteGroupServer, (cmd) -> sendInviteGroupManager(cmd, sender()))
                .match(GroupTextMessage.class, (msg) -> sendToGroupManager(msg, sender()))
                .match(String.class, System.out::println)
                .matchAny((cmd) -> System.out.println("problem" + cmd + "problem"))
                .build();
    }
}
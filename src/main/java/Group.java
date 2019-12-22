import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class Group extends AbstractActor implements Serializable {
    private String groupName;
    private final User admin;
    protected List<String> co_admins_list;
    protected HashMap<String, User> groupUsersMap;
    private Predicates predicates;
    private Router router;
    private List<Routee> routees;


    static public Props props(String groupName, User admin) {
        return Props.create(Group.class, () -> new Group(groupName, admin));
    }


    public Group(String groupName, User admin) {
        this.groupName = groupName;
        this.admin = admin;
        groupUsersMap = new HashMap<>();
        groupUsersMap.put(admin.getUserName(), admin);
        co_admins_list = new LinkedList<>();
        predicates = new Predicates();
        routees = new ArrayList<>();
        routees.add(new ActorRefRoutee(admin.getUserActorRef()));
        router = new Router(new BroadcastRoutingLogic(), routees);


    }

    public String getGroupName() {
        return groupName;
    }

    public User getAdmin() {
        return admin;
    }

    public HashMap<String, User> getGroupUsersMap() {
        return groupUsersMap;
    }

    public void addUser(User user) {
        groupUsersMap.put(user.getUserName(), user);
        routees.add(new ActorRefRoutee(user.getUserActorRef()));
        this.router = router.addRoutee(user.getUserActorRef());
        printFromGroupsConnection("groupUsersMap is :\n" + groupUsersMap.toString());
    }

    /*TODO after deleting the group i cant use the same name again - fix it!*/
    public void remove(User user) {
        if (this.groupUsersMap.containsKey(user.getUserName())) {// user is indeed in group
            /*removing the user*/
            routees.remove(new ActorRefRoutee(user.getUserActorRef()));
            router = router.removeRoutee(user.getUserActorRef());
            this.getGroupUsersMap().remove(user.getUserName());
            /*user is admin*/
            if (user.getUserName().equals(this.admin.getUserName())) {
                router.route(new Command(Command.Type.Group_Leave, Command.From.Group,
                        user.getUserName() + " has left " + groupName + "!"), self());
                router.route(new Command(Command.Type.Group_Leave, Command.From.Group, "" +
                        "[" + groupName + "] admin has closed " + groupName + "! group will be deleted"), self());
                /*telling each member of the group to delete the content of the group*/
                router.route(new Command(Command.Type.USER_DELETE_THIS_GROUP, Command.From.Group, this.groupName),
                        self());
                /*deleting the group*/
                getContext().parent().tell(new GroupCommand
                        (Command.Type.Delete_Group, Command.From.Group, self(), this.groupName), self());
            } else if (this.co_admins_list.contains(user.getUserName())) {
                /*user is co-admin*/
                co_admins_list.remove(user.getUserName());
                router.route(new Command(Command.Type.Group_Leave, Command.From.Group,
                        user.getUserName() + " is removed from co-admin list in " + groupName), self());
            } else {
                router.route(new Command(Command.Type.Group_Leave, Command.From.Group,
                        user.getUserName() + " has left " + groupName + "!"), self());
            }
        }
        /*user is not in the group*/
        else {
            user.getUserActorRef().tell(new Command(Command.Type.Error, Command.From.Group,
                    user.getUserName() + " is not in " + this.groupName), self());
        }


    }

    public boolean promote_co_admin(User user) {
        if (groupUsersMap.containsKey(user.getUserName())) {
            co_admins_list.add(user.getUserName());
            return true;
        } else
            return false;
    }

    private boolean verifyInvitation(InviteGroup inviteGroup, ActorRef UserActor) {
        String SourceUserName = inviteGroup.getSourceUser().getUserName();
        inviteGroup.setFrom(Command.From.Group);
        if ((!checkAdminUserName(SourceUserName) && (!checkCo_adminUserName(SourceUserName)))) {
            inviteGroup.setResult(false,
                    "You are neither an admin nor a co-admin of " + this.groupName + "!");
            UserActor.tell(inviteGroup, self());
            return false;
        } else if (this.groupUsersMap.containsKey(inviteGroup.getTarget())) {
            inviteGroup.setResult(false, inviteGroup.getTarget() + " is already in " + this.groupName);
            UserActor.tell(inviteGroup, self());
            return false;
        } else {
            return true;
        }
    }

    /*TODO might delete userActor*/
    private void inviteUser(InviteGroup inviteGroup, ActorRef UserActor) {
        if (verifyInvitation(inviteGroup, UserActor)) {
            inviteGroup.setFrom(Command.From.Group);
            inviteGroup.setGroupActorRef(self());
            inviteGroup.getTargetActorRef().tell(inviteGroup, self());

        }

    }

    private void sendGroupMessage(GroupCommand groupMessage) {
        printFromGroupsConnection("im in sendGroup message");
        groupMessage.setFrom(Command.From.Group);
        if (this.getGroupUsersMap().containsKey(groupMessage.getSourceUser().getUserName()))
            router.route(groupMessage, self());
        else {
            groupMessage.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                    "You are not part of " + groupName)
                    , self());
        }
    }

    /*private void sendGroupFileMessage(GroupCommand groupFileMessage) {
        groupFileMessage.setFrom(Command.From.Group);
        if (this.getGroupUsersMap().containsKey(groupFileMessage.getSourceUser().getUserName()))
            router.route(groupFileMessage, self());
        else {
            groupFileMessage.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                    "You are not part of " + groupName)
                    , self());
        }
    }*/


    private boolean checkAdminUserName(String SourceUserName) {
        return SourceUserName.equals(this.admin.getUserName());
    }

    private boolean checkCo_adminUserName(String SourceUserName) {
        return this.co_admins_list.contains(SourceUserName);
    }

    @Override
    public String toString() {
        return "Group{" +
                "groupName='" + groupName + '\'' +
                ", admin=" + admin +
                ", co_admins_list=" + co_admins_list +
                ", groupUsersMap=" + groupUsersMap +
                '}';
    }

    private void printFromGroupsConnection(String message) {
        getContext().parent().tell(message, getSelf());
    }

    private void getReplyToInvitation(InviteGroup inviteGroup) {
        User Target = inviteGroup.getUserResult();
        User Source = inviteGroup.getSourceUser();
        if (inviteGroup.getAnswer().equals("Yes")) {
            /*Todo handle patterns*/
            addUser(inviteGroup.getUserResult());
            Source.getUserActorRef().tell(new Command(Command.Type.invitationAnswer, Command.From.Group,
                    Target.getUserName() + " has accepted the invitation"), self());
            Target.getUserActorRef().tell(new Command(Command.Type.WelcomeMessage, Command.From.Group,
                    "Welcome to " + groupName + "!"), self());
        } else {
            Source.getUserActorRef().tell(new Command(Command.Type.invitationAnswer, Command.From.Group,
                    Target.getUserName() + " has declined the invitation"), self());
        }
        printUsers();
    }

    private void AdminOrCo_adminRemoveUser(RemoveUserGroup removeUserGroup) {
        printFromGroupsConnection("im in RemoveUserGroup\n" + removeUserGroup.toString());
        /*Target user is not in the group*/
        Command returnCommand = new Command(Command.Type.Error, Command.From.Group);
        User sourceUser = removeUserGroup.getSourceUser();
        ActorRef sourceUserRef = removeUserGroup.getSourceUser().getUserActorRef();
        if (!this.getGroupUsersMap().containsKey(removeUserGroup.getUserToRemove())) {
            printFromGroupsConnection(groupName + ": im in if");
            returnCommand.setResult(false,
                    removeUserGroup.getUserToRemove() + "does not exist in " + this.groupName + "!");
            sourceUserRef.tell(returnCommand, self());
            /*sourceUser is not an admin nor a co admin*/
        } else if ((!checkAdminUserName(sourceUser.getUserName()))
                && (!checkCo_adminUserName(sourceUser.getUserName()))) {
            printFromGroupsConnection(groupName + ": im in else if1");
            returnCommand.setResult(false,
                    "You are neither an admin nor a co-admin of " + this.groupName + "!");
            sourceUserRef.tell(returnCommand, self());
            /*if trying to remove group admin*/
        } else if (this.admin.getUserName().equals(removeUserGroup.getUserToRemove())) {
            printFromGroupsConnection(groupName + ": im in else if2");
            returnCommand.setResult(false,
                    "You are trying to remove an admin, you can't do that");
            sourceUserRef.tell(returnCommand, self());
        }
        /*now we can safely remove the user*/
        else {

            User toRemoveUser = this.groupUsersMap.get(removeUserGroup.getUserToRemove());
            ActorRef toRemoveUserRef = toRemoveUser.getUserActorRef();
            this.getGroupUsersMap().remove(toRemoveUser.getUserName());//removed from the group
            routees.remove(new ActorRefRoutee(toRemoveUserRef));
            router = router.removeRoutee(toRemoveUserRef);
            router.route(new Command(
                    Command.Type.Group_Leave,
                    Command.From.Group,
                    "[" + groupName + "][" + sourceUser.getUserName() + "]: " +
                            removeUserGroup.getUserToRemove() + " is removed from " + groupName), self());
            toRemoveUserRef.tell(new Command(
                            Command.Type.Group_Leave,
                            Command.From.Group,
                            "You have been removed from " + groupName + " by " + removeUserGroup.getSourceUser().getUserName())
                    , self());
            if (this.co_admins_list.contains(removeUserGroup.getUserToRemove())) {
                co_admins_list.remove(removeUserGroup.getUserToRemove());
                router.route(new Command(
                                Command.Type.Group_Leave,
                                Command.From.Group,
                                removeUserGroup.getUserToRemove() + " is removed from co-admin list in " + groupName)
                        , self());
            }
            toRemoveUserRef.tell(new Command(Command.Type.USER_DELETE_THIS_GROUP,
                    Command.From.Group, groupName), self());
        }

    }

    private void promoteUser(CoAdminCommand command) {
        String SourceUserName = command.getSourceUser().getUserName();
        if (this.getGroupUsersMap().containsKey(command.getTargetUser())) {// targetUser is in group
            if ((checkAdminUserName(SourceUserName) || (checkCo_adminUserName(SourceUserName)))) {//source is valid
                if (command.getType().equals(Command.Type.Group_Promote)) {
                    this.co_admins_list.add(command.getTargetUser());//added as co-admin
                    User targetUser = this.groupUsersMap.get(command.getTargetUser());
                    ActorRef targetUserRef = targetUser.getUserActorRef();
                    printFromGroupsConnection(targetUser.toString());
                    command.setResult(true,
                            "You have been promoted to co-admin in " + groupName + "!");
                    command.setFrom(Command.From.Group);
                    targetUserRef.tell(command, self());
                } else if (command.getType().equals(Command.Type.Group_Demote)) {
                    if (this.co_admins_list.remove(command.getTargetUser())) {
                        User targetUser = this.groupUsersMap.get(command.getTargetUser());
                        ActorRef targetUserRef = targetUser.getUserActorRef();
                        command.setResult(true,
                                "You have been demoted to user in " + groupName + "!");
                        command.setFrom(Command.From.Group);
                        targetUserRef.tell(command, self());
                    } else {
                        command.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                                command.getTargetUser() + " isn't a co-admin in " + groupName)
                                , self());
                    }

                }
            } else {
                command.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                        "You are neither an admin nor a co-admin of " + groupName)
                        , self());
            }
        } else {// targetUser isn't part of the group
            command.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                    command.getTargetUser() + " is not in the group")
                    , self());

        }
    }

    public void printUsers() {
        printFromGroupsConnection(groupName + " users are:\n" + groupUsersMap.toString());
    }

    private Command CreateErrorCmd(String errorString) {
        Command cmd = new Command(Command.Type.Error, Command.From.Group);
        cmd.setResult(false, errorString);
        return cmd;
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(InviteGroup.class, predicates.GroupInviteGroup, (invitation) ->
                        inviteUser(invitation, sender()))
                .match(InviteGroup.class, predicates.getReplyToInvitation, this::getReplyToInvitation)
                .match(DisConnectCommand.class, cmd -> remove(cmd.getUser()))
                .match(GroupCommand.class, predicates.Group_GroupLeave, cmd -> remove(cmd.getSourceUser()))
                .match(GroupTextMessage.class, this::sendGroupMessage)
                .match(RemoveUserGroup.class, predicates.removeUserFromGroup, this::AdminOrCo_adminRemoveUser)
                .match(CoAdminCommand.class, predicates.PromoteCommand, this::promoteUser)
                .match(GroupFileMessage.class, predicates.GroupFileMessage_Group, this::sendGroupMessage)
                .matchAny((cmd) -> printFromGroupsConnection("im in maatchany group:" + cmd.toString()))
                .build();
    }


}

import akka.actor.*;
import akka.routing.*;

import java.io.Serializable;
import java.time.Duration;
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
    protected HashMap<String, Pair<Duration, Cancellable>> groupUsersMutes;
    private Scheduler scheduler;

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
        this.scheduler = context().system().scheduler();
        groupUsersMutes = new HashMap<>();
    }


    private void unMuteUser(MuteGroup muteGroup, ActorRef sender) {
        User source = muteGroup.getSourceUser();
        //target is not in the group
        if (!groupUsersMap.containsKey(muteGroup.getTarget())) {
            sender.tell(new Command(Command.Type.Error, Command.From.Group,
                    muteGroup.getTarget() + " does not exist! "), getSelf());
        }
        //target is not muted
        if (!groupUsersMutes.containsKey(muteGroup.getTarget())) {
            sender.tell(new Command(Command.Type.Error, Command.From.Group,
                    muteGroup.getTarget() + " is not muted! "), getSelf());
        }
        //source is not admin or co admin
        if (!co_admins_list.contains(source.getUserName()) && !admin.getUserName().equals(source.getUserName())) {
            sender.tell(new Command(Command.Type.Error, Command.From.Group,
                    "You are neither an\n" + "admin nor a co-admin of  " + muteGroup.getGroupName()), getSelf());
        } else {
            User target = groupUsersMap.get(muteGroup.getTarget());
            Pair<Duration, Cancellable> mutedUserInfo = this.groupUsersMutes.remove(target.getUserName());
            if (mutedUserInfo != null) {
                mutedUserInfo.getValue().cancel();
                tellUserActor(new GroupTextMessage(groupName, "You have been unmuted in " + this.groupName + " by " + source.getUserName() + "!", Command.Type.Group_Text, Command.From.Group), target);
            }
        }
    }

    private void tellUserActor(GroupTextMessage msg, User target) {
        msg.setSourceUser(target);
        target.getUserActorRef().tell(msg, getSelf());
    }

    private void Mute(MuteGroup muteGroup, ActorRef sender) {
        User source = muteGroup.getSourceUser();
        String SourceUserName = source.getUserName();
        //edge cases
        if (!groupUsersMap.containsKey(muteGroup.getTarget())) {
            sender.tell(new Command(Command.Type.Error, Command.From.Group,
                    muteGroup.getTarget() + " does not exist! "), getSelf());
        } else if ((!checkAdminUserName(SourceUserName)) && (!checkCo_adminUserName(SourceUserName))) {
            sender.tell(new Command(Command.Type.Error, Command.From.Group,
                    "You are neither an admin nor a co-admin of " + this.groupName + "!"), getSelf());
        } else {
            //schedule the unmute action
            User target = groupUsersMap.get(muteGroup.getTarget());
            Cancellable cancel = scheduler.scheduleOnce(muteGroup.getDuration(), () -> {
                this.groupUsersMutes.remove(target.getUserName()); //remove user from the muted list
                tellUserActor(new GroupTextMessage(groupName, "You have been unmuted! Muting time is up! ", Command.Type.Group_Text, Command.From.Group), target);

            }, context().system().dispatcher());
            Pair<Duration, Cancellable> mutedUserInfo = new Pair<>(muteGroup.getDuration(), cancel);
            // add the user's info to the mutedUsers list
            groupUsersMutes.put(target.getUserName(), mutedUserInfo);
            // send the user a message that he's been muted
            tellUserActor(new GroupTextMessage(groupName, "You have been muted for " + muteGroup.getDuration().getSeconds() + " in " + this.groupName + " by " + source.getUserName() + " !", Command.Type.Group_Text, Command.From.Group), target);
        }
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
    }

    public void remove(User user) {
        if (this.groupUsersMutes.containsKey(user.getUserName())) {  //removing from mutes
            groupUsersMutes.remove(user.getUserName());

        }
        if (this.groupUsersMap.containsKey(user.getUserName())) {// user is indeed in group
            /*removing the user*/
            routees.remove(new ActorRefRoutee(user.getUserActorRef()));
            router = router.removeRoutee(user.getUserActorRef());
            this.getGroupUsersMap().remove(user.getUserName());
            user.getUserActorRef().tell(new Command(Command.Type.USER_DELETE_THIS_GROUP,
                            Command.From.Group,
                            this.groupName),
                    self());
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

            }
            router.route(new Command(Command.Type.Group_Leave, Command.From.Group,
                    user.getUserName() + " has left " + groupName + "!"), self());

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
            inviteGroup.setResult(false, String.format("%s is already in %s!", inviteGroup.getTarget(), this.groupName));
            UserActor.tell(inviteGroup, self());
            return false;
        } else {
            return true;
        }
    }

    private void inviteUser(InviteGroup inviteGroup, ActorRef UserActor) {
        if (verifyInvitation(inviteGroup, UserActor)) {
            inviteGroup.setFrom(Command.From.Group);
            inviteGroup.setGroupActorRef(self());
            inviteGroup.getTargetActorRef().tell(inviteGroup, self());

        }

    }

    private void sendGroupMessage(GroupCommand groupMessage) {
        String sourceName = groupMessage.getSourceUser().getUserName();
        groupMessage.setFrom(Command.From.Group);
        if (this.getGroupUsersMap().containsKey(sourceName)) {
            if (this.groupUsersMutes.containsKey(sourceName)) {
                groupMessage.getSourceUser().getUserActorRef()
                        .tell(CreateErrorCmd
                                        (String.format("You are muted for %d in %s! ",
                                                this.groupUsersMutes.get(sourceName).getKey().getSeconds(),
                                                groupName)),
                                self());
            } else {
                router.route(groupMessage, self());
            }
        } else {
            groupMessage.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                    "You are not part of " + groupName)
                    , self());
        }
    }


    private boolean checkAdminUserName(String SourceUserName) {
        return SourceUserName.equals(this.admin.getUserName());
    }

    private boolean checkCo_adminUserName(String SourceUserName) {
        return this.co_admins_list.contains(SourceUserName);
    }


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

    }

    private void AdminOrCo_adminRemoveUser(RemoveUserGroup removeUserGroup) {
        /*Target user is not in the group*/
        Command returnCommand = new Command(Command.Type.Error, Command.From.Group);
        User sourceUser = removeUserGroup.getSourceUser();
        ActorRef sourceUserRef = removeUserGroup.getSourceUser().getUserActorRef();
        if (!this.getGroupUsersMap().containsKey(removeUserGroup.getUserToRemove())) {
            returnCommand.setResult(false,
                    removeUserGroup.getUserToRemove() + " does not exist in " + this.groupName + "!");
            sourceUserRef.tell(returnCommand, self());
            /*sourceUser is not an admin nor a co admin*/
        } else if ((!checkAdminUserName(sourceUser.getUserName()))
                && (!checkCo_adminUserName(sourceUser.getUserName()))) {
            returnCommand.setResult(false,
                    "You are neither an admin nor a co-admin of " + this.groupName + "!");
            sourceUserRef.tell(returnCommand, self());
            /*if trying to remove group admin*/
        } else if (this.admin.getUserName().equals(removeUserGroup.getUserToRemove())) {
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
            toRemoveUserRef.tell(new Command(Command.Type.Group_Leave,
                            Command.From.Group,
                            "You have been removed from " + groupName + " by " + removeUserGroup.getSourceUser().getUserName() + "!")
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

    private void promoteUser(CoAdminCommand coAdminCommand) {
        String SourceUserName = coAdminCommand.getSourceUser().getUserName();
        if (this.getGroupUsersMap().containsKey(coAdminCommand.getTargetUser())) {// targetUser is in group
            if ((checkAdminUserName(SourceUserName) || (checkCo_adminUserName(SourceUserName)))) {//source is valid
                if (coAdminCommand.getType().equals(Command.Type.Group_Promote)) {
                    this.co_admins_list.add(coAdminCommand.getTargetUser());//added as co-admin
                    User targetUser = this.groupUsersMap.get(coAdminCommand.getTargetUser());
                    ActorRef targetUserRef = targetUser.getUserActorRef();
                    coAdminCommand.setResult(true,
                            "You have been promoted to co-admin in " + groupName + "!");
                    coAdminCommand.setFrom(Command.From.Group);
                    targetUserRef.tell(coAdminCommand, self());
                } else if (coAdminCommand.getType().equals(Command.Type.Group_Demote)) {
                    if (this.co_admins_list.remove(coAdminCommand.getTargetUser())) {
                        User targetUser = this.groupUsersMap.get(coAdminCommand.getTargetUser());
                        ActorRef targetUserRef = targetUser.getUserActorRef();
                        coAdminCommand.setResult(true,
                                "You have been demoted to user in " + groupName + "!");
                        coAdminCommand.setFrom(Command.From.Group);
                        targetUserRef.tell(coAdminCommand, self());
                    } else {
                        coAdminCommand.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                                coAdminCommand.getTargetUser() + " isn't a co-admin in " + groupName)
                                , self());
                    }

                }
            } else {
                coAdminCommand.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                        "You are neither an admin nor a co-admin of " + groupName)
                        , self());
            }
        } else {// targetUser isn't part of the group
            coAdminCommand.getSourceUser().getUserActorRef().tell(CreateErrorCmd(
                    coAdminCommand.getTargetUser() + " is not in the group")
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
                .match(MuteGroup.class, predicates.MuteGroup, cmd -> Mute(cmd, getSender()))
                .match(MuteGroup.class, predicates.unMuteGroup, cmd -> unMuteUser(cmd, getSender()))
                .match(InviteGroup.class, predicates.GroupInviteGroup, (invitation) ->
                        inviteUser(invitation, sender()))
                .match(InviteGroup.class, predicates.getReplyToInvitation, this::getReplyToInvitation)
                .match(DisConnectCommand.class, cmd -> remove(cmd.getUser()))
                .match(GroupCommand.class, predicates.Group_GroupLeave, cmd -> remove(cmd.getSourceUser()))
                .match(GroupTextMessage.class, this::sendGroupMessage)
                .match(RemoveUserGroup.class, predicates.removeUserFromGroup, this::AdminOrCo_adminRemoveUser)
                .match(CoAdminCommand.class, predicates.PromoteCommand, this::promoteUser)
                .match(GroupFileMessage.class, predicates.GroupFileMessage_Group, this::sendGroupMessage)
                .matchAny((cmd) -> printFromGroupsConnection("im in matchAny group:" + cmd.toString()))
                .build();
    }


}

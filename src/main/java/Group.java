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
        printUsers();
        printFromGroupsConnection("admin is : \t" + admin.getUserName());
        if (this.groupUsersMap.containsKey(user.getUserName())) {// user is indeed in group
            /*removing the user*/
            groupUsersMap.remove(user.getUserName());
            routees.remove(new ActorRefRoutee(user.getUserActorRef()));
            router = router.removeRoutee(user.getUserActorRef());
            /*user is admin*/
            printFromGroupsConnection("delete username is:\t" + user.getUserName());
            if (user.getUserName().equals(this.admin.getUserName())) {
                printFromGroupsConnection("deleting admin");
                router.route(new Command(Command.Type.Group_Leave, Command.From.Group,
                        user.getUserName() + " has left " + groupName + "!"), self());
                router.route(new Command(Command.Type.Group_Leave, Command.From.Group, "" +
                        "[" + groupName + "] admin has closed " + groupName + "! group will be deleted"), self());
                /*deleting the group*/
                getContext().parent().tell(new GroupCommand
                        (Command.Type.Delete_Group, Command.From.Group), self());
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
        inviteGroup.setFrom(Command.From.Group);
        if ((!checkAdminUserName(inviteGroup)) && (!checkCo_adminUserName(inviteGroup))) {
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

    private void sendGroupMessage(GroupTextMessage groupTextMessage) {
        printFromGroupsConnection("im in sendGroup message");
        groupTextMessage.setFrom(Command.From.Group);
        router.route(groupTextMessage, self());
    }


    private boolean checkAdminUserName(InviteGroup inviteGroup) {
        return inviteGroup.getSourceUser().getUserName().equals(this.admin.getUserName());
    }

    private boolean checkCo_adminUserName(InviteGroup inviteGroup) {
        return this.co_admins_list.contains(inviteGroup.getSourceUser());
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

    public void printUsers() {
        printFromGroupsConnection(groupName + " users are:\n" + groupUsersMap.toString());
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(InviteGroup.class, predicates.GroupInviteGroup, (invitation) ->
                        inviteUser(invitation, sender()))
                .match(InviteGroup.class, predicates.getReplyToInvitation, this::getReplyToInvitation)
                .match(DisConnectCommand.class, cmd -> remove(cmd.getUser()))
                .match(GroupTextMessage.class, this::sendGroupMessage)
                .matchAny((cmd) -> printFromGroupsConnection(cmd.toString()))
                .build();
    }


}

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class Group extends AbstractActor implements Serializable {
    private String groupName;
    private User admin;
    protected List<User> co_admins_list;
    protected HashMap<String, User> groupUsersMap;
    private Predicates predicates;


    static public Props props(String groupName, User admin) {
        return Props.create(Group.class, () -> new Group(groupName, admin));
    }


    public Group(String groupName, User admin) {
        this.groupName = groupName;
        this.admin = admin;
        groupUsersMap = new HashMap<>();
        groupUsersMap.put(admin.getUserName(), admin);
        this.co_admins_list = new LinkedList<>();
        predicates = new Predicates();


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
        printFromGroupsConnection("groupUsersMap is :\n" + groupUsersMap.toString());
    }

    public void remove(User user) {
        if (!(user.getUserName().equals(admin.getUserName())))
            groupUsersMap.remove(user.getUserName(), user);
        else{
            /*TODO add a delte for the group when the user remove is admin*/
        }

    }

    public boolean promote_co_admin(User user) {
        if (groupUsersMap.containsKey(user.getUserName())) {
            co_admins_list.add(user);
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
            groupUsersMap.put(inviteGroup.getTarget(), inviteGroup.getUserResult());
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
                .matchAny((cmd) -> printFromGroupsConnection(cmd.toString()))
                .build();
    }


}

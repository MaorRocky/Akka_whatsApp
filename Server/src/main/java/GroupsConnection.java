import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;

public class GroupsConnection extends AbstractActor {

    protected HashMap<String, ActorRef> GroupsMap;
    protected Predicates predicates;

    static public Props props() {
        return Props.create(GroupsConnection.class, GroupsConnection::new);
    }

    public GroupsConnection() {
        this.GroupsMap = new HashMap<>();
        predicates = new Predicates();

    }

    private void printFromServer(String message) {
        getContext().parent().tell(message, getSelf());
    }

    protected void CreateGroup(CreateGroupCommand cmd, ActorRef UserActor) {
        if (GroupsMap.containsKey(cmd.getGroupName())) {
            cmd.setResult(false, cmd.getGroupName() + " already exists");
        } else {
            final ActorRef group = getContext().actorOf(
                    Props.create(Group.class, cmd.getGroupName(), cmd.getUserAdmin()), "group" + cmd.getGroupName());
            GroupsMap.put(cmd.getGroupName(), group);
            cmd.setResult(true, cmd.getGroupName() + " created successfully");
        }
        printFromServer(GroupsMap.toString());
        sendBack(cmd, UserActor);
    }

    public void GroupInvite(InviteGroup inviteGroup, ActorRef UserActor) {

    }

    private boolean checkIfGroupExists(InviteGroup inviteGroup) {
        return GroupsMap.containsKey(inviteGroup.getGroupName());
    }

    /*checks if the user who sent the invite is admin/co-admin*/
    private boolean checkIfInvitationisValid(InviteGroup inviteGroup){


    }


    private void sendBack(Command command, ActorRef sender) {
        command.setFrom(Command.From.GroupsConnection);
        sender.tell(command, getSelf());
    }


    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(CreateGroupCommand.class, predicates.GroupsConnectionCreateGroup, (msg) -> CreateGroup(msg, sender()))
                .match(String.class, this::printFromServer)
                .match(InviteGroup.class, predicates.GroupsConnectionInviteGroup,
                        (invitation) -> GroupInvite(invitation, sender()))
                .matchAny((cmd) -> printFromServer(cmd.toString()))
                .build();
    }
}

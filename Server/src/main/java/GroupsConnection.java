import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.HashSet;

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
                    Props.create(Group.class, cmd.getGroupName(), cmd.getSourceUser()), "group" + cmd.getGroupName());
            GroupsMap.put(cmd.getGroupName(), group);
            cmd.setResult(true, cmd.getGroupName() + " created successfully");
        }
        printFromServer("Groups map is:\n" + GroupsMap.toString() + "\n");
        sendBack(cmd, UserActor);
    }

    public void GroupInvite(InviteGroup inviteGroup, ActorRef UserActor) {
        inviteGroup.setFrom(Command.From.GroupsConnection);
        String groupName = inviteGroup.getGroupName();
        if (checkIfGroupExists(inviteGroup)) {
            GroupsMap.get(groupName).tell(inviteGroup, UserActor);
        } else {
            inviteGroup.setResult(false, groupName + " does not exist!");
            UserActor.tell(inviteGroup, self());
        }
    }

    private boolean checkIfGroupExists(InviteGroup inviteGroup) {
        return GroupsMap.containsKey(inviteGroup.getGroupName());
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
                /*.match(InviteGroup.class, predicates.GroupInviteGroup_verfiedAdmin,
                        (invitation) -> sendInviteToDesiredUser(msg, sender()))*/

                .matchAny((cmd) -> printFromServer(cmd.toString()))
                .build();
    }
}

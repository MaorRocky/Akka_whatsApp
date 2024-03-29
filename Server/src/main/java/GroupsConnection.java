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
            cmd.setResult(false, cmd.getGroupName() + " already exists!");
        } else {
            final ActorRef group = getContext().actorOf(
                    Props.create(Group.class, cmd.getGroupName(), cmd.getSourceUser()), "group" + cmd.getGroupName());
            GroupsMap.put(cmd.getGroupName(), group);
            cmd.setResult(true, cmd.getGroupName() + " created successfully!");
            cmd.setGroupRef(group);
            cmd.setFrom(Command.From.GroupsConnection);
        }
        sendBack(cmd, UserActor);
    }

    public void GroupInvite(InviteGroup inviteGroup, ActorRef UserActor) {
        inviteGroup.setFrom(Command.From.GroupsConnection);
        String groupName = inviteGroup.getGroupName();
        if (checkIfGroupExists(groupName)) {
            GroupsMap.get(groupName).tell(inviteGroup, UserActor);
        } else {
            inviteGroup.setResult(false, groupName + " does not exist!");
            UserActor.tell(inviteGroup, self());
        }
    }

    private boolean checkIfGroupExists(String group) {
        return GroupsMap.containsKey(group);
    }


    private void sendBack(Command command, ActorRef sender) {
        command.setFrom(Command.From.GroupsConnection);
        sender.tell(command, getSelf());
    }


    private void sendToGroup(GroupCommand groupConnection) {
        groupConnection.setFrom(Command.From.GroupsConnection);
        String groupName = groupConnection.getGroupName();
        if (checkIfGroupExists(groupName)) {
            GroupsMap.get(groupName).tell(groupConnection, self());
        } else {
            groupConnection.getSourceUser().getUserActorRef()
                    .tell(new Command(Command.Type.Error, Command.From.GroupsConnection,
                            "Group " + groupName + " does not exists!"), self());
        }
    }

    private void deleteGroup(GroupCommand groupCommand) {
        String groupToDelete = groupCommand.getGroupName();
        if (this.GroupsMap.containsKey(groupToDelete)) {
            this.GroupsMap.remove(groupToDelete);
            ActorRef group = this.GroupsMap.get(groupCommand.getGroupName());
            group.tell(akka.actor.PoisonPill.getInstance(), ActorRef.noSender());
        }

    }


    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(CreateGroupCommand.class, predicates.GroupsConnectionCreateGroup, (msg) -> CreateGroup(msg, sender()))
                .match(String.class, this::printFromServer)
                .match(InviteGroup.class, predicates.GroupsConnectionInviteGroup,
                        (invitation) -> GroupInvite(invitation, sender()))
                .match(GroupTextMessage.class, this::sendToGroup)
                .match(GroupCommand.class, predicates.GroupConnection_Delete, this::deleteGroup)
                .match(GroupCommand.class, predicates.GroupConnection_GroupLeave, this::sendToGroup)
                .match(RemoveUserGroup.class, predicates.removeUserFromGroup, this::sendToGroup)
                .match(CoAdminCommand.class, predicates.PromoteCommand, this::sendToGroup)
                .match(GroupFileMessage.class, this::sendToGroup)
                .matchAny((cmd) -> printFromServer("MATCHANY:" + cmd.toString()))
                .build();
    }
}

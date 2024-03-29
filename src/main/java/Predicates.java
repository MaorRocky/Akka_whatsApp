import akka.japi.pf.FI;

public class Predicates {

    public FI.TypedPredicate<MuteGroup> unMuteUser;
    public FI.TypedPredicate<MuteGroup> MuteUser;
    public FI.TypedPredicate<ConnectCommand> connectCommandPred;
    public FI.TypedPredicate<DisConnectCommand> disconnectCmd;
    public FI.TypedPredicate<Command> ErrorCmd;
    public FI.TypedPredicate<TextMessage> sendTextToAnotherClient;
    public FI.TypedPredicate<TextMessage> receiveTextFromAnotherClient;
    public FI.TypedPredicate<FileMessage> sendFileToAnotherClient;
    public FI.TypedPredicate<FileMessage> receiveFileClient;
    public FI.TypedPredicate<CreateGroupCommand> createGroup;
    public FI.TypedPredicate<CreateGroupCommand> createGroup_respond;
    public FI.TypedPredicate<GroupCommand> GroupLeave;
    public FI.TypedPredicate<InviteGroup> InviteGroup;
    public FI.TypedPredicate<InviteGroup> InviteGroup_Error;
    public FI.TypedPredicate<InviteGroup> InviteGroup_Answer;
    public FI.TypedPredicate<InviteGroup> displayInvitation;
    public FI.TypedPredicate<Command> ReplyToInvitation;
    public FI.TypedPredicate<Command> displayAnswerAndWelcome;
    public FI.TypedPredicate<GroupTextMessage> groupTextMessage;
    public FI.TypedPredicate<GroupCommand> removeGroupFromUserActor;
    public FI.TypedPredicate<RemoveUserGroup> removeUserFromGroup;
    public FI.TypedPredicate<Command> RemoveGroupFromHashMap;
    public FI.TypedPredicate<GroupFileMessage> sendGroupFileMessage;
    public FI.TypedPredicate<GroupFileMessage> GroupFileMessage_recieve;
    public FI.TypedPredicate<CoAdminCommand> PromoteCommand;
    public FI.TypedPredicate<CoAdminCommand> PromoteCommand_reply;


    //Server predicate
    public FI.TypedPredicate<CreateGroupCommand> createGroupServer;
    public FI.TypedPredicate<InviteGroup> InviteGroupServer;
    public FI.TypedPredicate<GroupTextMessage> groupTextMessageServer;

    //    UsersConnection
    public FI.TypedPredicate<ConnectCommand> ConnectCommandUserConnection;
    public FI.TypedPredicate<DisConnectCommand> DisConnectCommandUsersConnection;
    public FI.TypedPredicate<TextMessage> TextMessageUsersConnection;
    public FI.TypedPredicate<FileMessage> sendFileToAnotherClient_usersConnection;


    //    GroupsConnection
    public FI.TypedPredicate<CreateGroupCommand> GroupsConnectionCreateGroup;
    public FI.TypedPredicate<InviteGroup> GroupsConnectionInviteGroup;
    public FI.TypedPredicate<GroupCommand> GroupConnection_Delete;
    public FI.TypedPredicate<GroupCommand> GroupConnection_GroupLeave;


    // Group
    public FI.TypedPredicate<MuteGroup> unMuteGroup;
    public FI.TypedPredicate<MuteGroup> MuteGroup;
    public FI.TypedPredicate<InviteGroup> GroupInviteGroup;
    public FI.TypedPredicate<InviteGroup> getReplyToInvitation;
    public FI.TypedPredicate<Command> GroupError;
    public FI.TypedPredicate<Command> GroupUserLeft;
    public FI.TypedPredicate<GroupCommand> Group_GroupLeave;
    public FI.TypedPredicate<GroupFileMessage> GroupFileMessage_Group;


    public Predicates() {
        //Client predicates
        unMuteUser = cmd -> cmd.getType().equals(Command.Type.User_unMute)&& cmd.getFrom().equals(Command.From.IO);
        MuteUser =  cmd -> cmd.getType().equals(Command.Type.User_Mute)&& cmd.getFrom().equals(Command.From.IO);
        connectCommandPred = cmd -> cmd.getType().equals(Command.Type.Connect);
        disconnectCmd = cmd -> cmd.getType().equals(Command.Type.Disconnect);
        sendTextToAnotherClient = cmd -> cmd.getFrom().equals(Command.From.IO)
                && (cmd.getType().equals(Command.Type.UserTextMessage));
        receiveTextFromAnotherClient = cmd -> cmd.getFrom().equals(Command.From.Client);
        sendFileToAnotherClient = cmd -> cmd.getType().equals(Command.Type.UserFileMessage)
                && cmd.getFrom().equals(Command.From.IO);
        receiveFileClient = cmd -> cmd.getType().equals(Command.Type.UserFileMessage)
                && cmd.getFrom().equals(Command.From.Client);
        createGroup = cmd -> cmd.getType().equals(Command.Type.Create_Group)
                && cmd.getFrom().equals(Command.From.IO);
        createGroup_respond = cmd -> cmd.getType().equals(Command.Type.Create_Group)
                && cmd.getFrom().equals(Command.From.GroupsConnection);
        InviteGroup = cmd -> cmd.getType().equals(Command.Type.Invite_Group)
                && cmd.getFrom().equals(Command.From.IO);
        InviteGroup_Error = cmd -> cmd.getType().equals(Command.Type.Error)
                && (!cmd.isSucceeded());
        InviteGroup_Answer = cmd -> cmd.getType().equals(Command.Type.Invite_Group)
                && (cmd.isSucceeded()) && cmd.GaveAnswer();
        displayInvitation = cmd -> cmd.getType().equals(Command.Type.Invite_Group)
                && (!cmd.GaveAnswer());
        ReplyToInvitation = cmd -> cmd.getType().equals(Command.Type.invitationAnswer)
                && cmd.getFrom().equals(Command.From.IO);

        displayAnswerAndWelcome = cmd -> cmd.getType().equals(Command.Type.invitationAnswer)
                || cmd.getType().equals(Command.Type.WelcomeMessage);

        groupTextMessage = cmd -> cmd.getType().equals(Command.Type.Group_Text)
                && cmd.getFrom().equals(Command.From.Group);
        removeGroupFromUserActor = cmd -> cmd.getType().equals(Command.Type.Group_Leave)
                && cmd.getFrom().equals(Command.From.Group);
        ErrorCmd = cmd -> cmd.getType().equals(Command.Type.Error);
        removeUserFromGroup = cmd -> cmd.getType().equals(Command.Type.Group_Remove);
        PromoteCommand_reply = cmd -> (cmd.getType().equals(Command.Type.Group_Promote)
                || cmd.getType().equals(Command.Type.Group_Demote))
                && cmd.getFrom().equals(Command.From.Group);

        GroupLeave = cmd -> cmd.getType().equals(Command.Type.Group_Leave)
                && cmd.getFrom().equals(Command.From.IO);
        sendGroupFileMessage = cmd -> cmd.getType().equals(Command.Type.Group_File)
                && cmd.getFrom().equals(Command.From.IO);
        GroupFileMessage_recieve = cmd -> cmd.getType().equals(Command.Type.Group_File)
                && cmd.getFrom().equals(Command.From.Group);

        //Server predicate
        createGroupServer = cmd -> cmd.getType().equals(Command.Type.Create_Group);
        InviteGroupServer = cmd -> cmd.getType().equals(Command.Type.Invite_Group);
        groupTextMessageServer = cmd -> cmd.getType().equals(Command.Type.Group_Text)
                && cmd.getFrom().equals(Command.From.IO);
        RemoveGroupFromHashMap = cmd -> cmd.getType().equals(Command.Type.USER_DELETE_THIS_GROUP)
                && cmd.getFrom().equals(Command.From.Group);
        PromoteCommand = cmd -> (cmd.getType().equals(Command.Type.Group_Promote)
                || cmd.getType().equals(Command.Type.Group_Demote))
                && !cmd.isSucceeded();

        // UsersConnection
        ConnectCommandUserConnection = cmd -> cmd.getType().equals(Command.Type.Connect)
                && cmd.getFrom().equals(Command.From.Server);
        DisConnectCommandUsersConnection = cmd -> cmd.getType().equals(Command.Type.Disconnect)
                && cmd.getFrom().equals(Command.From.Server);
        TextMessageUsersConnection = cmd -> cmd.getType().equals(Command.Type.UserTextMessage)
                && cmd.getFrom().equals(Command.From.Server);
        sendFileToAnotherClient_usersConnection = cmd -> cmd.getType().equals(Command.Type.UserFileMessage)
                && cmd.getFrom().equals(Command.From.Server);

        //GroupsConnectionCreateGroup
        GroupsConnectionCreateGroup = cmd -> cmd.getType().equals(Command.Type.Create_Group)
                && cmd.getFrom().equals(Command.From.Server);
        GroupsConnectionInviteGroup = cmd -> cmd.getType().equals(Command.Type.Invite_Group)
                && cmd.getFrom().equals(Command.From.Server);
        GroupConnection_Delete = cmd -> cmd.getType().equals(Command.Type.Delete_Group)
                && cmd.getFrom().equals(Command.From.Group);
        GroupConnection_GroupLeave = cmd -> cmd.getType().equals(Command.Type.Group_Leave)
                && cmd.getFrom().equals(Command.From.Server);
        //Group
        GroupInviteGroup = cmd -> cmd.getType().equals(Command.Type.Invite_Group)
                && cmd.getFrom().equals(Command.From.GroupsConnection);
        getReplyToInvitation = cmd -> cmd.getType().equals(Command.Type.Invite_Group)
                && cmd.GaveAnswer();
        GroupError = cmd -> cmd.getType().equals(Command.Type.Error)
                && cmd.getFrom().equals(Command.From.Group);
        GroupUserLeft = cmd -> cmd.getType().equals(Command.Type.Group_Leave)
                && cmd.getFrom().equals(Command.From.Group);
        Group_GroupLeave = cmd -> cmd.getType().equals(Command.Type.Group_Leave)
                && cmd.getFrom().equals(Command.From.GroupsConnection);
        GroupFileMessage_Group = cmd -> cmd.getType().equals(Command.Type.Group_File)
                && cmd.getFrom().equals(Command.From.GroupsConnection);
        MuteGroup = cmd -> cmd.getType().equals(Command.Type.User_Mute)&& cmd.getFrom().equals(Command.From.Client);
        unMuteGroup = cmd -> cmd.getType().equals(Command.Type.User_unMute)&& cmd.getFrom().equals(Command.From.Client);
    }
}
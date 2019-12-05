import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.concurrent.TimeUnit.SECONDS;


public class UserActor extends AbstractActor {

    private User myUser;
    private ActorSelection serverRef;
    private ActorRef ParserHandler;
    private Timeout askTimeout;
    private Predicates predicates;


    public UserActor() {
        myUser = new User(getSelf());
        askTimeout = new Timeout(Duration.create(1, SECONDS));
        predicates = new Predicates();
    }

    public void preStart() {
        serverRef = getContext().actorSelection(
                "akka.tcp://ServerSystem@127.0.0.1:3553/user/Server");
    }

    //set setParserHandler ActorRef at user connect command
    private void setParserHandler(ActorRef sender) {
        if (ParserHandler == null)
            ParserHandler = sender;
    }

    //sets myUser userName and boolean connected
    private void connectUserName(String name) {
        myUser.setUserName(name);
        myUser.connect();
    }

    //set From command field to Client and sends the command to the wanted ActorRef
    private void sendCommand(Command command, ActorRef actorRef) {
        command.setFrom(Command.From.Client);
        actorRef.tell(command, self());
    }


    //send command to the ParserActor to print the result
    private void print(Command.Type type, String str) {
        if (!str.equals(""))
            sendCommand(new Command(type, Command.From.Client, str), this.ParserHandler);
    }

    private void printNotConnected() {
        print(Command.Type.Error, "you are not connected to the system");
    }

    //return the current time String
    private String getTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(new Date());
    }


    //asks the server to connect new user if not connected already
    //if not succeeded then printing the false command returned
    //from the server
    private void connectUser(ConnectCommand command) {
        if (!myUser.isConnected()) {
            command.setUserRef(myUser.getUserActorRef());
            Command result = askServer(command);
            if (result.isSucceed()) {
                connectUserName(command.getUser().getUserName());
            }
            print(command.getType(), result.getResult());
        } else
            print(Command.Type.Error, "You're already connected");
    }

    //asks the server to disconnect user, if succeeded set myUser userName to null
    //and boolean connected to false
    //if not succeeded then printing the false command returned
    //from the server
    private void disconnectUser(DisConnectCommand command) {
        if (myUser.isConnected()) {
            command.setUser(myUser);
            Command result = askServer(command);
            if (result.isSucceed()) {
                myUser.setUserName(null);
                myUser.disconnect();
                print(Command.Type.Disconnect, result.getResult());
            } else
                print(Command.Type.Error, "server is offline! try again later!");
        } else
            printNotConnected();
    }

    //asks the server for command target ActorRef
    //if succeeded, send the result command which contains
    //the relevant message to the target user
    //else printing the false command returned from the server


    //asks the server actor the wanted command
    //if the server sends back result before timeout
    //returns the result
    //else returns false command with "server is offline!" result
    private Command askServer(Command command) {
        command.setFrom(Command.From.Client);
        Future<Object> future = Patterns.ask(serverRef, command, askTimeout);
        try {
            return (Command) Await.result(future, askTimeout.duration());
        } catch (Exception e) {
            command.setResult(false, "server is offline!");
            return command;
        }
    }

    private void sendToClient(Command command) {
        if (myUser.isConnected()) {
            Command result = askServer(command);
            if (result.isSucceed()) {
                sendCommand(result, result.getUserResult().getUserActorRef());
            } else
                print(result.getType(), result.getResult());
        } else
            printNotConnected();
    }


    //Printing the sent text message from another user
    private void createTextMessageToPrint(TextMessage TextMessage) {
        String message = "[" + getTime() + "][" + TextMessage.getTargetUser().getUserName() + "][" +
                TextMessage.getSourceUser().getUserName() + "] " + TextMessage.getMessage();
        print(Command.Type.User_Text, message);
    }

    //returns String message of file received from other user
    private String userFileMessage(FileMessage fileMessage) {
        return "[" + getTime() + "][" + myUser.getUserName() + "][" +
                fileMessage.getSourceUser().getUserName() + "] File received: " + fileMessage.getTargetFilePath();
    }

    private void downloadFile(FileMessage fileMessage) {
        Path path = Paths.get(fileMessage.getTargetFilePath());
        try {
            Files.write(path, fileMessage.getFile());
            String message = "";
            if (fileMessage.getType().equals(Command.Type.User_File))
                message = userFileMessage(fileMessage);
            print(Command.Type.User_File, message);
        } catch (Exception e) {
            print(Command.Type.Error, "Failed to download the sent file");
        }
    }

    /*sendToClient will be used when a user sends a message to another client*/
    /*createTextMessageToPrint will be used when a user will receive a message from another client*/
    public Receive createReceive() {

        return receiveBuilder()
                .match(ConnectCommand.class, predicates.connectCommandPred, (command) -> {
                    setParserHandler(getSender());
                    connectUser(command);
                })
                .match(DisConnectCommand.class, predicates.disconnectCmd, this::disconnectUser)
                .match(TextMessage.class, predicates.sendTextToAnotherClient, this::sendToClient)
                .match(TextMessage.class, predicates.receiveTextFromAnotherClient, this::createTextMessageToPrint)
                .match(FileMessage.class, predicates.sendFileToAnotherClient, this::sendToClient)
                .match(FileMessage.class, predicates.receiveFileClient, this::downloadFile)
                .matchAny(System.out::println)
                .build();
    }
}
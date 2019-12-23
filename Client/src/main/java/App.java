import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Scanner;

public class App {
    /*TODO the quit does not work*/
    public static void main(String[] args) {
        String input;
        Scanner scanner = new Scanner(System.in);
        ActorSystem system = ActorSystem.create("ClientWhatsApp");
        ActorRef UserHandler = system.actorOf(Props.create(UserActor.class), "UserHandler");
        ActorRef Parser = system.actorOf(Props.create(IOParserActor.class, UserHandler), "Parser");
        System.out.println("Enter \"/user connect <username>\" to connect to the server");
        System.out.println("Enter \"quit\" to finish the session");

        while (!(input = scanner.nextLine()).equalsIgnoreCase("quit")) {
            Parser.tell(input, Parser);
        }
        scanner.close();
        System.exit(0);
    }

}

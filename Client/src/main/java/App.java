import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Scanner;

public class App {
    /*TODO the quit does not work*/
    public static void main(String[] args) {
        boolean toQuit = false;
        String input;
        Scanner scanner = new Scanner(System.in);

        ActorSystem system = ActorSystem.create("ClientWhatsApp");

        ActorRef UserHandler = system.actorOf(Props.create(UserActor.class), "UserHandler");
        ActorRef Parser = system.actorOf(Props.create(ParserActor.class, UserHandler), "Parser");


        System.out.println("Enter \"/user connect <username>\" to connect to the server");

        while (!toQuit) {

            input = scanner.nextLine();
            if (input.equalsIgnoreCase("quit")) {
                toQuit = true;
                scanner.close();

            } else {
                Parser.tell(input, Parser);
            }
        }

    }

}

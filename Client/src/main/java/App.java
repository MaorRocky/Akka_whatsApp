import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Scanner;

public class App {

    public static void main(String args[]) {
        boolean toQuit = false;
        String input;
        Scanner scan = new Scanner(System.in);

        ActorSystem system = ActorSystem.create("ClientWhatsApp");

        ActorRef UserHandler = system.actorOf(Props.create(UserActor.class), "UserHandler");
        ActorRef Parser = system.actorOf(Props.create(ParserActor.class, UserHandler), "Parser");


        System.out.println("Enter \"/user connect <username>\" to connect to the server");

        while (!toQuit) {

            input = scan.nextLine();
            if (input.equalsIgnoreCase("quit")) {
                toQuit = true;

            } else {
                Parser.tell(input, Parser);
            }
        }

    }

}

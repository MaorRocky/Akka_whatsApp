import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.HashMap;

public class GroupsConnection extends AbstractActor {

    protected HashMap<String, Group> GroupsMap;
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

    @Override
    public Receive createReceive() {
        return null;
    }
}

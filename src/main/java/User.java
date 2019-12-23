import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class User implements Serializable {

    private String userName;
    private ActorRef userActorRef;
    private boolean connected;
    private HashMap<String, ActorRef> usersGroups;


    public User(String name) {
        this.userName = name;
    }

    public User(ActorRef actorRef) {
        this.userActorRef = actorRef;
        this.connected = false;
        usersGroups = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public ActorRef getUserActorRef() {
        return userActorRef;
    }

    public void setUserActorRef(ActorRef actorRef) {
        this.userActorRef = actorRef;
    }

    public void connect() {
        this.connected = true;
    }

    public void disconnect() {
        this.connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public HashMap<String, ActorRef> getUsersGroups() {
        return usersGroups;
    }

    public void addGroupToUsersGroups(String groupName, ActorRef group) {
        this.usersGroups.put(groupName, group);
    }

    public ActorRef getGroupManager(String groupName){
        return usersGroups.getOrDefault(groupName, null);
    }

}

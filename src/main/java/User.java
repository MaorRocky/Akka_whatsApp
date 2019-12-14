import akka.actor.ActorRef;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class User implements Serializable {

    private String userName;
    private ActorRef userActorRef;
    private boolean connected;
    private HashSet<ActorRef> usersGroups;
//    private Invitation invitation;
//    private Group.PType beforeMute;

    public User(String name) {
        this.userName = name;
    }

    public User(ActorRef actorRef) {
        this.userActorRef = actorRef;
        this.connected = false;
        usersGroups = new HashSet<>();
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

    public HashSet<ActorRef> getUsersGroups() {
        return usersGroups;
    }

    public void addGroupToUsersGroups(ActorRef group) {
        this.usersGroups.add(group);
    }


    /*
    public void setInvitation(Invitation inv){
        this.invitation = inv;
    }

    public Invitation getInvitation(){
        return invitation;
    }

    public void setBeforeMute(Group.PType type){
        this.beforeMute = type;
    }

    public Group.PType getBeforeMute(){
        return this.beforeMute;
    }*/
}

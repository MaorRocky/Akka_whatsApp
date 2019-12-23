Maor Rocky 203246079
Shvat Messica 208986752

In our project there at least 5 actors, and depending on the user input, more actors will be added.
for each new user a new actor is created, the same goes for each group.

The server actor has 2 childes:
a. is an actor which manages all of the users actors, hence his name usersConnection.
b. is an actor which manages all of the groups actors, hence his name groupsConnection,
    i.e. he manages all the group mangers(manger = actor). 


1. IOParserActor:
    a. The actor receives incoming user input from the keyboard, create and parse the relevant
        message and send them to the UserActor.
        Each user has a IOParserActor, and each IOParserActor is linked to a specific UserActor. 
        
    b. The actor receives Commands from the UserActor, those commands will be printed to screen.

2. UserActor:
    a. The UserActor will send the commands he received from the IOParserActor to the relevant 
        target. Sometimes commands will be sent to the server and than back to the UserActor with 
        some kind of response, either an Error or a "success" result, and sometimes they will be 
        sent to the groups-actors directly, using the User class which each UserActor has. 

3. ServerActor:
    As we explained above the ServerActor main job is to direct incoming commands to his relevant
    child, either the UsersConnection actor, or to the GroupsConnection actor.
4. UsersConnection:
    This actor uses a hashmap<String,User> which holds all the connected users in the system.
    This actor manages all the information about the connected users.
    It will receive and handle commands which regards only to get results to requests such as
    to get a user ActorRef, or to check whether or not a user exists.
5. GroupsConnection:
   This actor handles the creation and deletion of groups. In addition GroupsConnection redirect
   commands to the specific group actor.
   
   
    
Main Classes:
    1. User:
       This class represents a user, it holds all the information which is necessary for each user,
       such as "name", groups which the users belong to, his ActorRef and so on.
    2. Group extends AbstractActor :
       This class is an Actor which represents a group, it holds all the information which is necessary for each group,
       such as "groupName",group users, muted users, co-admin user and so on.         
    3. Command:
       This class is base class for all out different commands, each relative will extend this class,
       for the purpose of having specific fields for convenience.  
    4. Predicates:
       This class holds relevant predicates to check during out pattern matching.  


How to run:
first, run the server under "server" class.
second, run the "App", and follow instructions in the terminal







package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.User;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BGSProtocol implements BidiMessagingProtocol<List<Object>> {

    private boolean shouldTerminate = false;
    private DataBase dataBase = DataBase.getInstance();
    private ConnectionsImpl connections;
    private int connectionId;


    public BGSProtocol(ConnectionsImpl connections) {
        this.connections = connections;
    }

    public void start(int connectionId, Connections connections){
        this.connectionId = connectionId;
    }

    public void process(List<Object> message) {
        Iterator<Object> iter = message.iterator();
        short opcode = (short)iter.next();

        boolean connectionIdExist;

        switch (opcode) {
            case 1: //Register
                String[] userData = new String[3];
                int i = 0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        userData[i++] = (String) o;
                    }
                }
                String regUsername = userData[0]; String regPassword = userData[1]; String regDate = userData[2];
                Boolean ans1 = dataBase.registerUser(regUsername, regPassword, regDate);
                if(ans1){
                    connections.send(connectionId, ackFirstResponse(1));
                    //If successful an ACK message will be sent in return
                }else {
                    connections.send(connectionId,errorResponse());
                    // If the username is already registerd in
                    //the server, an ERROR message is returned
                }
                break;
            case 2: //Login
                String[] userLoginData = new String[2];
                i = 0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        userLoginData[i++] = (String) o;
                    }
                }
                String loginUsername = userLoginData[0];
                String loginPassword = userLoginData[1];
                boolean captcha = (short) message.get(message.size() - 1) == 1;
                User newUser = dataBase.verifyUserDetails(loginUsername, loginPassword, captcha);
                if(newUser != null){
                    newUser.logIn();
                    connections.addUserForConnection(newUser, connectionId);
                    connections.send(connectionId, ackFirstResponse(2));
                    //If successful an ACK message will be sent in return
                } else {
                    connections.send(connectionId,errorResponse());
                   //If the user doesn’t exist or the password
                   //doesn’t match the one entered for the username, sends an ERROR message.
                   //An ERROR message should also appear if the current client has already succesfully logged in.
                   // An ERROR message should appear also if the captcha byte is 0.
                }
                break;
            case 3: //Logout
                if(connections.checkIfUserLogIn(connectionId)){
                    connections.send(connectionId, ackFirstResponse(3));
                    User user = (User) connections.getConnectionsByUser().get(connectionId);
                    user.logOut();
                    connections.getConnectionsByUser().remove(connectionId);
                    ConnectionHandler handler = (ConnectionHandler) connections.getConnectionsById().get(connectionId);
                    try {
                        handler.close();
                    } catch (IOException exception){
                        System.out.println(exception);
                    }
                    shouldTerminate = true;
                    connections.getConnectionsById().remove(connectionId);
                    //Client may terminate only after reciving ACK message in replay.
                } else {
                    connections.send(connectionId,errorResponse());
                    // If no user is logged in, sends an ERROR message.
                }
                //TODO: Check the logic
                break;
            case 4: // Follow(0)/Unfollow(1)
                boolean follow = (short)iter.next() == 0;
                String userNameToFollow = (String) iter.next();
                User userToFollow = dataBase.getUserByName(userNameToFollow);
                if(connections.checkIfUserLogIn(connectionId) && userToFollow != null){
                    User currentUser = connections.getUserByConnectionId(connectionId);
                    if (follow){
                        if (!dataBase.checkIfUserFollowerOfOtherUser(currentUser, userToFollow)){
                            dataBase.follow(currentUser, userToFollow);
                            connections.send(connectionId,ackFollowResponse(userNameToFollow));
                        }else {
                            connections.send(connectionId,errorResponse());
                            //For a follow command to succeed, a user on the list must not already
                            //be on the following list of the logged in user.
                        }
                    }else {
                        if (dataBase.checkIfUserFollowerOfOtherUser(currentUser, userToFollow)) {
                            dataBase.unfollow(currentUser, userToFollow);
                            connections.send(connectionId,ackFollowResponse(userNameToFollow));
                        }else {
                            connections.send(connectionId,errorResponse());
                        }
                    }
                }else {
                    connections.send(connectionId,errorResponse());
                    //The user must be logged in, otherwise an ERROR message will be sent.
                }
                break;
            case 5: //Post
                //
                String content = (String) iter.next();
                User currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId)){

                }else {
                    connections.send(connectionId,errorResponse());
                    //The user must be logged in, otherwise an ERROR message will be sent.
                }


                break;
            case 6: //PM
                String[] PMData = new String[3];
                i = 0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        PMData[i++] = (String) o;
                    }
                    String PMUsername = PMData[0];
                    String PMContent = PMData[1];
                    String PMDateTime = PMData[2];  //format DD-MM-YYYY HH:MM
                    LocalDateTime dateTime = convertToDateTime(PMDateTime);

                }

                break;
            case 7: //LOGSTAT
                //TODO: Check the logic
                if(connections.checkIfUserLogIn(connectionId)){
                    for (User user : dataBase.getRegisterdUserList()){
                        if(user.isLogged()){
                            int userId = connections.getConnectionIdByUser(user);
                            connections.send(userId, ackStatAndLogStat(7, user));
                        }
                    }
                }else {
                    connections.send(connectionId,errorResponse());
                    //The user must be logged in, otherwise an ERROR message will be sent.
                }
                break;
            case 8: //STAT
                String usersString = (String)iter.next();
                String [] users = usersString.split("\\|");
                break;


        }
    }

    public boolean shouldTerminate(){
        return shouldTerminate;
    }


    private LocalDateTime convertToDateTime(String dateTime){
        String[] s = dateTime.split(" ");
        String[] date = s[0].split("-");
        int days = Integer.parseInt(date[0]);
        int months = Integer.parseInt(date[1]);
        int years = Integer.parseInt(date[2]);
        String[] time = s[0].split(":");
        int hours = Integer.parseInt(time[0]);
        int minuets = Integer.parseInt(time[1]);
        return LocalDateTime.of(years,months,days,hours,minuets);
    }
    
    private List<Object> ackFirstResponse(int op){
        List<Object> output = new ArrayList<>();
        output.add(((short)10));
        output.add(((short)op));
        return output;
    }

    private List<Object> ackFollowResponse(String UserName){
        List<Object> output = new ArrayList<>();
        output.add(((short)10));
        output.add(((short)4));
        output.add(UserName);
        output.add('\0');
        return output;
    }

    private List<Object> ackStatAndLogStat(int op, User user){
        List<Object> output = new ArrayList<>();
        output.add(((short)10));
        output.add(((short)op));
        output.add(dataBase.age(user)); //<Age>
        output.add(dataBase.numPosts(user)); //<NumPosts>
        output.add(dataBase.numFollowers(user)); //<NumFollowers>
        output.add(dataBase.numFollowing(user)); //<NumFollowing>
        return output;
    }

    private List<Object> errorResponse(){
        List<Object> output = new ArrayList<>();
        output.add(((short)11));
        return output;
    }


}



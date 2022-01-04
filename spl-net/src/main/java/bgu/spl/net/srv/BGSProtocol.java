package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

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
                    connections.send(connectionId, ackFirstResponse(opcode));
                }else {
                    connections.send(connectionId,errorResponse(opcode));
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
                    connections.send(connectionId, ackFirstResponse(opcode));
                } else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 3: //Logout
                if(connections.checkIfUserLogIn(connectionId)){
                    connections.send(connectionId, ackFirstResponse(opcode));
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
                } else {
                    connections.send(connectionId,errorResponse(opcode));
                }
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
                            connections.send(connectionId,errorResponse(opcode));
                        }
                    }else {
                        if (dataBase.checkIfUserFollowerOfOtherUser(currentUser, userToFollow)) {
                            dataBase.unfollow(currentUser, userToFollow);
                            connections.send(connectionId,ackFollowResponse(userNameToFollow));
                        }else {
                            connections.send(connectionId,errorResponse(opcode));
                        }
                    }
                }else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 5: //Post
                String content = (String) iter.next();
                User currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId)){
                    List<User> taggedUsers = getTaggedUsers(content);
                    dataBase.removeBlockedUsers(currentUser,taggedUsers);
                    List<User> followingUsers =dataBase.getFollowers(currentUser);
                    List<User> postUsers = new LinkedList<>(followingUsers);
                    postUsers.addAll(taggedUsers);
                    List<User> notLoggedUsers = new LinkedList<>();
                    for (User user : postUsers){
                        dataBase.addPost(currentUser, new Post(content));
                        if (user.isLogged()) {
                            int id = connections.getConnectionIdByUser(user);
                            connections.send(id, ackPostResponse(currentUser.getUsername(), content));
                        } else {
                            notLoggedUsers.add(user);
                            dataBase.addMessageToPendingNotifications(notLoggedUsers, new Post(content));
                        }
                    }
                }else {
                    connections.send(connectionId,errorResponse(opcode));
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
                    User sendTo = dataBase.getUserByName(PMUsername);
                    String filteredMessage = dataBase.filter(PMContent);
                    LocalDateTime dateTime = convertToDateTime(PMDateTime); ////TODO: ask omer for whet?
                    currentUser = connections.getUserByConnectionId(connectionId);
                    if(connections.checkIfUserLogIn(connectionId) && sendTo!=null && dataBase.canCommunicate(currentUser,sendTo)){
                            List<Object> pm = Arrays.asList(new Object[]{(short)9, (short)0, currentUser.getUsername(),(byte)0,filteredMessage,(byte) 0});
                            int id = connections.getConnectionIdByUser(sendTo);
                            connections.send(id, pm);
                        }
                    else {
                        connections.send(connectionId,errorResponse(opcode));
                    }

                }
                break;
            case 7: //LOGSTAT
                //TODO: Check if block
                currentUser = connections.getUserByConnectionId(connectionId);
                if(connections.checkIfUserLogIn(connectionId)){
                    for (User user : dataBase.getRegisterdUserList()){
                        if(user.isLogged() && dataBase.canCommunicate(currentUser,user)) {
                            connections.send(connectionId, ackStatAndLogStat(7, user));
                        }
                    }
                }else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;
            case 8: //STAT
                String usersString = (String)iter.next();
                String [] users = usersString.split("\\|");
                //TODO: Check if block
                currentUser = connections.getUserByConnectionId(connectionId); ////TODO: ask omer for whet?
                if(connections.checkIfUserLogIn(connectionId)) {
                    boolean allUsersExisting = true;
                    for (int j = 0; j < users.length && allUsersExisting; j++) {
                        User user = dataBase.getUserByName(users[j]);
                        if (user == null) {
                            allUsersExisting = false;
                        }
                    }
                    if (allUsersExisting) {
                        for (String userName : users) {
                            User user = dataBase.getUserByName(userName);
                            connections.send(connectionId, ackStatAndLogStat(8, user));
                        }
                    } else {
                        connections.send(connectionId, errorResponse(opcode));
                    }
                }
                else {
                    connections.send(connectionId,errorResponse(opcode));
                }
                break;

            case 12: //BLOCK
                String userToBlock_s = (String)iter.next();
                currentUser = connections.getUserByConnectionId(connectionId);
                User userToBlock = dataBase.getUserByName(userToBlock_s);
                if(connections.checkIfUserLogIn(connectionId) && userToBlock != null){
                    dataBase.block(currentUser,userToBlock);

                }else {
                    connections.send(connectionId, errorResponse(opcode));
                }
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
        output.add(user.getAge()); //<Age>
        output.add(dataBase.getNumPosts(user)); //<NumPosts>
        output.add(dataBase.getNumFollowersForUser(user)); //<NumFollowers>
        output.add(dataBase.getFollowingNumForUser(user)); //<NumFollowing>
        return output;
    }

    private List<Object> ackPostResponse(String username, String content){
        List<Object> output = new ArrayList<>();
        output.add(((short)9));
        output.add(((short)1));
        output.add(username);
        output.add((byte)0);
        output.add(content);
        output.add((byte)0);
        return output;
    }


    private List<Object> errorResponse(short messageOpcode){
        List<Object> output = new ArrayList<>();
        output.add(((short)11));
        output.add(messageOpcode);
        return output;
    }

    List<User> getTaggedUsers (String postContent){
        List<User> taggedList = new LinkedList<>();
        String[] words = postContent.split(" ");
        for (String str: words) {
            if (str.charAt(0) == '@') {
                String username = str.substring(1);
                User user = dataBase.getUserByName(username);
                if (user != null ) {
                    taggedList.add(user);
                }
            }
        }
        return taggedList;
    }


}


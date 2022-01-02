package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;
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
                    connections.send(connectionId,ackResponse(1));
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
                    connections.send(connectionId,ackResponse(2));
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
                if(connections.getConnectionsByUser().containsKey(connectionId)){
                    connections.send(connectionId,ackResponse(3));
                    User user = (User) connections.getConnectionsByUser().get(connectionId);
                    user.logOut();
                    connections.getConnectionsByUser().remove(connectionId);
                    BlockingConnectionHandler handler = (BlockingConnectionHandler) connections.getConnectionsById().get(connectionId);
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
                    // If no user is logged in, sends an ERROR message. ->
                    // Check for a key in connectionsByUser
                }
                //TODO: Check the logic
                break;
            case 4: //Follow
                boolean follow = (short)iter.next() == 0;
                String userToFollow = (String) iter.next();
    //            dataBase.follow();

                break;
            case 5: //Post
                String content = (String) iter.next();

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
                //TODO: COMPLETE THIS METHOD
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

    private List<Object> ackResponse(int op){
        List<Object> output = new ArrayList<>();
        output.add(((short)10));
        output.add(((short)op));
        return output;
    }

    private List<Object> errorResponse(){
        List<Object> output = new ArrayList<>();
        output.add(((short)11));
        return output;
    }


}



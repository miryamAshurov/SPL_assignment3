package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;


import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

public class BGSProtocol implements BidiMessagingProtocol<List<Object>> {

    private boolean shouldTerminate = false;



    public void start(int connectionId, Connections connections){

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

    public void process(List<Object> message) {

        Iterator<Object> iter = message.iterator();
        short opcode = (short)iter.next();



        switch (opcode) {
            case 1: //Register
                String[] userData = new String[3];
                int i =0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        userData[i++]=(String) o;
                    }
                }
                String regUsername=userData[0]; String regPassword = userData[1]; String regDate = userData[2];


                break;
            case 2: //Login
                String[] userLoginData = new String[2];
                i=0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        userLoginData[i++]=(String) o;
                    }
                }
                String loginUsername = userLoginData[0];
                String loginPassword = userLoginData[1];
                boolean captcha = (short)message.get(message.size()-1)==0;

                break;
            case 3: //Logout
                shouldTerminate=true;
                //TODO: COMPLETE THIS METHOD

                break;
            case 4: //Follow
                boolean follow = (short)iter.next() == 0;
                String userToFollow = (String) iter.next();
                break;
            case 5: //Post
                String content = (String) iter.next();

                break;
            case 6: //PM
                String[] PMData = new String[3];
                i =0;
                while (iter.hasNext()){
                    Object o = iter.next();
                    if (o instanceof String){
                        PMData[i++]=(String) o;
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
}

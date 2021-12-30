package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataBase {

    private List<User> registerdUserList = new ArrayList<>();
    private ConcurrentHashMap<User, List<User>> followersList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<Post>> userPostList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<PM>> userPMList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<Notification>> userNotificationList  = new ConcurrentHashMap<>();
    private static DataBase instance = null;


    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    /*REGISTER Messages*/
    public boolean registerUser (String username, String password, String date){
        if (checkIfUsernameAlreadyExists(username)){
            return false;
            // If the username is already registerd in
            //the server, an ERROR message is returned
        }else {
            User user = new User(username, password, date);
            registerdUserList.add(user);
            return true;
            //If successful an ACK message will be sent in return
        }
    }

    private boolean checkIfUsernameAlreadyExists(String username){
        for (User user : registerdUserList) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private User checkIfUserRegisterd(String username, String password){
        for (User user : registerdUserList){
            if(user.getUsername().equals(username)){
                if(user.getPassword().equals(password)){
                    return user;
                }
            }
        }
        return null;
    }

    /*LOGIN Messages*/
    public boolean verifyUserDetails(String username, String password , boolean captcha){
        if(captcha) {
            User user = checkIfUserRegisterd(username, password);
            if (user != null) {
                if (!user.isLogged()) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<User> getRegisterdUserList() {
        return registerdUserList;
    }


    /*LOGOUT Messages*/



    

    /*FOLLOW Messages*/
    public void follow(User user, User follower){
        if(!this.followersList.containsKey(user)){
            this.followersList.put(user,new ArrayList<>());
        }
        this.followersList.get(user).add(follower);
    }

    public void unfollow(User user, User follower){
        if(this.followersList.containsKey(user)){
            this.followersList.get(user).remove(follower);
        }
    }

    public int getNumFollowersForUser(User user){
        return followersList.get(user).size();
    }

    public int getFollowingNumForUser(User user){
        int output = 0;
        followersList.forEach((k, v) -> {
            for (User user1 : v) {
                if(user.equals(user1)){

                }

            }

            System.out.println((k + ":" + v));
        });
        return output;
    }

    public List<User> getFollowersListForUser(User user) {
        return followersList.get(user);
    }

    public void addPost(User user, Post post){
        if (!this.userPostList.containsKey(user)){
            this.userPostList.put(user,new ArrayList<>());
        }
        this.userPostList.get(user).add(post);
    }

    public int getUserNumPostThatHePost(User user){
        return userPostList.get(user).size();
    }

    public List<Post> getUserPostListForThatHePost(User user) {
        return userPostList.get(user);
    }

    public List<Post> getUserPostListForThatHeReceiv(User user) {
        return null;
    }

    public void addPM(User user, PM pm){
        if (!this.userPMList.containsKey(user)){
            this.userPMList.put(user,new ArrayList<>());
        }
        this.userPMList.get(user).add(pm);
    }

    public List<PM> getUserPMListThatHeSend(User user) {
        return userPMList.get(user);
    }

    public List<PM> getUserPMListThatHeReceiv(User user) {
        return null;
    }


    public void addNotification(User user, Notification notification){
        if (!this.userNotificationList.containsKey(user)){
            this.userNotificationList.put(user,new ArrayList<>());
        }
        this.userNotificationList.get(user).add(notification);
    }

    public List<Notification> getUserNotification(User user) {
        return userNotificationList.get(user);
    }




}

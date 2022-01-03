package bgu.spl.net.srv.bidi;

import bgu.spl.net.srv.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataBase {

    private List<User> registerdUserList = new ArrayList<>();
    private ConcurrentHashMap<User, List<User>> followersList = new ConcurrentHashMap<>();

    private ConcurrentHashMap<User, List<Post>> userPostList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<PM>> userPMList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<Message>> userMessageList  = new ConcurrentHashMap<>();
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
        }else {
            User user = new User(username, password, date);
            registerdUserList.add(user);
            return true;
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
    public User verifyUserDetails(String username, String password , boolean captcha){
        if(captcha) {
            User user = checkIfUserRegisterd(username, password);
            if (user != null) {
                if (!user.isLogged()) {
                    return user;
                }
            }
        }
        return null;
    }

    /*LOGOUT Messages*/



    

    /*FOLLOW Messages*/
    public boolean checkIfUserFollowerOfOtherUser(User user, User other){
        if(this.followersList.containsKey(other)){
            if (this.followersList.get(other).contains(user)){
                return true;
            }
        }
        return false;
    }

    public void follow(User user, User userToFollow){
        if(!this.followersList.containsKey(userToFollow)){
            this.followersList.put(userToFollow, new ArrayList<>());
        }
        this.followersList.get(userToFollow).add(user);
    }

    public void unfollow(User user, User userToUnfollow){
        if(this.followersList.containsKey(userToUnfollow)){
            this.followersList.get(userToUnfollow).remove(user);
        }
    }

    /*LOGSTAT Messages*/
    public List<User> getRegisterdUserList() {
        return registerdUserList;
    }

    public short age(User user){
        return (short) user.getAge();
    }

    public short numPosts(User user){
        short numPosts = 0;
        if (userPostList.containsKey(user)) {
            numPosts = (short) userPostList.get(user).size();
        }
        return numPosts;
    }

    public short numFollowers(User user){
        short numFollowers = 0;
        if (followersList.containsKey(user)) {
            numFollowers = (short) getNumFollowersForUser(user);
        }
        return numFollowers;
    }

    public int getNumFollowersForUser(User user){
        return followersList.get(user).size();
    }

    public short numFollowing(User user){
        return (short) getFollowingNumForUser(user);
    }

    public int getFollowingNumForUser(User user){
        final int[] output = {0};
        followersList.forEach((k, v) -> {
            for (User user1 : v) {
                if(user.equals(user1)){
                    output[0] = output[0] + 1;
                }
            }
        });
        return output[0];
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


//    public void addNotification(User user, Notification notification){
//        if (!this.userNotificationList.containsKey(user)){
//            this.userNotificationList.put(user,new ArrayList<>());
//        }
//        this.userNotificationList.get(user).add(notification);
//    }

//    public List<Notification> getUserNotification(User user) {
//        return userNotificationList.get(user);
//    }

    public User getUserByName(String name) {
        for (User user : registerdUserList) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        return null;
    }


}

package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DataBase {

    private List<User> registerdUserList = new ArrayList<>();
    private ConcurrentHashMap<User, List<User>> blockingUsers = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<User>> followersList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<Post>> userPostList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<PM>> userPMList  = new ConcurrentHashMap<>();
    private ConcurrentHashMap<User, List<Message>> pendingNotifications  = new ConcurrentHashMap<>();
    private final List<String>  filteredWords = Arrays.asList(new String[]{"fuck, fucking, bitch, cunt, ass, asshole"});
    private static DataBase instance = null;


    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    /*General*/
    public User getUserByName(String name) {
        for (User user : registerdUserList) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        return null;
    }

    /*REGISTER*/
    public boolean registerUser (String username, String password, String date){
        if (checkIfUsernameAlreadyExists(username)){
            return false;
        }else {
            User user = new User(username, password, date);
            registerdUserList.add(user);
            blockingUsers.put(user,new LinkedList<>());
            followersList.put(user,new LinkedList<>());
            userPostList.put(user,new LinkedList<>());
            userPMList.put(user,new LinkedList<>());
            pendingNotifications.put(user,new LinkedList<>());

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

    private User checkIfUserRegistered(String username, String password){
        for (User user : registerdUserList){
            if(user.getUsername().equals(username)){
                if(user.getPassword().equals(password)){
                    return user;
                }
            }
        }
        return null;
    }

    /*LOGIN*/
    public User verifyUserDetails(String username, String password , boolean captcha){
        if(captcha) {
            User user = checkIfUserRegistered(username, password);
            if (user != null) {
                if (!user.isLogged()) {
                    return user;
                }
            }
        }
        return null;
    }

    /*FOLLOW*/
    public boolean checkIfUserFollowerOfOtherUser(User user, User other){
        if(this.followersList.containsKey(other)){
            if (this.followersList.get(other).contains(user)){
                return true;
            }
        }
        return false;
    }

    public void follow(User user, User userToFollow){
        this.followersList.get(userToFollow).add(user);
    }

    public void unfollow(User user, User userToUnfollow){
        if(this.followersList.containsKey(userToUnfollow)){
            this.followersList.get(userToUnfollow).remove(user);
        }
    }

    /*LOGSTAT && STAT*/
    public List<User> getRegisterdUserList() {
        return registerdUserList;
    }

    public short getNumPosts(User user){
        short numPosts = 0;
        if (userPostList.containsKey(user)) {
            numPosts = (short) userPostList.get(user).size();
        }
        return numPosts;
    }

    public short getNumFollowersForUser(User user){
        short numFollowers = 0;
        if (followersList.containsKey(user)) {
            numFollowers = (short)followersList.get(user).size();
        }
        return numFollowers;
    }

    public short getFollowingNumForUser(User user){
        final int[] output = {0};
        followersList.forEach((k, v) -> {
            for (User user1 : v) {
                if(user.equals(user1)){
                    output[0] = output[0] + 1;
                }
            }
        });
        return (short)output[0];
    }

    public List<User> getFollowers (User user) {
        return followersList.get(user);
    }

    /*Post*/
    public void addPost(User user, Post post){
        this.userPostList.get(user).add(post);
    }

    public void removeBlockedUsers(User user, List<User> taggedUsers){
        for (User u : taggedUsers){
            if (blockingUsers.get(user).contains(u)){
                taggedUsers.remove(u);
            }
        }
    }

    public void addMessageToPendingNotifications (List<User> notLoggedUsers, Message message){
        for (User user: notLoggedUsers){
            pendingNotifications.get(user).add(message);
        }
    }


    /*Pm*/
    public boolean canCommunicate(User sender, User sendTo){
        if (blockingUsers.get(sender).contains(sendTo))
            return false;
        if (blockingUsers.get(sendTo).contains(sender))
            return false;
        return true;
    }

    public String filter (String pm){
        String [] words = pm.split(" ");
        String filteredPM = "";
        for (String word : words){
            if (filteredWords.contains(word)){
                word = "<filtered>";
            }
            filteredPM +=" " + word;
        }
        return filteredPM.substring(1);
    }

    /*Block*/
    public void block (User blockingUser, User blockedUser){
        blockingUsers.get(blockingUser).add(blockedUser);
    }

    public List<User> getBlockingUsers(User user) {
        return blockingUsers.get(user);
    }
}
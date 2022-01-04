package bgu.spl.net.srv;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Post extends Message {

    private String post;
    private LocalDateTime timeStamp;
//    private List<User> followersReceiv = new ArrayList<>();

    public Post(String post  /*List<User> followersReceiv*/) {
        this.post = post;
        this.timeStamp = LocalDateTime.now();
//        this.followersReceiv = followersReceiv;
    }

    public String getPost() {
        return post;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    //    public List<User> getFollowersReceiv() {
//        return followersReceiv;
//    }

}

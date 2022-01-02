package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.List;

public class Post extends Message {

    private String post;
    private List<User> followersReceiv = new ArrayList<>();

    public Post(String post, List<User> followersReceiv) {
        this.post = post;
        this.followersReceiv = followersReceiv;
    }

    public String getPost() {
        return post;
    }

    public List<User> getFollowersReceiv() {
        return followersReceiv;
    }

}

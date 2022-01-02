package bgu.spl.net.srv;

public class PM extends Message{

    private String message;
    private User receiver;

    public PM(String message, User receiver) {
        this.message = message;
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public User getReceiver() {
        return receiver;
    }
}

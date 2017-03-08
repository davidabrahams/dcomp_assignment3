import java.io.Serializable;

public class Message implements Serializable {
    String sender, receiver, msg;

    public Message (String s, String r, String m) {
        super();
        this.sender = s;
        this.receiver = r;
        this.msg = m;
    }

    @Override
    public String toString() {
        return "Message from " + sender + " to " + receiver + ". " + msg;
    }
}

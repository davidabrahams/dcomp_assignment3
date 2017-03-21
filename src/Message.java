import java.io.Serializable;

public class Message implements Serializable {
    public static final int MONEY=0;
    public static final int ELECTION=1;
    public static final int MARKER=2;
    int messageType;
    int dollars;
    NameIP sender;
    NameIP bestCandidate;

    public Message(int type) {
        messageType = type;
    }
}

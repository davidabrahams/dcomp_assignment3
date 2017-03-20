import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SnapshotState implements Serializable {
    int state;
    NameIP nip;
    Map<NameIP, List<Integer>> channels;
    public SnapshotState(int state, NameIP nip, Map<NameIP, List<Integer>> channels) {
        this.state = state;
        this.nip = nip;
        this.channels = channels;
    }

    public int totalValue() {
        int s = state;
        for (List<Integer> l : channels.values()) {
            for (Integer i : l) {
                s += i;
            }
        }
        return s;
    }

    @Override
    public String toString() {
        String s = "";
        s += nip.toString() + "\n";
        s += "Current balance: " + state + "\n";
        s += "Incoming channels: " + "\n";
        for (NameIP nip : channels.keySet())
            s += nip.name + ": " + Arrays.toString(channels.get(nip).toArray()) + "\n";
        return s;
    }
}

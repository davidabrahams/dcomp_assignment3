import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    private String name, ip;
    private int balance;
    private List<NameIP> otherPeers;
    private NameIP nextPeer;
    private boolean isLeader;
    private Map<NameIP, List<Integer>> channels;
    private Map<NameIP, List<Integer>> markedChannels;
    private Map<NameIP, Boolean> markerReceived;
    private int state;
    private boolean receivedMarker;
    private boolean shouldSendMarkers;

    public Peer(String name, String ip) throws RemoteException {
        balance = 200;
        this.name = name;
        this.ip = ip;
        otherPeers = new ArrayList<>();
    }

    @Override
    public void addPeer(NameIP peer) throws RemoteException {
        otherPeers.add(peer);
    }

    @Override
    public void receiveMoney(NameIP sender, int m) throws RemoteException {
        balance += m;
        if (channels != null && channels.containsKey(sender)) {
//            System.out.println("$" + m + " added to channel");
            channels.get(sender).add(m);
        }
        System.out.println("I received $" + m + ". Current balance: " + balance);
    }

    @Override
    public void setNextPeer(NameIP nip) throws RemoteException {
        nextPeer = nip;
    }

    NameIP nameIP() {
        return new NameIP(this.name, this.ip);
    }

    @Override
    public void electionMessage(NameIP bestNip, boolean alreadyElected) throws RemoteException, NotBoundException {
        System.out.println("Leader election message received. Best candidate: " + bestNip + ". Already elected: " + alreadyElected);
        this.isLeader = false;
        if (nameIP().equals(bestNip)) {
            this.isLeader = true;
            if (alreadyElected) {
                System.out.println("I, " + nameIP().name + " was elected the leader!");
                return;
            }
        }
        NameIP higher = (bestNip == null || nameIP().compareTo(bestNip) > 0) ? nameIP() : bestNip;
        (Util.getPeer(nextPeer)).electionMessage(higher, this.isLeader || alreadyElected);
    }

    @Override
    public void startSnapShot() {
        System.out.println("Starting snapshot process.");
        receivedMarker = false;
        shouldSendMarkers = false;
        channels = new HashMap<>();
        markerReceived = new HashMap<>();
        markedChannels = new HashMap<>();
        for (NameIP p : otherPeers)
            markerReceived.put(p, false);
    }

    @Override
    public SnapshotState endSnapShot() {
        return new SnapshotState(state, nameIP(), markedChannels);
    }

    private void recordAndSendMarkers() throws RemoteException, NotBoundException {
        for (NameIP channel : otherPeers) {
            System.out.println("Sending marker to " + channel.name);
            (Util.getPeer(channel)).receiveMarker(nameIP());
            System.out.println("Marker sent to " + channel.name);
        }
    }

    @Override
    public void receiveMarker(NameIP sender) throws RemoteException, NotBoundException {
        if (sender == null) {
            startSnapShot();
            for (NameIP channel : otherPeers)
                (Util.getPeer(channel)).startSnapShot();
        } else {
            System.out.println("Marker received from " + sender.name);
            markerReceived.put(sender, true);
        }

        if (!receivedMarker) {
            // Record the process state
            state = balance;
            System.out.println("Recording state as " + state);
            System.out.println("Listening on all channels");
            // Start listening on all channels
            for (NameIP channel : otherPeers)
                channels.put(channel, new ArrayList<>());
            // Received the first marker
            receivedMarker = true;
            shouldSendMarkers = true;
            if (sender != null)
                markedChannels.put(sender, new ArrayList<>());
        } else {
            markedChannels.put(sender, new ArrayList<>(channels.get(sender)));
            System.out.println("Recording channel state as " + markedChannels.get(sender));
            System.out.println("Am I the leader? " + isLeader);
            if (isLeader && !markerReceived.values().contains(false)) {
                printSnapshotState();
            }
        }
    }

    private void printSnapshotState() throws RemoteException, NotBoundException {
        // We've received markers from every incoming channel!
        System.out.println("Snapshot complete!");
        SnapshotState s = endSnapShot();
        System.out.println(s);
        int totalValue = s.totalValue();
        for (NameIP channel : otherPeers) {
            s = (Util.getPeer(channel)).endSnapShot();
            totalValue += s.totalValue();
            System.out.println(s);
        }
        System.out.println("Total Value: " + totalValue);
        System.out.println();
        System.out.println();
    }

    public void sendMoneyToPeer() throws RemoteException, NotBoundException {
        if (shouldSendMarkers) {
            recordAndSendMarkers();
            shouldSendMarkers = false;
        }
        int money = ThreadLocalRandom.current().nextInt(0, balance + 1);
        NameIP random = otherPeers.get(ThreadLocalRandom.current().nextInt(otherPeers.size()));
        PeerInterface recipient = Util.getPeer(random);// Every 20 iterations, take a snapshot
        System.out.println("Sending $" + Integer.toString(money) + " to " + random.name + ". Current balance: " + balance);
        recipient.receiveMoney(nameIP(), money);
        balance -= money;
        System.out.println("Sent $" + Integer.toString(money) + " to " + random.name + ". Current balance: " + balance);

    }

    @Override
    public boolean isLeader() throws RemoteException {
        return isLeader;
    }


}
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
    private boolean hasMarker;
    private int state;
    private boolean receivedMarker;

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
        if (channels != null && channels.containsKey(sender))
            channels.get(sender).add(m);
        balance += m;
        System.out.println("I received $" + m + ". Current balance: " + balance);
    }

    @Override
    public void setNextPeer(NameIP nip) throws RemoteException {
        nextPeer = nip;
    }

    private NameIP nameIP() {
        return new NameIP(this.name, this.ip);
    }

    @Override
    public void electionMessage(NameIP bestNip, boolean alreadyElected) throws RemoteException, NotBoundException {
        System.out.println("Leader election message received. Best candidate: " + bestNip + ". Already elected: " + alreadyElected);
        if (nameIP().equals(bestNip)) {
            if (alreadyElected) {
                System.out.println("I, " + nameIP().name + " was elected the leader!");
                return;
            }
            this.isLeader = true;
        }
        (Util.getPeer(nextPeer)).electionMessage(nameIP().compareTo(bestNip) > 0 ? nameIP() : bestNip, this.isLeader || alreadyElected);
    }

    @Override
    public void startSnapShot() {
        System.out.println("Starting snapshot process.");
        receivedMarker = false;
        hasMarker = false;
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

    private void handleMarkers() throws RemoteException, NotBoundException {
        if (hasMarker) {
            // Send markers to all other channels
            for (NameIP channel : otherPeers) {
                System.out.println("Sending marker to " + channel.name);
                (Util.getPeer(channel)).receiveMarker(nameIP());
            }
        }
        hasMarker = false;
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
            // Received the first marker
            receivedMarker = true;
            // Record the process state
            state = balance;
            System.out.println("Recording state as " + state);
            System.out.println("Listening on all channels");
            // Start listening on all channels
            for (NameIP channel : otherPeers)
                channels.put(channel, new ArrayList<>());
            if (sender != null) {
                markedChannels.put(sender, new ArrayList<>(channels.get(sender)));
                System.out.println("Recording channel state as " + markedChannels.get(sender));
            }
            hasMarker = true;
        } else {
            markedChannels.put(sender, new ArrayList<>(channels.get(sender)));
            System.out.println("Recording channel state as " + markedChannels.get(sender));
            if (isLeader && !markerReceived.values().contains(false)) {
                printSnapshotState();
            }
        }
    }

    private void printSnapshotState() throws RemoteException, NotBoundException {
        // We've received markers from every incoming channel!
        System.out.println("Snapshot complete!");
        SnapshotState s = endSnapShot();
        System.out.print(s);
        int totalValue = s.totalValue();
        for (NameIP channel : otherPeers) {
            s = (Util.getPeer(channel)).endSnapShot();
            totalValue += s.totalValue();
            System.out.print(s);
        }
        System.out.println("Total Value: " + totalValue);
        System.out.println();
        System.out.println();
    }

    public void start() throws InterruptedException, RemoteException, NotBoundException {
        System.out.println("Process started. Waiting for peers to join network.");
        while (otherPeers.size() < 1) {
            Thread.sleep(1000);
        }
        System.out.println("Peers joined! Commence sending money.");
        (Util.getPeer(nextPeer)).electionMessage(nameIP(), false);
        int count = 0;
        while (true) {
            Thread.sleep(ThreadLocalRandom.current().nextInt(5, 10));
            handleMarkers();
            int money = ThreadLocalRandom.current().nextInt(0, balance + 1);
            NameIP random = otherPeers.get(ThreadLocalRandom.current().nextInt(otherPeers.size()));
            PeerInterface peer = Util.getPeer(random);
            balance -= money;
            System.out.println("Sent $" + Integer.toString(money) + " to " + random.name + ". Current balance: " + balance);
            peer.receiveMoney(nameIP(), money);
            // Every 20 iterations, take a snapshot
            if (isLeader) {
                if (count==20) {
                    receiveMarker(null);
                    count = 0;
                } else
                    count++;
            }
        }
    }
}
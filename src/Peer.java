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
    private Queue<Message> messages;

    public Peer(String name, String ip) throws RemoteException {
        balance = 200;
        this.name = name;
        this.ip = ip;
        otherPeers = new ArrayList<>();
        messages = new LinkedList<>();
    }

    @Override
    public void addPeer(NameIP peer) throws RemoteException {
        otherPeers.add(peer);
    }

    private void incrementMoney(int m, NameIP sender) {
        balance += m;
        if (channels != null && channels.containsKey(sender)) {
            channels.get(sender).add(m);
            System.out.println("Processing $" + m + ". New balance: " + balance + ". Also adding to incoming channel: " + sender.name);
        } else {
            System.out.println("Processing $" + m + ". New balance: " + balance);
        }
    }

    @Override
    public void setNextPeer(NameIP nip) throws RemoteException {
        nextPeer = nip;
    }

    @Override
    public void sendMessage(Message m) throws RemoteException {
        messages.offer(m);
    }

    private NameIP nameIP() {
        return new NameIP(this.name, this.ip);
    }

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
        Message newMessage = new Message(Message.ELECTION);
        newMessage.bestCandidate = higher;
        newMessage.alreadyElected = this.isLeader || alreadyElected;
        (Util.getPeer(nextPeer)).sendMessage(newMessage);
    }

    @Override
    public void startSnapShot() {
        System.out.println("Starting snapshot process.");
        receivedMarker = false;
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

    private void sendMarkers() throws RemoteException, NotBoundException {
        for (NameIP channel : otherPeers) {
            System.out.println("Sending marker to " + channel.name);
            Message m = new Message(Message.MARKER);
            m.sender = nameIP();
            (Util.getPeer(channel)).sendMessage(m);
            System.out.println("Marker sent to " + channel.name);
        }
    }

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
            if (sender != null)
                markedChannels.put(sender, new ArrayList<>());
            sendMarkers();
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

    public void handleMessages() throws RemoteException, NotBoundException {
        while (!messages.isEmpty()) {
            Message m = messages.poll();
            if (m.messageType == Message.ELECTION) {
                electionMessage(m.bestCandidate, m.alreadyElected);
            } else if (m.messageType == Message.MONEY) {
                incrementMoney(m.dollars, m.sender);
            } else if (m.messageType == Message.MARKER) {
                receiveMarker(m.sender);
            }
        }

    }

    public void clearMessagesAndMakeTransaction() throws RemoteException, NotBoundException {
        handleMessages();
        int money = ThreadLocalRandom.current().nextInt(0, balance + 1);
        NameIP random = otherPeers.get(ThreadLocalRandom.current().nextInt(otherPeers.size()));
        PeerInterface recipient = Util.getPeer(random);
        Message m = new Message(Message.MONEY);
        m.sender = nameIP();
        m.dollars = money;
        recipient.sendMessage(m);
        balance -= money;
        System.out.println("Sent $" + Integer.toString(money) + " to " + random.name + ". Current balance: " + balance);

    }

    @Override
    public boolean isLeader() throws RemoteException {
        return isLeader;
    }


}
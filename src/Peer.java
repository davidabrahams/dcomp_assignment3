import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    private String name, ip;
    private int balance;
    private List<NameIP> otherPeers;
    private NameIP nextPeer;
    private boolean isLeader;
    private Map<NameIP, List<Integer>> channels;
    private Map<NameIP, Boolean> markerReceived;
    private int state;
    private boolean receivedMarker;

    public Peer(String name, String ip) throws RemoteException {
        balance = 200;
        this.name = name;
        this.ip = ip;
    }

    @Override
    public void startSendingMoney(List<NameIP> peers) throws RemoteException {
        otherPeers = peers;
        Thread t1 = new Thread(new TransactionRunner());
        t1.start();
    }

    @Override
    public void receiveMoney(NameIP sender, int m) throws RemoteException {
        System.out.println("I received $" + Integer.toString(m));
        if (channels.containsKey(sender))
            channels.get(sender).add(m);
        balance += m;
    }

    @Override
    public void setNextPeer(NameIP nip) throws RemoteException {
        nextPeer = nip;
    }

    private NameIP nameIP() {
        return new NameIP(this.name, this.ip);
    }

    @Override
    public void receiveMessage(NameIP bestNip, boolean alreadyElected) throws RemoteException, NotBoundException {
        System.out.println("Leader election message received. Best candidate: " + bestNip + ". Already elected: " + alreadyElected);
        if (nameIP().equals(bestNip)) {
            if (alreadyElected) {
                System.out.println("I, " + nameIP().name + " was elected the leader!");
                return;
            }
            this.isLeader = true;
        }
        (Util.getPeer(nextPeer)).receiveMessage(nameIP().compareTo(bestNip) > 0 ? nameIP() : bestNip, this.isLeader || alreadyElected);
    }

    @Override
    public void clearSnapShot() {
        System.out.println("Clearing snapshot data.");
        channels = new HashMap<>();
        markerReceived = new HashMap<>();
        for (NameIP p : otherPeers)
            markerReceived.put(p, false);
    }

    @Override
    public SnapshotState endSnapShot() {
        return new SnapshotState(state, nameIP(), channels);
    }

    @Override
    public void receiveMarker(NameIP sender) throws RemoteException, NotBoundException {
        if (sender != null)
            markerReceived.put(sender, true);
        else {
            clearSnapShot();
            for (NameIP channel : otherPeers)
                (Util.getPeer(channel)).clearSnapShot();
        }
        if (receivedMarker) {
            if (isLeader) {
                if (!markerReceived.values().contains(false)) {
                    // We've received markers from every incoming channel!
                    System.out.println("Snapshot complete!");
                    System.out.println(endSnapShot());
                    for (NameIP channel : otherPeers) {
                        System.out.println((Util.getPeer(channel)).endSnapShot());
                    }
                }
            }
            return;
        }
        // Received the first marker
        receivedMarker = true;
        // Record the process state
        state = balance;
        // Send markers to all other channels
        for (NameIP channel : otherPeers) {
            // Initialize the incoming channels
            channels.put(channel, new ArrayList<>());
            (Util.getPeer(channel)).receiveMarker(nameIP());
        }
    }

    private class TransactionRunner implements Runnable {
        public void run() {
            try {
                (Util.getPeer(nextPeer)).receiveMessage(nameIP(), false);
                while (true) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                    int money = ThreadLocalRandom.current().nextInt(0, balance + 1);
                    NameIP random = otherPeers.get(ThreadLocalRandom.current().nextInt(otherPeers.size()));
                    PeerInterface peer = Util.getPeer(random);
                    balance -= money;
                    System.out.println("Sending $" + Integer.toString(money) + " to " + random.name);
                    peer.receiveMoney(nameIP(), money);
                    if (isLeader && ThreadLocalRandom.current().nextInt(0, 10) == 0) {
                        receiveMarker(null);
                    }
                }
            } catch (RemoteException | NotBoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
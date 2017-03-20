import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    private String name, ip;
    private int balance;
    private List<NameIP> otherPeers;
    private NameIP nextPeer;
    private boolean isLeader;

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
    public void receiveMoney(int m) throws RemoteException {
        System.out.println("I received $" + Integer.toString(m));
        balance += m;
    }

    @Override
    public void setNextPeer(NameIP nip) throws RemoteException {
        nextPeer = nip;
    }

    @Override
    public void receiveMessage(NameIP prevNip, boolean alreadyElected) throws RemoteException, NotBoundException {
        System.out.println("Leader election message received from " + prevNip + ". Already elected: " + alreadyElected);
        NameIP myNip = new NameIP(this.name, this.ip);
        if (myNip.equals(prevNip)) {
            if (alreadyElected) {
                System.out.println("Leader elected: " + myNip.name);
                return;
            }
            this.isLeader = true;
        }
        (Util.getPeer(nextPeer)).receiveMessage(myNip.compareTo(prevNip) > 0 ? myNip : prevNip, this.isLeader || alreadyElected);
    }

    public String getName() {
        return name;
    }

    private class TransactionRunner implements Runnable {
        public void run() {
            NameIP myNip = new NameIP(name, ip);
            try {
                (Util.getPeer(nextPeer)).receiveMessage(myNip, false);
            } catch (RemoteException | NotBoundException e) {
                System.out.println("Error in leader election");
                e.printStackTrace();
            }
            while (true) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1999, 2000));
                } catch (InterruptedException e) {
                    System.out.println("Error in Thread.sleep");
                    return;
                }
                int money = ThreadLocalRandom.current().nextInt(0, balance + 1);
                NameIP random = otherPeers.get(ThreadLocalRandom.current().nextInt(otherPeers.size()));
                PeerInterface peer = null;
                try {
                    peer = Util.getPeer(random);
                } catch (RemoteException | NotBoundException e) {
                    System.out.println("Error looking up peer with name: " + random.name + " at IP: " + random.ip);
                }
                balance -= money;
                System.out.println("Sending $" + Integer.toString(money) + " to " + random.name);
                try {
                    peer.receiveMoney(money);
                } catch (RemoteException e) {
                    System.out.println("Error sending money to " + random.name);
                    return;
                }
            }
        }
    }
}
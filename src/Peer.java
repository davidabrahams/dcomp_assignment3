import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    private String name;
    private int balance;
    private List<String> allPeers;
    private Registry reg;

    public Peer(Registry reg) throws RemoteException {
        balance = 200;
        this.reg = reg;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void startSendingMoney(List<String> peers) throws RemoteException {
        allPeers = peers;
        Thread t1 = new Thread(new TransactionRunner());
        t1.start();
    }

    @Override
    public void receiveMoney(int m) throws RemoteException {
        System.out.println("I received $" + Integer.toString(m));
        balance += m;
    }

    public String getName() {
        return name;
    }

    private class TransactionRunner implements Runnable {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1999, 2000));
                } catch (InterruptedException e) {
                    System.out.println("Error in Thread.sleep");
                    return;
                }
                int money = ThreadLocalRandom.current().nextInt(0, balance + 1);
                String randomPeer = allPeers.get(ThreadLocalRandom.current().nextInt(allPeers.size()));
                PeerInterface peer;
                try {
                    peer = (PeerInterface) reg.lookup(randomPeer);
                } catch (RemoteException | NotBoundException e) {
                    System.out.println("Error looking up " + randomPeer + " in registry");
                    return;
                }
                balance -= money;
                System.out.println("Sending $" + Integer.toString(money) + " to " + randomPeer);
                try {
                    peer.receiveMoney(money);
                } catch (RemoteException e) {
                    System.out.println("Error sending money to " + randomPeer);
                    return;
                }
            }
        }
    }

}
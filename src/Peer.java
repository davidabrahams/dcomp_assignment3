import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    private String name;
    private int balance;
    private List<NameIP> otherPeers;

    public Peer(String name) throws RemoteException {
        balance = 200;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
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
                NameIP random = otherPeers.get(ThreadLocalRandom.current().nextInt(otherPeers.size()));
                PeerInterface peer;
                try {
                    peer = (PeerInterface) LocateRegistry.getRegistry(random.ip, 1099).lookup(random.name);
                } catch (RemoteException | NotBoundException e) {
                    System.out.println("Process with name " + random.name +
                            " not found on server at IP " + random.ip);
                    return;
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
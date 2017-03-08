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

    public Peer(Registry reg)  throws RemoteException {
        balance = 200;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void makeTransactions() throws InterruptedException, RemoteException, NotBoundException {
        while (true) {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 2000));
            int money = ThreadLocalRandom.current().nextInt(1, balance + 1);
            String randomPeer = allPeers.get(ThreadLocalRandom.current().nextInt(allPeers.size()));
            PeerInterface peer = (PeerInterface) reg.lookup(randomPeer);
            balance -= money;
            System.out.println("Sending $" + Integer.toString(money) + " to " + randomPeer);
            peer.receiveMoney(money);
        }
    }

    @Override
    public void startSendingMoney(List<String> peers) throws RemoteException {
        System.out.println("IM SENDING MONEY!");
        allPeers = peers;
    }

    @Override
    public void receiveMoney(int m) throws RemoteException {
        System.out.println("I received $" + Integer.toString(m));
        balance += m;
    }

    public String getName() {
        return name;
    }
}
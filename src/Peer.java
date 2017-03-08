import java.rmi.*;
import java.rmi.server.*;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    public Peer()  throws RemoteException {
    }

    @Override
    public void startSendingMoney() throws RemoteException {
        System.out.println("IM SENDING MONEY!");
    }
}
import java.rmi.*;
import java.rmi.server.*;

public class Client extends UnicastRemoteObject implements ClientInterface {

    public String name;

    public Client(String n)  throws RemoteException {
        this.name=n;
    }

    @Override
    public String getName() throws RemoteException {
        return this.name;
    }

    @Override
    public void sendToClient(Message m) throws RemoteException{
        System.out.println();
        System.out.println();
        System.out.println("Message received from " + m.sender + ":");
        System.out.println(m.msg);
        System.out.println();
        System.out.print("Your command -> ");
    }
}
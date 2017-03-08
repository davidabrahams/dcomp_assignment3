import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements ServerInterface{
    private Map<String, PeerInterface> clientMap;

    public Server() throws RemoteException {
        clientMap = new HashMap<>();
    }

    @Override
    public boolean register(PeerInterface client) throws RemoteException {
        System.out.println("Request to register " + client.getName() +" received.");
        if (clientMap.keySet().contains(client.getName()))
            return false;
        clientMap.put(client.getName(), client);
        return true;
    }

    @Override
    public List<String> getUsers() throws RemoteException {
        System.out.println("Request for user received");
        List<String> l  = new ArrayList<>();
        l.addAll(clientMap.keySet());
        return l;
    }

    @Override
    public boolean sendToServer(Message m) throws RemoteException {
        System.out.println("Server message received");
        if(!clientMap.keySet().contains(m.receiver))
            return false;
        clientMap.get(m.receiver).sendToClient(m);
        return true;
    }

}

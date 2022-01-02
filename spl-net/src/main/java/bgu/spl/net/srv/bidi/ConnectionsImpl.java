package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.User;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionsById = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, User> connectionsByUser = new ConcurrentHashMap<>();

    private static ConnectionsImpl instance = null;

    public static ConnectionsImpl getInstance() {
        if (instance == null) {
            instance = new ConnectionsImpl();
        }
        return instance;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (connectionsById.containsKey(connectionId)) {
            connectionsById.get(connectionId).send(msg);
            return true;
        }
        //if the connection ID doesn't exist you should return false and not proceed
        return false;
    }

    @Override
    public void broadcast(T msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }

    public void addConnection(int idCounter, ConnectionHandler<T> handler) {
        connectionsById.put(idCounter, handler);
    }

    public void addUserForConnection(User user, int idCounter){
        if(connectionsById.containsKey(idCounter)){
            connectionsByUser.put(idCounter,user);
        }
    }

    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnectionsById() {
        return connectionsById;
    }

    public ConcurrentHashMap<Integer, User> getConnectionsByUser() {
        return connectionsByUser;
    }
}

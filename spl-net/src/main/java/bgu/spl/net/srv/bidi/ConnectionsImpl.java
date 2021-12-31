package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BlockingConnectionHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl implements Connections<List<Object>> {

    private ConcurrentHashMap<Integer, BlockingConnectionHandler<List<Object>>> connections = new ConcurrentHashMap<>();
    AtomicInteger idCounter;

    public ConnectionsImpl() {
        this.idCounter = new AtomicInteger(0);
    }

    public void addConnection(BlockingConnectionHandler<List<Object>> handler) {
        System.out.println("New Client Connected");
        int id = idCounter.getAndIncrement();
        connections.put(id, handler);
    }

    @Override
    public boolean send(int connectionId, List<Object> msg) {
        return false;
    }

    @Override
    public void broadcast(List<Object> msg) {

    }

    @Override
    public void disconnect(int connectionId) {

    }
}

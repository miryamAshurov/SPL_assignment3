package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.bidi.*;
import java.io.IOException;
import java.util.List;

public class TPCMain {
    public static void main(String[] args) throws IOException {
        ConnectionsImpl connections = ConnectionsImpl.getInstance();
        try (BaseServer<List<Object>> server = BaseServerImp.threadPerClient(7777,
                () -> new BGSProtocol(connections),
                () -> new BidiMessageEncoderDecoder())) {
            server.setConnections(connections);
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

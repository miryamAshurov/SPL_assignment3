package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.srv.Reactor;
import bgu.spl.net.srv.BGSProtocol;
import bgu.spl.net.srv.BidiMessageEncoderDecoder;
import bgu.spl.net.srv.ConnectionsImpl;
import java.io.IOException;

public class ReactorMain {
    public static void main(String[] args) throws IOException {
        ConnectionsImpl connections = ConnectionsImpl.getInstance();
        try (Reactor server = new Reactor(
                Integer.valueOf(args[0]),  //number of working threads
                Integer.valueOf(args[1]),  //port
                () -> new BGSProtocol(connections),
                () -> new BidiMessageEncoderDecoder())){
            server.serve();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

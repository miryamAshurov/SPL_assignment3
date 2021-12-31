package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.bidi.BGSProtocol;
import bgu.spl.net.srv.bidi.BaseServerImp;
import bgu.spl.net.srv.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.srv.bidi.ConnectionsImpl;

import java.io.IOException;
import java.util.List;

public class TPCMain {
    public static void main(String[] args) throws IOException {
        ConnectionsImpl connections = new ConnectionsImpl();
        try(BaseServer<List<Object>> server =
                    BaseServerImp.threadPerClient(7777, ()-> (MessagingProtocol<List<Object>>) new BGSProtocol(connections),()->new BidiMessageEncoderDecoder())){
            server.setConnections(connections);
            server.serve();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}

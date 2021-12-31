package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;

import java.util.function.Supplier;

public class BaseServerImp extends BaseServer {



    public BaseServerImp(int port, Supplier protocolFactory, Supplier encdecFactory) {
        super(port, protocolFactory, encdecFactory);
    }


    @Override
    protected void execute(BlockingConnectionHandler handler) {

    }

    public static <T> BaseServer<T> threadPerClient(
            int port,
            Supplier<MessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        return new BaseServerImp(port, protocolFactory, encdecFactory) {
            @Override
            protected void execute(BlockingConnectionHandler handler) {
                new Thread(handler).start();
            }
        };

    }
}

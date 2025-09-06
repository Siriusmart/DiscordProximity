package ws.siri.proximity.backend;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

@ServerEndpoint(
    value = "/subscription",
    encoders = { SubscriptionConnection.MessageEncoder.class }, 
    decoders = { SubscriptionConnection.MessageDecoder.class }
)
public class SubscriptionConnection {
    public static class Message {
        public String t;
        public Object c;

        public Message() {}

        public Message(String type, Object content) {
            this.t = type;
            this.c = content;
        }
    }

    public static class MessageEncoder implements Encoder.Text<Message> {

        private static Gson gson = new Gson();

        @Override
        public String encode(Message message) throws EncodeException {
            return gson.toJson(message);
        }

        @Override
        public void init(EndpointConfig endpointConfig) {}

        @Override
        public void destroy() {}
    }

    public static class MessageDecoder implements Decoder.Text<Message> {
        private static Gson gson = new Gson();

        @Override
        public Message decode(String s) throws DecodeException {
            return gson.fromJson(s, Message.class);
        }

        @Override
        public boolean willDecode(String s) {
            return (s != null);
        }

        @Override
        public void init(EndpointConfig endpointConfig) {}

        @Override
        public void destroy() {}
    }


    private Session session;
    private static HashSet<SubscriptionConnection> connections = new HashSet<>();

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        this.session = session;
        connections.add(this);

        Message message = new Message();
        message.t = "connected";
        this.session.getBasicRemote().sendObject(message);
    }

    @OnMessage
    public void onMessage(Session session, Message message) throws IOException {
        switch(message.t) {
            case "sub":
                String[] subList = ((List<Object>) message.c).toArray(new String[0]);
                Records.subscribe(subList);
                break;
            case "unsub":
                String[] unsubList = ((List<Object>) message.c).toArray(new String[0]);
                Records.unsubscribe(unsubList);
                break;
            case "clear":
                Records.unsubscribeAll();
                break;
            default:
                Logger.getLogger("WS proximity onMessage").log(Level.WARNING, "Unknown message type " + message.t);
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        connections.remove(this);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.getLogger("Subscription Endpoint").log(Level.SEVERE, throwable.toString());
    }

    public static void broadcast(Message message) {
        for(SubscriptionConnection conn : connections) {
            try {
                conn.session.getBasicRemote().sendObject(message);
            } catch (Exception e) {
                Logger.getLogger("Subscription broadcast").log(Level.SEVERE, "Failed to send object to a client " + e);
            }
        }
    }
}

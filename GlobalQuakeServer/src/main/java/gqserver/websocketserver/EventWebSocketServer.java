package gqserver.websocketserver;

import globalquake.core.GlobalQuake;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.archive.ArchivedQuake;
import globalquake.core.events.GlobalQuakeEventListener;
import globalquake.core.events.specific.QuakeCreateEvent;
import globalquake.core.events.specific.QuakeRemoveEvent;
import globalquake.core.events.specific.QuakeUpdateEvent;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.json.JSONObject;
import org.json.JSONArray;

public class EventWebSocketServer extends WebSocketServer {
    private static InetSocketAddress address = new InetSocketAddress("0.0.0.0", 8887);

    private static EventWebSocketServer instance = new EventWebSocketServer();

    

	private EventWebSocketServer() {
        super(address);
    }
    
    public void init() {
        this.initEventListeners();
    }

    private void initEventListeners() {
        GlobalQuake.instance.getEventHandler().registerEventListener(new GlobalQuakeEventListener()
        {
            @Override
            public void onQuakeCreate(QuakeCreateEvent event) {
                ArchivedQuake quake = new ArchivedQuake(event.earthquake());
                quake.setRegion(event.earthquake().getRegion());
                sendQuakeCreate(quake);
            }

            @Override
            public void onQuakeUpdate(QuakeUpdateEvent event) {
                ArchivedQuake quake = new ArchivedQuake(event.earthquake());
                quake.setRegion(event.earthquake().getRegion());
                sendQuakeUpdate(quake);
            }
        });

    }

    public static EventWebSocketServer getInstance() {
        return instance;
    }

    private void sendQuakeCreate(ArchivedQuake quake) {
        JSONObject json = new JSONObject();
        json.put("action", "create");
        json.put("data", quake.getGeoJSON());
        this.broadcast(json.toString());
    }

    private void sendQuakeUpdate(ArchivedQuake quake) {
        JSONObject json = new JSONObject();
        json.put("action", "update");
        json.put("data", quake.getGeoJSON());
        this.broadcast(json.toString());
    }

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("new connection from " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

    @Override
    public void onMessage(WebSocket conn, String message) {
        //Protocol does not receive messages
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        //Protocol does not receive messages
    }

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
	}
	
	@Override
	public void onStart() {
		System.out.println("server started successfully");
	}


	public static void main(String[] args) {
        EventWebSocketServer s = EventWebSocketServer.getInstance();
        System.out.println("thread test Server started at " + s.getAddress());
    }
}
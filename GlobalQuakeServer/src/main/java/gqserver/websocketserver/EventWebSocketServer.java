package gqserver.websocketserver;

import globalquake.core.GlobalQuake;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.archive.ArchivedQuake;
import globalquake.core.events.GlobalQuakeEventListener;
import globalquake.core.events.specific.QuakeCreateEvent;
import globalquake.core.events.specific.QuakeRemoveEvent;
import globalquake.core.events.specific.QuakeUpdateEvent;

import globalquake.core.Settings;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.json.JSONObject;

import org.tinylog.Logger;

public class EventWebSocketServer extends WebSocketServer {
    private static InetSocketAddress address = new InetSocketAddress(Settings.RTWSEventIP, Settings.RTWSEventPort);

    private static EventWebSocketServer instance = new EventWebSocketServer();

	private EventWebSocketServer() {
        super(address);
    }
    
    public void init() {
        //This is an init because it should only be called if the module is intended to be used. Otherwise, it will be a waste of resources.
        this.initEventListeners();
    }

    private static ArchivedQuake convertToArchivedQuake(Earthquake quake) {
        ArchivedQuake archivedQuake = new ArchivedQuake(quake);
        archivedQuake.setRegion(quake.getRegion());
        return archivedQuake;
    }

    private void initEventListeners() {
        GlobalQuake.instance.getEventHandler().registerEventListener(new GlobalQuakeEventListener()
        {
            @Override
            public void onQuakeCreate(QuakeCreateEvent event) {
                broadcastQuake("create", convertToArchivedQuake(event.earthquake()));
            }

            @Override
            public void onQuakeUpdate(QuakeUpdateEvent event) {
                broadcastQuake("update", convertToArchivedQuake(event.earthquake()));
            }

            @Override
            public void onQuakeRemove(QuakeRemoveEvent event) {
                broadcastQuake("remove", convertToArchivedQuake(event.earthquake()));
            }
        });

    }

    public static EventWebSocketServer getInstance() {
        return instance;
    }

    private void broadcastQuake(String action, ArchivedQuake quake) {
        JSONObject json = new JSONObject();
        json.put("action", action);
        json.put("data", quake.getGeoJSON());
        this.broadcast(json.toString());
    }

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
        Logger.info("RTWS: New connection from {}", conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Logger.info("RTWS: Connection closed {}", conn.getRemoteSocketAddress());
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
        Logger.error("RTWS: An error occurred on connection {}: {}", conn.getRemoteSocketAddress(), ex);
	}
	
	@Override
	public void onStart() {
        Logger.info("RTWS: Server started on {}", address);
	}
}
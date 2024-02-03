package gqserver.api;

import java.io.IOException;
import java.io.Serializable;

public interface Packet extends Serializable {

    public static final long serialVersionUID = 0L;

    default void onServerReceive(ServerClient serverClient) throws IOException {}

}

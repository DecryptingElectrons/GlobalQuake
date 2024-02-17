package globalquake.client;

import globalquake.core.archive.ArchivedEvent;
import globalquake.core.archive.EarthquakeArchive;
import globalquake.core.archive.ArchivedQuake;
import globalquake.core.earthquake.data.Earthquake;
import globalquake.core.earthquake.quality.QualityClass;
import gqserver.api.Packet;
import gqserver.api.data.earthquake.ArchivedQuakeData;
import gqserver.api.packets.earthquake.ArchivedQuakePacket;

public class EarthquakeArchiveClient extends EarthquakeArchive {

    public void processPacket(ClientSocket ignoredSocket, Packet packet) {
        if(packet instanceof ArchivedQuakePacket quakePacket) {
            if(getArchivedQuakeByUUID(quakePacket.archivedQuakeData().uuid()) == null) {
                archiveQuake(quakePacket, null);
            }
        }
    }

    public void archiveQuake(ArchivedQuakePacket quakePacket, Earthquake earthquake){
        archiveQuake(createArchivedQuake(quakePacket), earthquake);
    }

    private ArchivedQuake createArchivedQuake(ArchivedQuakePacket quakePacket) {
        ArchivedQuakeData data = quakePacket.archivedQuakeData();
        Long dummyFirstUpdateMillis = data.origin() + 100; //The client does not receive this data, in order to satisfy the constructor, a dummy value is used. Event origin time + 100ms
        ArchivedQuake archivedQuake = new ArchivedQuake(
                data.uuid(), data.lat(), data.lon(), data.depth(), data.magnitude(), data.origin(), QualityClass.values()[data.qualityID()], data.finalUpdateMillis(), dummyFirstUpdateMillis
        );

        quakePacket.archivedEventDataList().forEach(archivedEventData -> archivedQuake.getArchivedEvents().add(new ArchivedEvent(
                archivedEventData.lat(), archivedEventData.lon(), archivedEventData.maxRatio(), archivedEventData.pWave()
        )));

        return archivedQuake;
    }
}

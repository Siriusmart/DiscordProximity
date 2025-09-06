package ws.siri.proximity.watcher;

import ws.siri.proximity.backend.Records;
import ws.siri.proximity.backend.SubscriptionConnection;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.HashSet;

public class VolumeUpdater {
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        NetHandlerPlayClient nethandler = Minecraft.getMinecraft().getNetHandler();
        EntityPlayerSP me = Minecraft.getMinecraft().thePlayer;

        HashSet<String> activePlayersInWorld = new HashSet<>();

        if(nethandler != null) {
            String myName = me.getName();
            for(NetworkPlayerInfo playerName : nethandler.getPlayerInfoMap()) {
                String ign = playerName.getGameProfile().getName();
                if(Records.playerIsActive(ign) && !ign.equals(myName)) {
                    activePlayersInWorld.add(ign);
                }

            }
        }

        HashMap<String, Double> distances = new HashMap<>();

        if(!activePlayersInWorld.isEmpty()) {
            Vec3 myLocation = me.getPositionEyes(me.eyeHeight);

            for(EntityPlayer player : Minecraft.getMinecraft().theWorld.playerEntities) {
                if(activePlayersInWorld.contains(player.getName())) {
                    distances.put(player.getName(), myLocation.distanceTo(player.getPositionEyes(player.eyeHeight)));
                }
            }
        }


        HashMap<String, Double> diff = Records.pushPlayers(distances, activePlayersInWorld);

        if(diff.isEmpty()) {
            return;
        }

        // CompletableFuture.runAsync(() -> {
            SubscriptionConnection.broadcast(new SubscriptionConnection.Message("set", diff));
        // });
    }
}

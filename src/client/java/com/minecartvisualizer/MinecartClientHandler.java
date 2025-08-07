package com.minecartvisualizer;

import com.minecartvisualizer.config.MinecartVisualizerConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.minecartvisualizer.MinecartVisualizerClient.travelTimers;

public class MinecartClientHandler {

    private static final Map<UUID, MinecartDataPayload> MINECART_DATA = new ConcurrentHashMap<>();
    private static final Map<UUID, HopperMinecartDataPayload> HOPPER_MINECART_DATA = new ConcurrentHashMap<>();
    private static final Map<UUID, TNTMinecartDataPayload> TNT_MINECART_DATA = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3d> serverMinecarts = new ConcurrentHashMap<>();
    private static final List<List<UUID>> minecartGroups = new CopyOnWriteArrayList<>();
    public static List<UUID> leaderMinecarts = new CopyOnWriteArrayList<>();


    //矿车分组，用于堆叠矿车显示优化
    public static void updateMinecartGroups() {
        minecartGroups.clear();

        List<Map.Entry<UUID, Vec3d>> minecarts = new ArrayList<>(serverMinecarts.entrySet());
        Iterator<Map.Entry<UUID, Vec3d>> iterator = serverMinecarts.entrySet().iterator();


        if (minecarts.size() == 1) {
            List<UUID> newGroup = new ArrayList<>();
            newGroup.add(minecarts.getFirst().getKey());
            minecartGroups.add(newGroup);
            return;
        }

        while (iterator.hasNext()){

            Map.Entry<UUID, Vec3d> entry = iterator.next();
            UUID uuid = entry.getKey();
            Vec3d pos = entry.getValue();

            if (!MinecartVisualizerUtils.isEntityLoaded(uuid)){
                iterator.remove();
                continue;
            }
            boolean addedToExistingGroup = false;

            for (List<UUID> group : minecartGroups) {
                UUID groupLeaderUUID = group.getFirst();
                Vec3d groupLeaderPos = serverMinecarts.get(groupLeaderUUID);

                if (groupLeaderPos != null) {
                    if (pos.squaredDistanceTo(groupLeaderPos) < 1) {
                        group.add(uuid);
                        addedToExistingGroup = true;
                        break;
                    }
                }
            }

            if (!addedToExistingGroup) {
                List<UUID> newGroup = new ArrayList<>();
                newGroup.add(uuid);
                minecartGroups.add(newGroup);
            }
        }

    }

    public static void register() {

        PayloadTypeRegistry.playS2C().register(MinecartDataPayload.ID, MinecartDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HopperMinecartDataPayload.ID, HopperMinecartDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TNTMinecartDataPayload.ID, TNTMinecartDataPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(MinecartDataPayload.ID, (payload, context) -> MinecraftClient.getInstance().execute(() -> {
            MINECART_DATA.put(payload.uuid(), payload);
            serverMinecarts.put(payload.uuid(),payload.pos());

            if (MinecartVisualizerConfig.mergeStackingMinecartInfo){
                List<UUID> minecarts = new ArrayList<>();
                updateMinecartGroups();
                for (List<UUID> group : minecartGroups) {minecarts.add(group.getFirst());}
                leaderMinecarts = minecarts;
            }

            if (travelTimers != null && travelTimers.containsKey(payload.uuid())){
                if (travelTimers.get(payload.uuid()).hasMoved){
                    travelTimers.get(payload.uuid()).tickCount++;
                }
            }
        }));

        ClientPlayNetworking.registerGlobalReceiver(HopperMinecartDataPayload.ID,
                (payload, context) -> MinecraftClient.getInstance().execute(() -> HOPPER_MINECART_DATA.put(payload.uuid(), payload)));

        ClientPlayNetworking.registerGlobalReceiver(TNTMinecartDataPayload.ID,
                (payload, context) -> MinecraftClient.getInstance().execute(() -> {TNT_MINECART_DATA.put(payload.uuid(), payload);
                if (payload.isExploded() && MinecartVisualizerConfig.trackTNTMinecart){
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    Text headText = Text.literal("[Exploded]").setStyle(Style.EMPTY.withColor(0x8FBF3A));
                    Text posText = Text.literal("At" + payload.explosionPos().toString()).setStyle(Style.EMPTY.withColor(0xDE2E6E));
                    Text attackerText = Text.literal("[Attacker:" + payload.attacker() + "]").setStyle(Style.EMPTY.withColor(0x7EADFF));
                    Text message = headText.copy().append(attackerText).append(posText);
                    if (player != null){player.sendMessage(message, false);}
                }
                }));
    }


    public static MinecartDataPayload getMinecartData(UUID uuid) {
        return MINECART_DATA.get(uuid);
    }

    public static HopperMinecartDataPayload getHopperMinecartData(UUID uuid) {
        return HOPPER_MINECART_DATA.get(uuid);
    }

    public static TNTMinecartDataPayload getTNTMinecartData(UUID uuid) {
        return TNT_MINECART_DATA.get(uuid);
    }
}
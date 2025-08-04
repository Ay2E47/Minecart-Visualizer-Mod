package com.minecartvisualizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static com.minecartvisualizer.MinecartVisualizerClient.*;
import static com.minecartvisualizer.command.MinecartVisualizerCommands.counter;

public class MinecartTrackerTools {

    public static void setNewTimer(UUID uuid, Vec3d destination, ClientPlayerEntity player) {
        travelTimers.put(uuid,new MinecartTimerState(destination,player));
    }

    public static void setNewHopperMinecartTracker(UUID uuid, ClientPlayerEntity player, int number){
        hopperMinecartTrackers.put(number, new HopperMinecartState(player, uuid));
    }

    public static void removeHopperMinecartTracker(int number, ClientPlayerEntity player){
        if (hopperMinecartTrackers.containsKey(number)){
            hopperMinecartTrackers.remove(number);
            if (player != null) {player.sendMessage(Text.literal("Has removed Tracker-" + number),true);}
        }else {
            player.sendMessage(Text.literal("Can't find Tracker-" + number),true);
        }

    }

    public static Vec3d getMinecartPosition(UUID uuid) {
        MinecartDataPayload data = MinecartClientHandler.getMinecartData(uuid);
        if (data != null) {return data.pos();}
        return null;
    }

    public static List<ItemStack> getHopperMinecartInventory(UUID uuid) {
        HopperMinecartDataPayload data = MinecartClientHandler.getHopperMinecartData(uuid);
        if (data != null) {return data.items();}
        return null;
    }


    public static Entity getLookedAtEntity(){
        if (MinecraftClient.getInstance().crosshairTarget != null && MinecraftClient.getInstance().crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) MinecraftClient.getInstance().crosshairTarget;
            return entityHit.getEntity();
        }else{
            return null;
        }
    }

    public static boolean isEntityLoaded(UUID uuid) {

        if (MinecraftClient.getInstance().world == null) {return false;}
        Iterable<Entity> allEntities = MinecraftClient.getInstance().world.getEntities();
        if (allEntities == null){return false;}
        List<UUID> entitiesUUID = new ArrayList<>();
        for (Entity allEntity : allEntities) {
            UUID entityUuid = allEntity.getUuid();
            entitiesUUID.add(entityUuid);
        }
        return entitiesUUID.contains(uuid);
    }

    public static long getGameTime(){
        long gameTime = 0L;
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world != null){
            gameTime = world.getTime();
        }
        return gameTime;
    }

    public static List<UUID> toUUIDList(Map<Integer, HopperMinecartState> stateMap) {
        List<UUID> uuidList = new ArrayList<>();
        for (HopperMinecartState state : stateMap.values()) {
            uuidList.add(state.uuid);
        }
        return uuidList;
    }

    public static int getNextAvailableNumber() {

        for (int i = 1; i < counter; i++) {
            if (!hopperMinecartTrackers.containsKey(i)) {
                return i;
            }
        }
        return counter++;
    }

}

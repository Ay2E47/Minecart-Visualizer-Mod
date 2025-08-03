package com.minecartvisualizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HopperMinecartState {
    public UUID uuid;
    public List<ItemStack> inventory;
    public List<ItemStack> lastInventory;
    public Vec3d pos;
    public ClientPlayerEntity player;
    public long startTime;


    HopperMinecartState(ClientPlayerEntity player, UUID uuid){
        this.uuid = uuid;
        this.player = player;
        pos = null;
        inventory = null;
        lastInventory = null;
        ClientWorld world = MinecraftClient.getInstance().world;
        startTime = 0L;
        if (world != null){startTime = world.getTime();}
    }

    public void updateState(List<ItemStack> inventory, Vec3d pos){
        this.lastInventory = this.inventory;
        this.inventory = inventory;
        this.pos = pos;
    }

    public Map<Integer, Boolean> CheckInventoryChanges(){
        Map<Integer, Boolean> map = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            if (inventory.get(i).getItem() != lastInventory.get(i).getItem() || inventory.get(i).getCount() != lastInventory.get(i).getCount()){
                map.put(i, true);
            }else {
                map.put(i, false);
            }
        }
        return map;
    }

}

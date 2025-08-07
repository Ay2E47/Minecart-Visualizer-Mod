package com.minecartvisualizer;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class MinecartTimerState {
    public Vec3d destination;
    public Vec3d startingPoint;
    public int tickCount;
    public ClientPlayerEntity player;
    public boolean hasMoved;

    public MinecartTimerState(Vec3d destination,ClientPlayerEntity player) {
        this.destination = destination;
        this.tickCount = 0;
        this.player = player;
        hasMoved = false;
        startingPoint = null;
    }
}

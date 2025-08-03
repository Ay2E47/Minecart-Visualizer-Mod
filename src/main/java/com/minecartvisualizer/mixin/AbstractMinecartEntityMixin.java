package com.minecartvisualizer.mixin;

import com.minecartvisualizer.MinecartDataPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.UUID;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends Entity {


    protected AbstractMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void sendMinecartData(CallbackInfo ci) {
        if (!this.getWorld().isClient) {

            ServerWorld serverWorld = (ServerWorld) this.getWorld();

            UUID uuid = this.getUuid();
            Vec3d pos = this.getPos();
            Vec3d velocity = this.getVelocity();
            float yaw = this.getYaw();
            MinecartDataPayload payload = new MinecartDataPayload(uuid,pos,velocity,yaw);
            serverWorld.getPlayers(player -> player.squaredDistanceTo(this) < 32 * 32)
                    .forEach(player -> ServerPlayNetworking.send(player, payload));
        }
    }
}

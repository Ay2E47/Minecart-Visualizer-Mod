package com.minecartvisualizer.mixin.client;


import com.minecartvisualizer.MinecartVisualizerUtils;
import com.minecartvisualizer.config.MinecartVisualizerConfig;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.minecartvisualizer.MinecartVisualizerClient.hopperMinecartTrackers;
import static com.minecartvisualizer.MinecartVisualizerClient.travelTimers;

@Mixin(Entity.class)
public class EntityMixin {

    @Unique
    Entity entity = (Entity)(Object)this;

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void overrideIsGlowing(CallbackInfoReturnable<Boolean> ci) {
        if (MinecartVisualizerConfig.enableMinecartVisualization && MinecartVisualizerConfig.glowingTrackingMinecart){
            if(MinecartVisualizerUtils.toUUIDList(hopperMinecartTrackers).contains(entity.getUuid())){
                ci.setReturnValue(Boolean.TRUE);
            }
        }
    }
}

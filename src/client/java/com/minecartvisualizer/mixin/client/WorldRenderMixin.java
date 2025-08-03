package com.minecartvisualizer.mixin.client;
import com.minecartvisualizer.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.minecartvisualizer.config.MinecartVisualizerConfig;


@Mixin(WorldRenderer.class)
public abstract class WorldRenderMixin {

    @Inject(
            method = "renderEntity",
            at = @At(
                    value = "TAIL"
            )
    )
    private  void renderHopperMinecartInfo(
            Entity entity,
            double cameraX,
            double cameraY,
            double cameraZ,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            CallbackInfo ci
    ) {

            HopperMinecartDataPayload hopperMinecartData = MinecartClientHandler.getHopperMinecartData(entity.getUuid());

    if (!InfoRenderer.shouldRender(entity)){
        if (MinecartVisualizerClient.uuid != null && entity.getUuid().equals(MinecartVisualizerClient.uuid)) {
            hopperMinecartData = MinecartClientHandler.getHopperMinecartData(MinecartVisualizerClient.uuid);
        }else {return;}
    }

    if (hopperMinecartData == null) {return;}

        if (MinecartVisualizerConfig.enableHopperMinecartInventoryDisplay) {
            InfoRenderer.renderInventory(
                    hopperMinecartData,
                    entity,
                    cameraX,
                    cameraY,
                    cameraZ,
                    tickDelta,
                    matrices
            );
        }

        if (MinecartVisualizerConfig.enableHopperMinecartEnableDisplay) {
            InfoRenderer.renderLocked(
                    hopperMinecartData,
                    entity,
                    cameraX,
                    cameraY,
                    cameraZ,
                    matrices,
                    vertexConsumers
            );
        }
    }

}

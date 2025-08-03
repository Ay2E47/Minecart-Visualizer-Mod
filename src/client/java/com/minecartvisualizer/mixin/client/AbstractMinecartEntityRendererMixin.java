package com.minecartvisualizer.mixin.client;

import com.minecartvisualizer.InfoRenderer;
import com.minecartvisualizer.MinecartClientHandler;
import com.minecartvisualizer.MinecartDataPayload;
import com.minecartvisualizer.MinecartVisualizerClient;
import com.minecartvisualizer.config.MinecartVisualizerConfig;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.AbstractMinecartEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.MinecartEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;






@Mixin(AbstractMinecartEntityRenderer.class)
public abstract class AbstractMinecartEntityRendererMixin extends EntityRenderer {

    @Unique
    private AbstractMinecartEntity entity;

    protected AbstractMinecartEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(
            method = "updateRenderState*",
            at = @At("TAIL")
    )
    private void getEntity(AbstractMinecartEntity entity,
                               MinecartEntityRenderState state,
                               float tickDelta,
                               CallbackInfo ci) {
        this.entity = entity;
    }


    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/MinecartEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at =  @At("TAIL")
    )
    public void renderInfo(MinecartEntityRenderState state,
                                   MatrixStack matrices,
                                   VertexConsumerProvider vertexConsumer,
                                   int light,
                                   CallbackInfo ci){


            MinecartDataPayload displayInfo = MinecartClientHandler.getMinecartData(entity.getUuid());

            if (!InfoRenderer.shouldRender(entity)){
                if (MinecartVisualizerClient.uuid != null && entity.getUuid().equals(MinecartVisualizerClient.uuid)) {
                    displayInfo = MinecartClientHandler.getMinecartData(MinecartVisualizerClient.uuid);
                }else {return;}
            }

            if (displayInfo == null){return;}

            if (MinecartVisualizerConfig.enableTextDisplay){
                InfoRenderer.renderTexts(displayInfo, entity, matrices, vertexConsumer);
            }
    }
}
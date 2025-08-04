package com.minecartvisualizer.mixin.client;

import com.minecartvisualizer.*;
import com.minecartvisualizer.config.MinecartVisualizerConfig;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.AbstractMinecartEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.MinecartEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.minecartvisualizer.MinecartVisualizerClient.hopperMinecartTrackers;


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

            List<MutableText> infoTexts = new ArrayList<>();

            if (MinecartVisualizerConfig.enableInfoTextDisplay){
                infoTexts = InfoRenderer.getInfoTexts(displayInfo);
            }


            if (MinecartVisualizerConfig.enableTrackerNumberDisplay && !hopperMinecartTrackers.isEmpty()){
                int number = 0;
                for (Map.Entry<Integer, HopperMinecartState> entry : hopperMinecartTrackers.entrySet()) {
                    if (entry.getValue().uuid == entity.getUuid()){
                        number = entry.getKey();
                    }
                }
                infoTexts.add(Text.literal("Tracker-" + number).setStyle(Style.EMPTY));
            }
            InfoRenderer.renderTexts(infoTexts, entity, matrices, vertexConsumer);
    }
}
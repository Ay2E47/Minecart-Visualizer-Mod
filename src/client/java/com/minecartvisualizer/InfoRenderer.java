package com.minecartvisualizer;

import com.minecartvisualizer.config.MinecartVisualizerConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;


public class InfoRenderer {
    public static boolean getCustomRenderLayer;

    public static void renderTexts(MinecartDataPayload displayInfo, Entity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumer) {

        boolean[] enableFunctions = {
                MinecartVisualizerConfig.enablePosTextDisplay,
                MinecartVisualizerConfig.enableVelocityTextDisplay,
                MinecartVisualizerConfig.enableYawTextDisplay
        };

        ArrayList<MutableText> infoTexts = displayInfo.getInfoTexts(MinecartVisualizerConfig.accuracy, enableFunctions);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        float height = entity.getHeight() + 1;
        float y = 10 - (infoTexts.size() * 10);

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        float cameraYaw = camera.getYaw();
        float cameraPitch = camera.getPitch();

        matrices.push();
        matrices.translate(0.0, height, 0.0);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw + 180));
        if (MinecartVisualizerConfig.alwaysFacingThePlayer) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-cameraPitch));
        }
        matrices.scale(0.03f, -0.03f, 0.03f);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();

        float y1 = y;
        for (Text infoText : infoTexts) {
            float x = -textRenderer.getWidth(infoText) / 2f;
            textRenderer.draw(
                    infoText, x, y1, -2130706433, false,
                    matrix4f, vertexConsumer,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0x4CC8C8C8,
                    0xF000F0
            );
            y1 += 10;
        }


        float y2 = y;
        for (Text infoText : infoTexts) {
            float x = -textRenderer.getWidth(infoText) / 2f;
            textRenderer.draw(
                    infoText, x, y2, 0xFFFFFFFF, false,
                    matrix4f, vertexConsumer,
                    TextRenderer.TextLayerType.NORMAL,
                    0, 0xF000F0
            );
            y2 += 10;
        }

        matrices.pop();

    }

    public static void renderInventory(HopperMinecartDataPayload hopperMinecartData, Entity entity,
                                       double cameraX,
                                       double cameraY, double cameraZ,
                                       float tickDelta,
                                       MatrixStack matrices) {


        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        List<ItemStack> items = hopperMinecartData.items();

        double x = MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
        double y = MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
        double z = MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

        double itemY = y + 0.7;

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        float cameraYaw = camera.getYaw();
        float cameraPitch = camera.getPitch();


        matrices.push();

        matrices.translate(x - cameraX, itemY - cameraY, z - cameraZ);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cameraYaw + 180));
        if (MinecartVisualizerConfig.alwaysFacingThePlayer) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-cameraPitch));
        }

        double XOffset = -1;

        for (int i = 0; i < 5; i++) {
            ItemStack item = items.get(i);

            matrices.push();
            matrices.translate(XOffset, 0.5, 0.0);


            //绘制背景
            VertexConsumer buffer = immediate.getBuffer(RenderLayer.getGui());
            MatrixStack.Entry entry = matrices.peek();
            Matrix4f matrix = entry.getPositionMatrix();
            float length1 = 0.2f;
            float length2 = 0.22f;

            //画框
            buffer.vertex(matrix, -length1, -length1, -0.06f).color(0.53f, 0.53f, 0.53f, 0.6f);
            buffer.vertex(matrix, length1, -length1, -0.06f).color(0.53f, 0.53f, 0.53f, 0.6f);
            buffer.vertex(matrix, length1, length1, -0.06f).color(0.53f, 0.53f, 0.53f, 0.6f);
            buffer.vertex(matrix, -length1, length1, -0.06f).color(0.53f, 0.53f, 0.53f, 0.6f);
            //描边
            buffer.vertex(matrix, -length2, -length2, -0.08f).color(0.863f, 0.863f, 0.863f, 0.6f);
            buffer.vertex(matrix, length2, -length2, -0.08f).color(0.863f, 0.863f, 0.863f, 0.6f);
            buffer.vertex(matrix, length2, length2, -0.08f).color(0.863f, 0.863f, 0.863f, 0.6f);
            buffer.vertex(matrix, -length2, length2, -0.08f).color(0.863f, 0.863f, 0.863f, 0.6f);

            //绘制物品
            matrices.scale(0.38f, 0.38f, 0.38f);
            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

            getCustomRenderLayer = true;
            itemRenderer.renderItem(
                    item,
                    ModelTransformationMode.GUI,
                    0xF000F0,
                    OverlayTexture.DEFAULT_UV,
                    matrices,
                    immediate,
                    null,
                    0

            );
            getCustomRenderLayer = false;

            //绘制堆叠数量
            if (item.getCount() != 1 && MinecartVisualizerConfig.enableItemStackCountDisplay) {
                String countString = item.getCount() + "";
                Text countText = Text.literal(countString);
                matrices.translate(0.23, -0.2, 0.1);
                matrices.scale(0.035f, -0.035f, 0.035f);
                TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                textRenderer.draw(
                        countText,
                        0.0f, 0.0f,
                        0xFFFFFFFF,
                        false, matrix4f, immediate,
                        TextRenderer.TextLayerType.SEE_THROUGH,
                        0,
                        0xF000F0);
            }
            matrices.pop();
            XOffset += 0.5;
        }

        matrices.pop();
    }

    public static void renderLocked(HopperMinecartDataPayload hopperMinecartData, Entity entity,
                                    double cameraX,
                                    double cameraY, double cameraZ,
                                    MatrixStack matrices,
                                    VertexConsumerProvider vertexConsumer) {

        //漏斗矿车上锁可视化
        if (hopperMinecartData.enable()) return;
        Box worldBox = hopperMinecartData.boundingBox();

        Vec3d minecartPos = entity.getPos();
        Vec3d correctedVector = minecartPos.subtract(worldBox.getCenter());
        Box correctedBox = worldBox
                .offset(correctedVector)
                .expand(0.06, 0, 0.02)
                .offset(0, 0.4, 0);

        Box viewBox = correctedBox.offset(-cameraX, -cameraY, -cameraZ);
        VertexConsumer lines = vertexConsumer.getBuffer(RenderLayer.LINES);

        VertexRendering.drawBox(matrices, lines, viewBox, 1.0f, 0.2f, 0.2f, 1.0f);


    }

    public static boolean shouldRender(Entity entity) {

        if (!MinecartVisualizerConfig.enableMinecartVisualization) {
            return false;
        }

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && entity.squaredDistanceTo(player) > MinecartVisualizerConfig.infoRenderDistance * MinecartVisualizerConfig.infoRenderDistance) {
            return false;
        }

        return !MinecartVisualizerConfig.mergeStackingMinecartInfo ||
                MinecartClientHandler.leaderMinecarts.contains(entity.getUuid());
    }
}




package com.minecartvisualizer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.UUID;

public record MinecartDataPayload(UUID uuid,Vec3d pos,Vec3d velocity,float yaw) implements CustomPayload {
    public static final Id<MinecartDataPayload> ID = new CustomPayload.Id<>(MinecartVisualizer.MINECART_DATA_PACKET_ID);

    @Override
    public Id<? extends CustomPayload> getId() {return ID;}

    public static final ValueFirstEncoder<PacketByteBuf, MinecartDataPayload> ENCODER = (payload, buf) -> {
        buf.writeUuid(payload.uuid);
        buf.writeDouble(payload.pos().x);
        buf.writeDouble(payload.pos().y);
        buf.writeDouble(payload.pos().z);
        buf.writeDouble(payload.velocity().x);
        buf.writeDouble(payload.velocity().y);
        buf.writeDouble(payload.velocity().z);
        buf.writeFloat(payload.yaw());
    };

    public static final PacketDecoder<PacketByteBuf, MinecartDataPayload> DECODER = (buf) -> {
        UUID uuid = buf.readUuid();
        double xPos = buf.readDouble();
        double yPos = buf.readDouble();
        double zPos = buf.readDouble();
        Vec3d pos = new Vec3d(xPos, yPos, zPos);
        double xVel = buf.readDouble();
        double yVel = buf.readDouble();
        double zVel = buf.readDouble();
        Vec3d velocity = new Vec3d(xVel, yVel, zVel);
        float yaw = buf.readFloat();


        return new MinecartDataPayload(uuid,pos,velocity,yaw);
    };

    public static final PacketCodec<PacketByteBuf, MinecartDataPayload> CODEC = PacketCodec.of(ENCODER, DECODER);

    public ArrayList<MutableText> getInfoTexts(int accuracy,boolean[] EnableFunctions){
        ArrayList<MutableText> InfoTexts = new ArrayList<>();

        Vec3d rawPos = this.pos();
        if (rawPos == null) {
            InfoTexts.add(Text.translatable("Pos").append(":").append(Text.translatable("unknown")));
        } else {
            Vec3d adjustedPos = FormatTools.truncate(rawPos, accuracy);
            if (EnableFunctions[0]){InfoTexts.add(Text.translatable("Pos").append(": ").append(adjustedPos.toString()));}
        }

        Vec3d rawVelocity = this.velocity();
        if (rawVelocity == null) {
            InfoTexts.add(Text.translatable("Velocity").append(":").append(Text.translatable("unknown")));
        } else {
            Vec3d adjustedVelocity = FormatTools.truncate(rawVelocity, accuracy);
            if (EnableFunctions[1]){InfoTexts.add(Text.translatable("Velocity").append(": ").append(adjustedVelocity.toString()));}
        }

        double rawYaw = this.yaw();
        double adjustedYaw = FormatTools.truncate(rawYaw, accuracy);
        if(EnableFunctions[2]){InfoTexts.add(Text.translatable("Yaw").append(": ").append(String.valueOf(adjustedYaw)));}
        return InfoTexts;
    }


}

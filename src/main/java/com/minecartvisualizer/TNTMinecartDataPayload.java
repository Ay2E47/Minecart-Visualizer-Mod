package com.minecartvisualizer;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record TNTMinecartDataPayload(UUID uuid, int fuseTicks, boolean isExploded, String attacker, Vec3d explosionPos, Float damageWobbleStrength) implements CustomPayload {
    public static final Id<TNTMinecartDataPayload> ID = new CustomPayload.Id<>(MinecartVisualizer.TNT_MINECART_DATA_PACKET_ID);

    @Override
    public Id<? extends CustomPayload> getId() {return ID;}

    public static final ValueFirstEncoder<PacketByteBuf, TNTMinecartDataPayload> ENCODER = (payload, buf) -> {
        buf.writeUuid(payload.uuid);
        buf.writeInt(payload.fuseTicks);
        buf.writeBoolean(payload.isExploded);
        buf.writeString(payload.attacker);
        buf.writeVec3d(payload.explosionPos);
        buf.writeFloat(payload.damageWobbleStrength);

    };

    public static final PacketDecoder<PacketByteBuf, TNTMinecartDataPayload> DECODER = (buf) -> {
        UUID uuid = buf.readUuid();
        int fuseTicks = buf.readInt();
        boolean isExploded = buf.readBoolean();
        String attacker = buf.readString();
        Vec3d explosionPos = buf.readVec3d();
        float damageWobbleStrength = buf.readFloat();
        return new TNTMinecartDataPayload(uuid, fuseTicks, isExploded, attacker, explosionPos, damageWobbleStrength);
    };

    public static final PacketCodec<PacketByteBuf, TNTMinecartDataPayload> CODEC = PacketCodec.of(ENCODER, DECODER);


    public List<MutableText> getInfoTexts(boolean[] enableSettings){
        List<MutableText> infoTexts = new ArrayList<>();
        MutableText damageWobbleStrengthText = Text.literal("Wobble:" + damageWobbleStrength()).setStyle(Style.EMPTY.withColor(0xE61717));
        MutableText fuseTicks = Text.literal("Fuse:" + fuseTicks()).setStyle(Style.EMPTY.withColor(0xE61717));
        if(enableSettings[0]){infoTexts.add(damageWobbleStrengthText);}
        if (enableSettings[1]){infoTexts.add(fuseTicks);}
        return infoTexts;
    }

}

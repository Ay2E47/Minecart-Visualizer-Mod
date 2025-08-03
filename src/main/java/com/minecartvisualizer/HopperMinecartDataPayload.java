package com.minecartvisualizer;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HopperMinecartDataPayload(UUID uuid, boolean enable, Box boundingBox , List<ItemStack> items) implements CustomPayload {
    public static final Id<HopperMinecartDataPayload> ID = new CustomPayload.Id<>(MinecartVisualizer.HOPPER_MINECART_DATA_PACKET_ID);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static final ValueFirstEncoder<PacketByteBuf, HopperMinecartDataPayload> ENCODER = (payload, buf) -> {
        buf.writeUuid(payload.uuid());
        buf.writeBoolean(payload.enable());

        Box box = payload.boundingBox();
        buf.writeDouble(box.minX);
        buf.writeDouble(box.minY);
        buf.writeDouble(box.minZ);
        buf.writeDouble(box.maxX);
        buf.writeDouble(box.maxY);
        buf.writeDouble(box.maxZ);

        List<ItemStack> items = payload.items();
        ItemStack slot1 = items.get(0);
        ItemStack slot2 = items.get(1);
        ItemStack slot3 = items.get(2);
        ItemStack slot4 = items.get(3);
        ItemStack slot5 = items.get(4);

        ItemStack.OPTIONAL_PACKET_CODEC.encode((RegistryByteBuf)buf, slot1);
        ItemStack.OPTIONAL_PACKET_CODEC.encode((RegistryByteBuf)buf, slot2);
        ItemStack.OPTIONAL_PACKET_CODEC.encode((RegistryByteBuf)buf, slot3);
        ItemStack.OPTIONAL_PACKET_CODEC.encode((RegistryByteBuf)buf, slot4);
        ItemStack.OPTIONAL_PACKET_CODEC.encode((RegistryByteBuf)buf, slot5);

    };

    public static final PacketDecoder<PacketByteBuf, HopperMinecartDataPayload> DECODER = (buf) -> {
        UUID uuid = buf.readUuid();
        boolean enable = buf.readBoolean();

        double minX = buf.readDouble();
        double minY = buf.readDouble();
        double minZ = buf.readDouble();
        double maxX = buf.readDouble();
        double maxY = buf.readDouble();
        double maxZ = buf.readDouble();
        Box boundingBox = new Box(minX, minY, minZ, maxX, maxY, maxZ);

        List<ItemStack> items = new ArrayList<>(5);
        items.add(0,ItemStack.OPTIONAL_PACKET_CODEC.decode((RegistryByteBuf)buf));
        items.add(1,ItemStack.OPTIONAL_PACKET_CODEC.decode((RegistryByteBuf)buf));
        items.add(2,ItemStack.OPTIONAL_PACKET_CODEC.decode((RegistryByteBuf)buf));
        items.add(3,ItemStack.OPTIONAL_PACKET_CODEC.decode((RegistryByteBuf)buf));
        items.add(4,ItemStack.OPTIONAL_PACKET_CODEC.decode((RegistryByteBuf)buf));

        return new HopperMinecartDataPayload(uuid,enable,boundingBox,items);
    };
    public static final PacketCodec<PacketByteBuf, HopperMinecartDataPayload> CODEC = PacketCodec.of(ENCODER, DECODER);
}



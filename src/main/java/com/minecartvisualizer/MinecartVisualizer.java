package com.minecartvisualizer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MinecartVisualizer implements ModInitializer {
	public static final String MOD_ID = "minecartvisualizer";
	public static final Identifier MINECART_DATA_PACKET_ID = Identifier.of(MOD_ID,"minecart_data_packet");
	public static final Identifier HOPPER_MINECART_DATA_PACKET_ID = Identifier.of(MOD_ID,"hopper_minecart_data_packet");
	public static final Identifier TNT_MINECART_DATA_PACKET_ID = Identifier.of(MOD_ID,"tnt_minecart_data_packet");
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);



	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playC2S().register(MinecartDataPayload.ID, MinecartDataPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(HopperMinecartDataPayload.ID, HopperMinecartDataPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(TNTMinecartDataPayload.ID,TNTMinecartDataPayload.CODEC);

	}
}
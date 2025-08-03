package com.minecartvisualizer;

import com.minecartvisualizer.command.MinecartVisualizerCommands;
import com.minecartvisualizer.config.MinecartVisualizerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.*;


public class MinecartVisualizerClient implements ClientModInitializer {


	public static Map<UUID, MinecartTimerState> travelTimers = new HashMap<>();
	public static Map<Integer, HopperMinecartState> hopperMinecartTrackers = new HashMap<>();
	private static KeyBinding enableKeyBinding;
	private static KeyBinding displayInfoKeyBinding;
	private static KeyBinding setTrackerBinding;
	public static UUID uuid;

	public void onInitializeClient() {
		MinecartClientHandler.register();
		MinecartVisualizerCommands.registerCommands();

		enableKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Enable",
				InputUtil.Type.KEYSYM,
				InputUtil.UNKNOWN_KEY.getCode(),
				"MinecartVisualizer"));

		displayInfoKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Show Info",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"MinecartVisualizer"));

		setTrackerBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"Set Tracker",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_O,
				"MinecartVisualizer"));



		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (enableKeyBinding.wasPressed()) {
				MinecartVisualizerConfig.enableMinecartVisualization = !MinecartVisualizerConfig.enableMinecartVisualization;
			}

			while (setTrackerBinding.wasPressed()){
				ClientPlayerEntity player = client.player;
				MinecartVisualizerCommands.setNewTracker(MinecartVisualizerCommands.counter++,player);
			}

			if(!MinecartVisualizerConfig.enableMinecartVisualization && displayInfoKeyBinding.isPressed()){
				if (MinecartTrackerTools.getLookedAtEntity() != null){uuid = MinecartTrackerTools.getLookedAtEntity().getUuid();}
			}else {uuid = null;}
		});





		//漏斗矿车追踪器
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!hopperMinecartTrackers.isEmpty()){
				Iterator<Map.Entry<Integer, HopperMinecartState>> iterator = hopperMinecartTrackers.entrySet().iterator();

				while (iterator.hasNext()) {
					Map.Entry<Integer, HopperMinecartState> entry = iterator.next();
					int number = entry.getKey();
					UUID entityUuid = entry.getValue().uuid;
					HopperMinecartState state = entry.getValue();

					long gameTime = MinecartTrackerTools.getGameTime();
					String changeInItem = "Item Changed: %s->%s";
					String changeInCount = " %d->%d ";

					long runTime;
					Text runTimeText = Text.literal("" );
					if (MinecartVisualizerConfig.trackerOutputRuntime){
						runTime = gameTime - state.startTime;
						runTimeText = Text.literal("[gt" + runTime + "]" ).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFD02C)));
					}

					if (!MinecartTrackerTools.isEntityLoaded(entityUuid)) {
						Text unLoadedText = Text.literal("Tracker-"+ entry.getKey() +" be unloaded");
						state.player.sendMessage(runTimeText.copy().append(unLoadedText).formatted(Formatting.RED),false);
						iterator.remove();
						continue;
					}

					List <ItemStack> inventory = MinecartTrackerTools.getHopperMinecartInventory(entityUuid);
					Vec3d pos = MinecartTrackerTools.getMinecartPosition(entityUuid);
					state.updateState(inventory, pos);

					if (state.inventory != null && state.lastInventory != null && state.pos != null){


						Map<Integer, Boolean> map = state.CheckInventoryChanges();
						for (Map.Entry<Integer, Boolean> slot : map.entrySet()){
							if (slot.getValue()) {

								int slotNumber = slot.getKey();
								int fixedSlotNumber = slotNumber+1;



								Text slotText = Text.literal("[" + "Slot" + fixedSlotNumber + "]").formatted(Formatting.GOLD);
								Text itemText;
								Text countText = Text.literal("");

								if (state.inventory.get(slotNumber).getItem() != state.lastInventory.get(slotNumber).getItem()){
									String id1 = state.lastInventory.get(slotNumber).getItem().toString();
									String id2 = state.inventory.get(slotNumber).getItem().toString();
									String itemString = String.format(changeInItem,id1.replace("minecraft:",""),id2.replace("minecraft:",""));
									itemText = Text.literal(" " + itemString).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xE60013)));

								}else {
									String itemString = " " + state.inventory.get(slotNumber).getItem().toString().replace("minecraft:","");
									itemText = Text.literal(itemString).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xBD41F2)));
								}
								if (state.inventory.get(slotNumber).getCount() != state.lastInventory.get(slotNumber).getCount()){
									String countString = String.format(changeInCount,state.lastInventory.get(slotNumber).getCount(), state.inventory.get(slotNumber).getCount());
									if (state.inventory.get(slotNumber).getCount() > state.lastInventory.get(slotNumber).getCount()){
										countText = Text.literal(countString).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7CFC00)));
									}else {
										countText = Text.literal(countString).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF1493)));
									}
								}


								Text posText;
								if (pos != null && MinecartVisualizerConfig.trackerOutputPosition) {
									Vec3d formatedPos = FormatTools.truncate(pos,1);
									posText = Text.literal(formatedPos.toString()).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7EADFF)));
								} else {posText = Text.literal("");}

								Text numberText = Text.literal("[Tracker-" + number + "]").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x3490dE)));
								Text dividingLine = Text.literal(" |").formatted(Formatting.GRAY);
								Text finalText = runTimeText.copy()
										.append(numberText)
										.append(slotText)
										.append(itemText)
										.append(dividingLine)
										.append(countText)
										.append(posText);
								state.player.sendMessage(finalText,false);
							}
						}
					}
                }
			}
		});

		//矿车运动计时器
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (travelTimers != null){
				Iterator<Map.Entry<UUID, MinecartTimerState>> iterator = travelTimers.entrySet().iterator();

				while (iterator.hasNext()){
					Map.Entry<UUID, MinecartTimerState> entry = iterator.next();
					UUID uuid = entry.getKey();
					MinecartTimerState state = entry.getValue();

					if (!MinecartTrackerTools.isEntityLoaded(uuid)) {
						iterator.remove();
						continue;
					}
					Vec3d entityPos = MinecartTrackerTools.getMinecartPosition(uuid);

					if (entityPos == null){return;}
					BlockPos minecartBlockPos = new BlockPos((int) entityPos.x, (int) entityPos.y, (int) entityPos.z);

					BlockPos destinationBlockPos = new BlockPos((int) state.destination.x, (int) state.destination.y, (int) state.destination.z);

					if (minecartBlockPos.equals(destinationBlockPos)) {
						state.player.sendMessage(Text.literal("Move to" + entityPos + "takes" + state.tickCount + "gt"),false);
						iterator.remove();

				}
			}
			}

		});
	}
}
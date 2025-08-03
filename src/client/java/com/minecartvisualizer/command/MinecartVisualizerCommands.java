package com.minecartvisualizer.command;

import com.minecartvisualizer.HopperMinecartState;
import com.minecartvisualizer.MinecartTimerState;
import com.minecartvisualizer.MinecartTrackerTools;
import com.minecartvisualizer.config.MinecartVisualizerConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.minecartvisualizer.MinecartVisualizerClient.hopperMinecartTrackers;
import static com.minecartvisualizer.MinecartVisualizerClient.travelTimers;

public class MinecartVisualizerCommands {
    public static int counter = 1;
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("MinecartVisualizer")
                        .then(ClientCommandManager.literal("tracker")
                                .then(ClientCommandManager.literal("TravelTimer")
                                        .then(ClientCommandManager.argument("x", DoubleArgumentType.doubleArg())
                                                .then(ClientCommandManager.argument("y", DoubleArgumentType.doubleArg())
                                                        .then(ClientCommandManager.argument("z", DoubleArgumentType.doubleArg())
                                                                .executes(context -> {
                                                                    ClientPlayerEntity player = context.getSource().getPlayer();
                                                                    UUID lookedAtUuid = null;
                                                                    if (MinecartTrackerTools.getLookedAtEntity() != null){
                                                                        lookedAtUuid = MinecartTrackerTools.getLookedAtEntity().getUuid();
                                                                    }
                                                                    if (lookedAtUuid == null) {
                                                                        if (player != null && MinecartTrackerTools.getLookedAtEntity() instanceof AbstractMinecartEntity)
                                                                        {player.sendMessage(Text.literal("ERROR:Should face to Minecart entity"),true);}
                                                                        return 0;}

                                                                    double x = DoubleArgumentType.getDouble(context, "x");
                                                                    double y = DoubleArgumentType.getDouble(context, "y");
                                                                    double z = DoubleArgumentType.getDouble(context, "z");

                                                                    Vec3d destination = new Vec3d(x, y, z);

                                                                    MinecartTrackerTools.setNewTimer(lookedAtUuid, destination, player);

                                                                    if (player != null) {player.sendMessage(Text.literal("Set a TravelTimer successful"),true);}
                                                                    return 1;
                                                                })))))
                                .then(ClientCommandManager.literal("HopperMinecartTracker")
                                        .then(ClientCommandManager.literal("Set")
                                                .then(ClientCommandManager.argument("number",IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            ClientPlayerEntity player = context.getSource().getPlayer();
                                                            int number = IntegerArgumentType.getInteger(context, "number");
                                                            return setNewTracker(number,player);
                                                        }))
                                                .executes(context -> {
                                                    ClientPlayerEntity player = context.getSource().getPlayer();
                                                    while (hopperMinecartTrackers.containsKey(counter)){counter++;}
                                                    return setNewTracker(counter++,player);
                                                })
                                        )
                                        .then(ClientCommandManager.literal("Remove")
                                                .then(ClientCommandManager.argument("number",IntegerArgumentType.integer())
                                                        .executes(context -> {
                                                            ClientPlayerEntity player = context.getSource().getPlayer();
                                                            int number = IntegerArgumentType.getInteger(context, "number");
                                                            MinecartTrackerTools.removeHopperMinecartTracker(number, player);
                                                            if (player != null && !(hopperMinecartTrackers.containsKey(number))){
                                                                player.sendMessage(Text.literal("The number has be removed"),true);
                                                                return 0;
                                                            }
                                                            if (player != null) {player.sendMessage(Text.literal("Remove successful"),true);}
                                                            return 1;
                                                        })))
                                        .then(ClientCommandManager.literal("RemoveAllTracker")
                                                .executes(context ->{
                                                    hopperMinecartTrackers.clear();
                                                    ClientPlayerEntity player = context.getSource().getPlayer();
                                                    if (player != null) {player.sendMessage(Text.literal("Remove successful"),true);}
                                                    return 1;
                                                })))
                                .executes(context -> {
                                    List<Text> outputMessages = new ArrayList<>();
                                    TextColor travelTimerColor = TextColor.fromRgb(0x40E0D0);
                                    TextColor hopperTrackerColor = TextColor.fromRgb(0xFFD700);
                                    TextColor detailColor = TextColor.fromRgb(0xFFFFFF);

                                    if (!travelTimers.isEmpty()) {
                                        outputMessages.add(Text.literal("--- Travel Timers ---"));
                                        for (Map.Entry<UUID, MinecartTimerState> entry : travelTimers.entrySet()) {
                                            UUID uuid = entry.getKey();
                                            MinecartTimerState state = entry.getValue();

                                            Vec3d pos = MinecartTrackerTools.getMinecartPosition(uuid);
                                            String posString = (pos != null) ? String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z) : "Known position";

                                            Text head = Text.literal("【TravelTimer】").setStyle(Style.EMPTY.withColor(travelTimerColor));
                                            Text content = Text.empty()
                                                    .append(Text.literal(" | Already tracked " + state.tickCount + " ticks").setStyle(Style.EMPTY.withColor(detailColor)))
                                                    .append(Text.literal(" | At: " + posString).setStyle(Style.EMPTY.withColor(detailColor)));
                                            Text message = Text.empty()
                                                    .append(head)
                                                    .append(content);

                                            outputMessages.add(message);
                                        }
                                    }

                                    if (!hopperMinecartTrackers.isEmpty()) {
                                        outputMessages.add(Text.literal("--- Hopper Trackers ---"));
                                        for (Map.Entry<Integer, HopperMinecartState> entry : hopperMinecartTrackers.entrySet()) {
                                            HopperMinecartState state = entry.getValue();

                                            Vec3d pos = MinecartTrackerTools.getMinecartPosition(state.uuid);
                                            String posString = (pos != null) ? String.format("%.2f, %.2f, %.2f", pos.x, pos.y, pos.z) : "Known position";

                                            long runTime;
                                            long gameTime = MinecartTrackerTools.getGameTime();
                                            runTime = gameTime - state.startTime;
                                            Text head = Text.literal("【HopperMinecartTracker】").setStyle(Style.EMPTY.withColor(hopperTrackerColor));
                                            Text content = Text.literal(" | Number: " + entry.getKey())
                                                    .append(Text.literal(" | Already tracked " + runTime + " ticks").setStyle(Style.EMPTY.withColor(detailColor)))
                                                    .append(Text.literal(" | At: " + posString).setStyle(Style.EMPTY.withColor(detailColor)));
                                            Text message = Text.empty()
                                                    .append(head)
                                                    .append(content);

                                            outputMessages.add(message);
                                        }
                                    }

                                    ClientPlayerEntity player = context.getSource().getPlayer();
                                    for (Text message : outputMessages){
                                        if (player != null) {player.sendMessage(message,false);}
                                    }

                                    if (outputMessages.isEmpty()) {
                                        player.sendMessage(Text.literal("There are currently no minecart being tracked."),true);
                                            return 0;
                                    }
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("function")
                                .then(ClientCommandManager.literal("TextDisplay")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "enableTextDisplay", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("PosDisplay")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "enablePosTextDisplay", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("VelocityDisplay")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "enableVelocityTextDisplay", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("YawDisplay")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "enableYawTextDisplay", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("InventoryDisplay")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "enableHopperMinecartInventoryDisplay", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("LockedDisplay")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "enableHopperMinecartEnableDisplay", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("itemStackCountDisplay")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "enableItemStackCountDisplay", BoolArgumentType.getBool(context, "value")))))
                        )
                        .then(ClientCommandManager.literal("option")
                                .then(ClientCommandManager.literal("AlwaysFacingThePlayer")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "alwaysFacingThePlayer", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("MergeStackingMinecartInfo")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "mergeStackingMinecartInfo", BoolArgumentType.getBool(context, "value")))))
                                .then(ClientCommandManager.literal("TrackerOutputRunTime")
                                        .then(ClientCommandManager.argument("value",BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "trackerOutputRuntime",BoolArgumentType.getBool(context,"value")))))
                                .then(ClientCommandManager.literal("TrackerOutputPosition")
                                        .then(ClientCommandManager.argument("value",BoolArgumentType.bool())
                                                .executes(context -> setBooleanSetting(context.getSource(), "trackerOutputPosition",BoolArgumentType.getBool(context,"value")))))
                                .then(ClientCommandManager.literal("GlowingTrackingMinecart")
                                        .then(ClientCommandManager.argument("value", BoolArgumentType.bool()))
                                )
                        )
                        .then(ClientCommandManager.literal("argument")
                                .then(ClientCommandManager.literal("InfoRenderDistance")
                                        .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 32))
                                                .executes(context -> setIntegerSetting(context.getSource(), "infoRenderDistance", IntegerArgumentType.getInteger(context, "value")))))
                                .then(ClientCommandManager.literal("Accuracy")
                                        .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 16))
                                                .executes(context -> setIntegerSetting(context.getSource(), "accuracy", IntegerArgumentType.getInteger(context, "value")))))
                        )
                        .executes(context -> toggleBooleanSetting(context.getSource(), "enableMinecartVisualization"))
        ));

    }

    private static int toggleBooleanSetting(FabricClientCommandSource source, String settingName){

        if (settingName.equals("enableMinecartVisualization")) {MinecartVisualizerConfig.enableMinecartVisualization = !MinecartVisualizerConfig.enableMinecartVisualization;}
        source.sendFeedback(Text.literal("§a[MinecartVisualizer] Configure §f" + settingName + " to §e" + MinecartVisualizerConfig.enableMinecartVisualization));
        return 1;
    }

    private static int setBooleanSetting(FabricClientCommandSource source, String settingName, boolean value) {
        boolean success = true;
        switch (settingName) {
            case "enableMinecartVisualization":
                MinecartVisualizerConfig.enableMinecartVisualization = value;
                break;
            case "enableTextDisplay":
                MinecartVisualizerConfig.enableTextDisplay = value;
                break;
            case "enablePosTextDisplay":
                MinecartVisualizerConfig.enablePosTextDisplay = value;
                break;
            case "enableVelocityTextDisplay":
                MinecartVisualizerConfig.enableVelocityTextDisplay = value;
                break;
            case "enableYawTextDisplay":
                MinecartVisualizerConfig.enableYawTextDisplay = value;
                break;
            case "enableHopperMinecartEnableDisplay":
                MinecartVisualizerConfig.enableHopperMinecartEnableDisplay = value;
                break;
            case "enableHopperMinecartInventoryDisplay":
                MinecartVisualizerConfig.enableHopperMinecartInventoryDisplay = value;
                break;
            case "enableItemStackCountDisplay":
                MinecartVisualizerConfig.enableItemStackCountDisplay = value;
                break;
            case "mergeStackingMinecartInfo":
                MinecartVisualizerConfig.mergeStackingMinecartInfo = value;
                break;
            case "alwaysFacingThePlayer":
                MinecartVisualizerConfig.alwaysFacingThePlayer = value;
                break;
            case "trackerOutputRuntime":
                MinecartVisualizerConfig.trackerOutputRuntime = value;
                break;
            case "trackerOutputPosition":
                MinecartVisualizerConfig.trackerOutputPosition = value;
                break;
            case "glowingTrackingMinecart":
                MinecartVisualizerConfig.glowingTrackingMinecart = value;
            default:
                success = false;

        }
        if (success) {
            source.sendFeedback(Text.literal("§a[MinecartVisualizer] Configure §f" + settingName + " to: §e" + value));
        } else {
            source.sendError(Text.literal("§c[MinecartVisualizer] Unknown configuration: " + settingName));
        }
        return 1;
    }

    private static int setIntegerSetting(FabricClientCommandSource source, String settingName, int value) {
        boolean success = true;
        switch (settingName) {
            case "infoRenderDistance":
                MinecartVisualizerConfig.infoRenderDistance = value;
                break;
            case "accuracy":
                MinecartVisualizerConfig.accuracy = value;
                break;
            default:
                success = false;
                break;
        }

        if (success) {
            source.sendFeedback(Text.literal("§a[MinecartVisualizer] Configure §f" + settingName + " Set: §e" + value));
        } else {
            source.sendError(Text.literal("§c[MinecartVisualizer] Unknown configuration: " + settingName));
        }
        return 1;
    }

    public static int setNewTracker(int number, ClientPlayerEntity player){
        UUID lookedAtUuid = null;
        if (MinecartTrackerTools.getLookedAtEntity() != null){
            lookedAtUuid = MinecartTrackerTools.getLookedAtEntity().getUuid();
        }
        if (lookedAtUuid == null || !(MinecartTrackerTools.getLookedAtEntity() instanceof HopperMinecartEntity)) {
            if (player != null)
            {player.sendMessage(Text.literal("ERROR:Should face to HopperMinecart entity"),true);}
            return 0;}

        if (hopperMinecartTrackers.containsKey(number)){
            player.sendMessage(Text.literal("The number has be used"),true);
            return 0;
        }

        MinecartTrackerTools.setNewHopperMinecartTracker(lookedAtUuid, player, number);

        if (player != null) {player.sendMessage(Text.literal("Set Tracker-" + number + " successful"),true);}
        return 1;
    }
}

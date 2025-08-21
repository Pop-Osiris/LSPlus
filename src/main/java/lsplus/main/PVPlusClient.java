package lsplus.main;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static lsplus.main.PVDataManager.savedPlayerVaults;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import static lsplus.main.DimensionCheck.checkDimension;


public class PVPlusClient implements ClientModInitializer {

    public static final KeyBinding menuKeybind = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                    "Open PV Screen", // translation key for binding
                    InputUtil.Type.KEYSYM, // KEYSYM is keyboard binding
                    GLFW.GLFW_KEY_N, // default key
                    "Lightskies Plus" // translation key category name
            )
    );

    final String LSPPrefix = "§7§l[§6§lL§e§lS§f§l+§7§l]§r ";

    // ts resets in restart cause theres no config file yet oopsies :3
    private static int tickCounter = 0;
    private static int alertMessage = 0;

    public static boolean doPillagerMessage = false;
    public static boolean doDimensionCheck = false;
    public static boolean doAutoRaffle = true;


    private boolean shouldLoadVaults = false; // do not change

    @Override
    public void onInitializeClient() {

        // PV Data handler
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            shouldLoadVaults = true;
            System.out.println("Set flag to load vault data on next client ticks.");
            if (client != null && client.world != null) {
                System.out.println("PV Data Initializing..");
                PVDataManager.initializePVsForStartup();
            }
            // else {
             //   System.err.println("attempted to load vault data on join, but client or world was null.");
            //}
        });

        // PV Data handler
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            if (client != null && client.world != null) {
                PVDataManager.savePVsForShutdown();
                System.out.println("Saved vault data on disconnecting from server.");
                shouldLoadVaults = false; // reset on disconnect to prevent accidental loads bro im crashingggg
            } else {
                System.err.println("Attempted to save vault data on disconnect, but client or world was null.");
            }
        });



        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("pillagerleadermessage")
                    .executes(context -> {
                        doPillagerMessage = !doPillagerMessage;
                        if (doPillagerMessage) {
                            context.getSource().sendFeedback(Text.literal(LSPPrefix + "You will now announce when a pillager leader has spawned."));
                            doDimensionCheck = true;
                        } else {
                            context.getSource().sendFeedback(Text.literal(LSPPrefix + "You will no longer announce when a pillager leader has spawned."));
                            doDimensionCheck = false;
                        }
                        return 1;
            }));

            dispatcher.register(literal("autoraffle")
                    .executes(context -> {
                        doAutoRaffle = !doAutoRaffle;
                        if (doAutoRaffle) {
                            context.getSource().sendFeedback(Text.literal(LSPPrefix + "You will now enter raffles automatically."));
                        } else {
                            context.getSource().sendFeedback(Text.literal(LSPPrefix + "You will no longer enter raffles automatically."));
                        }
                        return 1;
                    }));


//            dispatcher.register(literal("savetest")
//                    .executes(context -> {
//                        PVDataManager.savePVData();
//                        return 1;
//                    }));
        });


         ClientTickEvents.END_CLIENT_TICK.register(client -> {
             // PV Data handler
             if (shouldLoadVaults && client != null && client.world != null) {
                 System.out.println("PV Data Initializing..");
                 PVDataManager.initializePVsForStartup();
                 System.out.println("Loaded PV data");
                 shouldLoadVaults = false;
             }


             if (menuKeybind.wasPressed()) {
                 // System.out.println("Menu Keybind Pressed!");
                 MinecraftClient.getInstance().setScreen(new PVSelectionScreen(savedPlayerVaults));
             }

             if (doDimensionCheck) {
                 tickCounter++;
                 if (tickCounter >= 20) {
                     tickCounter = 0;
                     // update this and the function so it can be used in if statement here
                     checkDimension("afk", "deepdark");
                 }
             }
         });

        MinecraftClient client = MinecraftClient.getInstance();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String chatMessage = message.getString();
            if (doPillagerMessage) {
                if (chatMessage.equals("[ᴍᴏʙꜱ] • The Dark Pillager Leader has risen!")) {
                    System.out.println("Received chat message: " + chatMessage);
                    assert client.player != null;
                    alertMessage++;

                    // IM SOSOSOSOS SORRY IF ANYONE READS THE SOURCE CODE AND FINDS THIS OMG THIS IS SOME SLOP YANDERE DEV CODE I KNOW PLEASE DONT TELL ME

                    if (alertMessage == 1) {
                        client.getNetworkHandler().sendChatMessage("[BossAlert] a pillager leader spawned if someone wants to come kill it :3");
                    }
                    if (alertMessage == 2) {
                        client.getNetworkHandler().sendChatMessage("[BossAlert] Wow a think I see a certain boss over here...");
                    }
                    if (alertMessage == 3) {
                        client.getNetworkHandler().sendChatMessage("[BossAlert] bro im so sick of these pillager leaders that keep showing up, can someone kill it for me?");
                    }
                    if (alertMessage == 4) {
                        client.getNetworkHandler().sendChatMessage("[BossAlert] theres a free ominous banner somewhere in deep dark rn!");
                    }
                    if (alertMessage == 5) {
                        client.getNetworkHandler().sendChatMessage("[BossAlert] theres this pillager leader guy in the deep dark dimension someone has to kill");
                    }
                    if (alertMessage >= 6) {
                        client.getNetworkHandler().sendChatMessage("[BossAlert] theres a pillager leader here come kill this guy bruh");
                        alertMessage = 0;
                    }
                }
            }

            if (doAutoRaffle) {
                if (chatMessage.contains("\uD83C\uDFAB A RAFFLE HAS STARTED! \uD83C\uDFAB")) {
                    client.getNetworkHandler().sendCommand("raffle enter");
                }
            }

        });
    }
}


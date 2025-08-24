package lsplus.main;

import lsplus.main.util.SendClientMessage;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class SynchronizePVs {
    private boolean isPVSyncing;
    private boolean reachedMaxPVs;
    private int currentPV = 0;
    private int tickCounter = 0;
    private int currentSyncId = -1; // inventory SyncId for the screen id shit cause minecraft is weird
    private final int DELAY_TICKS = 20;


    private boolean screenOpenedAfterInit = false;
    private int screenOpenTickCounter = 0;
    private final int SCREEN_OPEN_DELAY = 5;


    public void startPVSyncing() {
        if (isPVSyncing) return;
        isPVSyncing = true;
        System.out.println("PV Syncing Started");
        SendClientMessage.sendMessage(true, "Sit tight while we sync data. This process only takes a minute, and is critical for the mod's functionality when using the mod for the first time.");

        // Listens for when HandledScreen is initialized and
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (isPVSyncing && screen instanceof HandledScreen) {
                HandledScreen<?> handledScreen = (HandledScreen<?>) screen;
                currentSyncId = handledScreen.getScreenHandler().syncId;
                //System.out.println("HandledScreen opened. SyncID: " + currentSyncId);

                screenOpenTickCounter = SCREEN_OPEN_DELAY;
                screenOpenedAfterInit = true;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isPVSyncing) {

                if (screenOpenedAfterInit) {
                    screenOpenTickCounter--;
                    if (screenOpenTickCounter <= 0) {
                        screenOpenedAfterInit = false;
                        closePVScreen();
                    }
                }

                // counter for time outside inventory.
                if (!reachedMaxPVs && client.currentScreen == null) {
                    if (tickCounter < DELAY_TICKS) {
                        tickCounter++;
                    } else {
                        tickCounter = 0;
                        sendNextPVCommand();
                    }
                }
            }
        });

        // listener for the error message, which indicated reached max pvs
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (isPVSyncing) {
                String chatMessage = message.getString();
                if (chatMessage.equals("[ʟɪɢʜᴛѕᴋɪᴇѕ] • You don't have permission for that!")) {
                    reachedMaxPVs = true;
                    isPVSyncing = false;
                    System.out.println("SynchronizePVs: PV Syncing completed with Total PVs synced: " + (currentPV - 1));
                    SendClientMessage.sendMessage(true,"PV Syncing completed with Total PVs synced: " + (currentPV - 1));
                    SendClientMessage.sendMessage(true,"You can run this again at any time by running /sync-pvs!");
                }
            }
        });

        // Start the open pv process
        sendNextPVCommand();
    }

    private void sendNextPVCommand() {
        if (reachedMaxPVs) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            currentPV += 1;
            client.getNetworkHandler().sendCommand("pv " + currentPV);
            //System.out.println("Attempting to open PV: " + currentPV);
        }
    }

    private void closePVScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null && currentSyncId != -1) {

            // this is the same system in HandledScreenCloseMixin that saves the contents of the pv to PVDataManager map
            Screen currentScreen = client.currentScreen;
            if (currentScreen instanceof HandledScreen<?> handledScreen) {
                ScreenHandler closedHandler = handledScreen.getScreenHandler();

                if (closedHandler instanceof GenericContainerScreenHandler containerHandler) {
                    Text title = handledScreen.getTitle();
                    String inventoryName = title.getString();
                    // System.out.println("Closing inventory with name: " + inventoryName);

                    if (inventoryName.startsWith("Vault #")) {
                        List<ItemStack> currentContainerContents = new ArrayList<>();
                        int chestInventorySize = containerHandler.getInventory().size();

                        for (int i = 0; i < chestInventorySize; i++) {
                            ItemStack stack = containerHandler.getSlot(i).getStack();
                            currentContainerContents.add(stack);
                        }

                        //System.out.println("PV Successfully Saved: " + inventoryName + ", Size: " + currentContainerContents.size());
                        PVDataManager.savePlayerVaultContents(inventoryName, currentContainerContents);
                        PVDataManager.savePVData();
                    }
                }
            }
            // Send the close packet to server with the syncId so the inv closes on server
            client.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(currentSyncId));
            //System.out.println("SynchronizePVs: Manually sent CloseHandledScreenC2SPacket for SyncID: " + currentSyncId);

            //  call setScreen(null) to handle the client-side state
            client.setScreen(null);
            currentSyncId = -1; // Clear syncId after sending the packets and close menu
        }
    }
}
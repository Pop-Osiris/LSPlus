package lsplus.main.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(net.minecraft.client.gui.screen.ingame.HandledScreens.class)
public class HandledScreensMixin {

    private static List<ItemStack> pvContents = new ArrayList<>();

    @Inject(at = @At("TAIL"), method = "open")
    private static <T extends ScreenHandler> void open(ScreenHandlerType<T> type, MinecraftClient client, int id, Text title, CallbackInfo ci) {
        String inventoryName = title.getString();
        // System.out.println("Opening inventory with name: " + inventoryName);


            if (inventoryName.contains("Vault #")) {
            pvContents.clear();

            if (client.player != null && client.player.currentScreenHandler != null) {
                ScreenHandler currentHandler = client.player.currentScreenHandler;
                if (currentHandler instanceof GenericContainerScreenHandler containerHandler) {

                    Inventory pvInventory = containerHandler.getInventory();

                    for (int i = 0; i < pvInventory.size(); i++) {
                        ItemStack stack = pvInventory.getStack(i);
                        if (!stack.isEmpty()) {
                            pvContents.add(stack);
                        }
                    }
                }
            }
            // System.out.println("Chest contents have been saved! Size: " + chestContents.size());
        }

    }
}
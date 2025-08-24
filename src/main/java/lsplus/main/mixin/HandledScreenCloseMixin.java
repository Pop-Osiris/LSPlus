package lsplus.main.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen; // target
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import lsplus.main.PVDataManager;
import java.util.ArrayList;
import java.util.List;

@Mixin(HandledScreen.class)
public class HandledScreenCloseMixin {

    @Inject(at = @At("TAIL"), method = "close") // inject at the end of the close method
    private void onClose(CallbackInfo ci) {
        HandledScreen<?> handledScreen = (HandledScreen<?>) (Object) this; // cast this to the target class
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
                    // if (!stack.isEmpty()) {} // not needed anymore because saving is fixed
                        currentContainerContents.add(stack);

                }
                System.out.println("PV Successfully Saved: " + inventoryName);
                // System.out.println("PV Saved: " + inventoryName + " Contents List: " + currentContainerContents);
                PVDataManager.savePlayerVaultContents(inventoryName, currentContainerContents);
                PVDataManager.savePVData();
                // System.out.println("chest contents saved with size " + chestContents.size());

            }
        } else {
            System.out.println("Closed screen was not a generic container (e.g., chest).");
        }
    }
}
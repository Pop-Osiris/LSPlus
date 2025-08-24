package lsplus.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import lsplus.main.util.Rect;

import org.lwjgl.glfw.GLFW; // needed for mouse clicks


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PVSelectionScreenRenderer {
    private final MinecraftClient client;
    private final HashMap<String, List<ItemStack>> itemMap;
    private final List<List<Integer>> vaultRows; // stores vault numbers grouped into rows
    private final int x, y, width, height; // set bounds of the scrollable area
    private int scrollOffset = 0; // Current scroll position offset
    private final int entryHeight; // Height of each row of vaults
    private int totalContentHeight; // Total height of all vault rows if fully visible
    private final List<Rect> clickableRegions = new ArrayList<>();

    private final HashMap<String, List<ItemStack>> originalItemMap;


    public PVSelectionScreenRenderer(MinecraftClient client, HashMap<String, List<ItemStack>> itemMap, int x, int y, int width, int height) {
        this.client = client;
        this.itemMap = new HashMap<>(itemMap);
        this.originalItemMap = new HashMap<>(itemMap);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;



        int gridHeight = 6;
        int cellSize = 18;
        int fontHeight = client.textRenderer.fontHeight;
        int spaceForLabelAndPadding = fontHeight + 5;
        this.entryHeight = (gridHeight * cellSize) + spaceForLabelAndPadding; // Height of one row of vaults

        // Group vaults into rows
        this.vaultRows = groupVaultsIntoRows(itemMap);

        // Calculate total content height
        this.totalContentHeight = this.vaultRows.size() * this.entryHeight;
    }

    public void emptyPVMenu() {

    }

    private List<List<Integer>> groupVaultsIntoRows(HashMap<String, List<ItemStack>> map) {
        List<Integer> vaultNumbers = new ArrayList<>();
        for (String key : map.keySet()) {
            try {
                String[] parts = key.split("#");
                if (parts.length > 1) {
                    vaultNumbers.add(Integer.parseInt(parts[1]));
                }
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Warning: Invalid vault key format: " + key + ". Error: " + e.getMessage());
            }
        }
        java.util.Collections.sort(vaultNumbers);

        List<List<Integer>> rows = new ArrayList<>();

        // Change how many entries there are on a row
        int gridsPerRow = 3;

        List<Integer> currentRow = new ArrayList<>();
        for (int vaultNum : vaultNumbers) {
            currentRow.add(vaultNum);
            if (currentRow.size() == gridsPerRow) {
                rows.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }
        if (!currentRow.isEmpty()) {
            rows.add(currentRow); // Add any remaining vaults in the last row
        }
        return rows;
    }

    public void filterVaults(String query) {
        // new arraylist
        // loop through original hashmap
        // check if the hashmap key contains the var query
        List<Integer> pvNumbers = new ArrayList<>();

        for (Map.Entry<String, List<ItemStack>> entry : originalItemMap.entrySet()) {

            // loop thru hash and get the string of content names from key
            String pvKey = entry.getKey();
            List<ItemStack> items = entry.getValue();
            // System.out.println("PV " + pvKey + " Content names: ");
            for (ItemStack stack : items) {
                String itemName = stack.getName().getString();
                // System.out.println(itemName);
            }



            // check pv name with query and pass if true
            if (pvKey.toLowerCase().contains(query.toLowerCase())) {
                try {
                    String[] parts = pvKey.split("#");
                    if (parts.length > 1) {
                        pvNumbers.add(Integer.parseInt(parts[1]));
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.err.println("Warning: Invalid vault key format in filter: " + pvKey + ". Error: " + e.getMessage());
                }
            }
            // compare each item name with query and pass if true
            else {
                boolean itemMatches = false;
                for (ItemStack stack : items) {
                    if (stack.getName().getString().toLowerCase().contains(query.toLowerCase())) {
                        itemMatches = true;
                        break;
                    }
                }
                if (itemMatches) {
                    try {
                        String[] parts = pvKey.split("#");
                        if (parts.length > 1) {
                            pvNumbers.add(Integer.parseInt(parts[1]));
                        }
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        System.err.println("Warning: Invalid vault key format in item filter: " + pvKey + ". Error: " + e.getMessage());
                    }
                }
            }
        }
        java.util.Collections.sort(pvNumbers); // sort so it can be ordered right

        // basically just rebuild same shit as before but for filtered one
        this.vaultRows.clear();
        int gridsPerRow = 3;
        List<Integer> currentRow = new ArrayList<>();
        for (int vaultNum : pvNumbers) {
            currentRow.add(vaultNum);
            if (currentRow.size() == gridsPerRow) {
                this.vaultRows.add(new ArrayList<>(currentRow));
                currentRow.clear();
            }
        }
        if (!currentRow.isEmpty()) {
            this.vaultRows.add(currentRow);
        }
        // update ts as well after filtering
        this.totalContentHeight = this.vaultRows.size() * this.entryHeight;
        this.scrollOffset = 0; // reset scroll offset cause i dont like being in the void
    }




    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.enableScissor(this.x, this.y, this.x + this.width, this.y + this.height); // clips everything outside of screen for optimization !

        clickableRegions.clear();

        int currentY = this.y - scrollOffset;

        for (int rowIndex = 0; rowIndex < vaultRows.size(); rowIndex++) {
            List<Integer> vaultsInRow = vaultRows.get(rowIndex);
            int rowY = currentY + rowIndex * entryHeight;

            if (rowY + entryHeight >= this.y && rowY <= this.y + this.height) {
                int gridWidth = 9;
                int cellSize = 18;
                int gridSpacingX = 10;

                int totalRowWidth = 0;
                for (int i = 0; i < vaultsInRow.size(); i++) {
                    totalRowWidth += (gridWidth * cellSize);
                    if (i < vaultsInRow.size() - 1) {
                        totalRowWidth += gridSpacingX;
                    }
                }

                // get the centered starting position
                //int startingX = startingX;
                int vaultX = this.x + (this.width - totalRowWidth) / 2;

//                System.out.println("startingX: " + startingX);
//                System.out.println("this.width:" + this.width);
//                System.out.println("totalRowWidth: " + totalRowWidth);
//                System.out.println("this.x: " + this.x);

                for (int i = 0; i < vaultsInRow.size(); i++) {
                    int vaultNum = vaultsInRow.get(i);
                    String vaultKey = "Vault #" + vaultNum;
                    List<ItemStack> items = itemMap.get(vaultKey);

                    int vaultContentWidth = gridWidth * cellSize;
                    int vaultContentHeight = 6 * cellSize + client.textRenderer.fontHeight + 5;

                    Rect vaultRect = new Rect(vaultX, rowY, vaultContentWidth, vaultContentHeight, vaultNum);
                    clickableRegions.add(vaultRect);

                    if (vaultRect.contains(mouseX, mouseY)) {
                        context.fill(vaultX, rowY, vaultX + vaultContentWidth, rowY + vaultContentHeight, 0x20FFFFFF);
                    }
                    if (items == null || items.isEmpty()) {
                        vaultX += (gridWidth * cellSize + gridSpacingX);
                        continue;
                    }

                    Text vaultLabel = Text.of(vaultKey);
                    int labelWidth = client.textRenderer.getWidth(vaultLabel);
                    context.drawText(client.textRenderer, vaultLabel, vaultX + (gridWidth * cellSize - labelWidth) / 2, rowY, 0xFFFFFF, true);

                    int gridStartY = rowY + client.textRenderer.fontHeight + 5;

                    int itemIndex = 0;
                    for (ItemStack stack : items) {
                        int col = itemIndex % gridWidth;
                        int row = itemIndex / gridWidth;

                        if (row >= 6) break;

                        int itemX = vaultX + col * cellSize;
                        int itemY = gridStartY + row * cellSize;

                        context.drawItem(stack, itemX, itemY);

                        if (stack.getCount() > 1) {
                            String count = String.valueOf(stack.getCount());
                            context.getMatrices().push();
                            float scale = 1f;
                            context.getMatrices().scale(scale, scale, 1.0f);
                            context.getMatrices().translate(0.0f, 0.0f, 5000.0f);
                            float scaledX = (itemX + cellSize - (client.textRenderer.getWidth(count) * scale) / scale - 1) / scale;
                            float scaledY = (itemY + cellSize - client.textRenderer.fontHeight * scale / scale - 1) / scale;
                            context.drawText(client.textRenderer, count, (int) scaledX, (int) scaledY, 0xFFFFFF, true);
                            context.getMatrices().pop();
                        }
                        itemIndex++;
                    }
                    vaultX += (gridWidth * cellSize + gridSpacingX);
                }
            }
        }
        context.disableScissor(); // Disable scissor test after drawing
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            for (int i = 0; i < clickableRegions.size(); i++) {
                Rect bounds = clickableRegions.get(i);
                if (bounds.contains(mouseX, mouseY)) {

                    // find what pv the mouse is hovering over and set to var
                    int clickedVaultNum = bounds.vaultNumber;

                    // kinda redundant and was causing a bug
                    // Iterate through vaultRows to find the clicked vault
//                    int currentVaultCount = 0;
//                    for (List<Integer> row : vaultRows) {
//                        for (int vaultNumInRow : row) {
//                            if (currentVaultCount == i) { // If the index matches
//                                clickedVaultNum = vaultNumInRow;
//                                break;
//                            }
//                            currentVaultCount++;
//                        }
//                        if (clickedVaultNum != -1) break;
//                    }

                    if (clickedVaultNum != -1) {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) {
                            client.getNetworkHandler().sendCommand("pv " + clickedVaultNum);
                            client.setScreen(null);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        // only allow scrolling if the mouse is within the list area
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            int scrollAmount = (int) (amount * 22); // scroll speed

            // update scrollOffset to clamp that it within valid bounds so canmt scroll past top or bottom of page
            scrollOffset -= scrollAmount;
            scrollOffset = Math.max(0, scrollOffset);
            scrollOffset = Math.min(scrollOffset, totalContentHeight - height);

            return true;
        }
        return false;
    }
}
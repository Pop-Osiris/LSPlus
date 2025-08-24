package lsplus.main;

import lsplus.main.util.ClickableTextWidget;
import lsplus.main.SynchronizePVs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.List;



import static lsplus.main.PVDataManager.firstTimeInitialize;
import static lsplus.main.PVDataManager.savedPlayerVaults;

public class PVSelectionScreen extends Screen {

    private final HashMap<String, List<ItemStack>> itemMap;
    private PVSelectionScreenRenderer vaultsRenderer;
    private TextFieldWidget searchBox;
    private ClickableWidget firstInstallPage;

    public PVSelectionScreen(HashMap<String, List<ItemStack>> itemMap) {
        super(Text.of("Player Vaults"));
        this.itemMap = itemMap;
    }

    @Override
    protected void init() {
        super.init();

        // constructor for the entries and scroll and stuff
        // ts currently takes full screen basically
        int listX = 0;
        int listY = 32; // not all the way to the top so i can fit shit up there like search box
        int listWidth = this.width;
        int listHeight = this.height - 32; // little bit of room on bottom cause it may be annoying

        if (this.client != null) {
            this.vaultsRenderer = new PVSelectionScreenRenderer(this.client, this.itemMap, listX, listY, listWidth, listHeight);
        }

        //System.out.println(this.itemMap);
        //System.out.println(firstTimeInitialize);
        if (this.itemMap.isEmpty()) {

            //System.out.println("First Time Initialize should start");

            // add a new clickable text for first time initialization/empty menu shit
            int textX = 0;
            int textY = this.height / 2 - 10;
            int textWidth = this.width;
            int textHeight = 20;

            Text initializationText = Text.of("hmm.. it seems to be a bit empty here. Click here to sync PVs!");
            int textColor = 0xFFFFFF;

            // create runnable action for when clicked
            Runnable initializeAction = () -> {
                //System.out.println("Player Vaults Initialized!");
                new SynchronizePVs().startPVSyncing();
                firstTimeInitialize = false;
                this.close();
            };

            ClickableTextWidget initializeButton = new ClickableTextWidget(textX, textY, textWidth, textHeight, initializationText, textColor, () -> initializeAction);
            this.addDrawableChild(initializeButton);
        }


        // constructor for search bar
        int searchBarWidth = 200;
        int searchBarHeight = 20;
        int searchBarX = (this.width + searchBarWidth) / 2; // x position of search bar
        int searchBarY = 5;

        this.searchBox = new TextFieldWidget(this.client.textRenderer, searchBarX, searchBarY, searchBarWidth, searchBarHeight, Text.literal("Search Item Name or PV Number"));
        this.searchBox.setMaxLength(30);
        this.searchBox.setDrawsBackground(true);
        this.searchBox.setFocusUnlocked(false);
        this.searchBox.setEditableColor(0xFFFFFF);
        this.searchBox.setPlaceholder(Text.literal("Search Item Name or PV Number"));

        this.searchBox.setChangedListener(this::onSearchQueryChanged);

        this.addDrawableChild(this.searchBox);

    }

    private void onSearchQueryChanged(String query) {
        this.vaultsRenderer.filterVaults(query);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta); // render the background first RAH
        context.fill(0, 0, this.width, this.height, 0x99000000); // background hhh
        this.vaultsRenderer.render(context, mouseX, mouseY, delta); // Render the custom list
        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 10, 0xFFFFFF); // Render the screen title
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.searchBox); // set search box as focused
            return true;
        }

        if (this.vaultsRenderer.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(null); // if a pv was clicked set to null cause it will close menu anyways
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {

        if (this.searchBox.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    // Override ts so game can handle scrolling
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.vaultsRenderer.mouseScrolled(mouseX, mouseY, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }
}
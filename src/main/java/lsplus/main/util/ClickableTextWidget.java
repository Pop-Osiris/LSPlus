package lsplus.main.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class ClickableTextWidget extends ClickableWidget {

    private final Text text;
    private final int color;
    private final Supplier<Runnable> clickAction; // uses supplier for the click action

    public ClickableTextWidget(int x, int y, int width, int height, Text text, int color, Supplier<Runnable> clickAction) {
        super(x, y, width, height, text);
        this.text = text;
        this.color = color;
        this.clickAction = clickAction;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawCenteredTextWithShadow(textRenderer, this.text, this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - textRenderer.fontHeight) / 2, this.color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.clickAction.get().run(); // run the provided action on click
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
package lsplus.main.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;

public class SendClientMessage {
    public static void sendMessage(Boolean includePrefix, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.inGameHud != null) {
            ChatHud chatHud = client.inGameHud.getChatHud();
            if (chatHud != null) {
                if (includePrefix) {
                    String LSPPrefix = "§7§l[§6§lL§e§lS§f§l+§7§l]§r ";
                    chatHud.addMessage(Text.literal(LSPPrefix + message));
                } else {
                    chatHud.addMessage(Text.literal(message));
                }


            }
        }
    }
}

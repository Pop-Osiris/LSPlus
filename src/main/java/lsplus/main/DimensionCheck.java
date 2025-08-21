package lsplus.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DimensionCheck {
     public static void checkDimension(String checkIfInDimension, String sendToDimension) {
         MinecraftClient client = MinecraftClient.getInstance();
         ClientPlayerEntity player = client.player;

         if (player != null) {
             RegistryKey<World> dimensionKey = player.getWorld().getRegistryKey();

             RegistryKey<World> afkDimensionKey = RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", checkIfInDimension));

             if (dimensionKey.equals(afkDimensionKey)) {
                 client.getNetworkHandler().sendCommand("warp " + sendToDimension);
             }
//             else {
//                 String dimensionName = dimensionKey.getValue().toString();
//                 System.out.println("player is in dimension " + dimensionName);
//             }
         }
     }
}

package lsplus.main.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryWrapper;

import java.io.*;
import java.util.*;

import net.minecraft.item.Items;


public class NBTConvert {

    public static NbtCompound serializeHashMap(Map<String, List<ItemStack>> data) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.getRegistryManager() != null) {
            RegistryWrapper.WrapperLookup lookup = client.world.getRegistryManager();

            NbtCompound rootTag = new NbtCompound();

            for (Map.Entry<String, List<ItemStack>> entry : data.entrySet()) {
                String category = entry.getKey();
                List<ItemStack> itemStacks = entry.getValue();

                NbtList itemStackListNbt = new NbtList();

                for (ItemStack itemStack : itemStacks) {
                    NbtCompound itemStackNbt = (NbtCompound) itemStack.toNbt(lookup);
                    itemStackListNbt.add(itemStackNbt);
                }
                rootTag.put(category, itemStackListNbt);
            }
            return rootTag;
        }

        System.err.println("Cannot serialize HashMap: RegistryWrapper.WrapperLookup is not available (not in a world).");
        return null;
    }

    public static Map<String, List<ItemStack>> deserializeNBT(File nbtFile) {
        Map<String, List<ItemStack>> data = new HashMap<>();
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.world.getRegistryManager() == null) {
            System.err.println("Cannot deserialize HashMap: RegistryWrapper.WrapperLookup is not available (not in a world).");
            return data;
        }
        RegistryWrapper.WrapperLookup lookup = client.world.getRegistryManager();

        try (FileInputStream fis = new FileInputStream(nbtFile);
             DataInputStream dis = new DataInputStream(fis)) {
            NbtCompound rootTag = net.minecraft.nbt.NbtIo.readCompressed(dis, NbtSizeTracker.of(2097152L));

            for (String category : rootTag.getKeys()) {
                NbtElement element = rootTag.get(category);

                if (element instanceof NbtList itemStackListNbt &&
                        itemStackListNbt.getHeldType() == NbtElement.COMPOUND_TYPE) {

                    List<ItemStack> itemStacks = new ArrayList<>();

                    for (NbtElement itemStackElement : itemStackListNbt) {
                        NbtCompound itemStackNbt = (NbtCompound) itemStackElement;

                        Optional<ItemStack> optionalItemStack = ItemStack.fromNbt(lookup, itemStackNbt);
                        if (optionalItemStack.isPresent()) {
                            ItemStack itemStack = optionalItemStack.get();
                            itemStacks.add(itemStack);
                        } else {
                            System.err.println("Warning: Failed to deserialize ItemStack from NBT: " + itemStackNbt.asString());
                        }
                    }
                    data.put(category, itemStacks);
                } else {
                    System.err.println("Warning: Expected NbtList of NbtCompounds for key " + category + ", but found different type.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static void main(String[] args) {
        Map<String, List<ItemStack>> playerInventories = new HashMap<>();
        List<ItemStack> inventory1 = new ArrayList<>();
        inventory1.add(new ItemStack(Items.DIAMOND_SWORD, 1));
        inventory1.add(new ItemStack(Items.DIRT, 64));
        playerInventories.put("player1_inventory", inventory1);

        List<ItemStack> inventory2 = new ArrayList<>();
        inventory2.add(new ItemStack(Items.APPLE, 5));
        inventory2.add(new ItemStack(Items.STONE, 32));
        playerInventories.put("player2_inventory", inventory2);

        NbtCompound nbtData = serializeHashMap(playerInventories);

        if (nbtData != null) {
            File file = new File("player_inventories_native.nbt");
            try (FileOutputStream fos = new FileOutputStream(file);
                 DataOutputStream dos = new DataOutputStream(fos)) {
                net.minecraft.nbt.NbtIo.writeCompressed(nbtData, dos);
                System.out.println("HashMap serialized to " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }

            Map<String, List<ItemStack>> loadedInventories = deserializeNBT(file);
            System.out.println("Deserialized HashMap: " + loadedInventories);

            if (loadedInventories.containsKey("player1_inventory")) {
                System.out.println("Player 1's inventory:");
                for (ItemStack stack : loadedInventories.get("player1_inventory")) {
                    System.out.println("  - " + stack.getCount() + "x " + stack.getName().getString());
                }
            }
        }
    }
}

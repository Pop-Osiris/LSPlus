package lsplus.main;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;

import java.io.*;
import java.util.*;

public class PVDataManager {
    static HashMap<String, List<ItemStack>> savedPlayerVaults = new HashMap<>();

    private static final File PVS_FILE = new File("player_vaults.nbt");

    public static NbtCompound serializePVS() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.world.getRegistryManager() != null) {
            RegistryWrapper.WrapperLookup lookup = client.world.getRegistryManager();

            NbtCompound rootTag = new NbtCompound();

            for (Map.Entry<String, List<ItemStack>> entry : savedPlayerVaults.entrySet()) {
                String pvID = entry.getKey();
                List<ItemStack> itemStacks = entry.getValue();

                NbtList itemStackListNbt = new NbtList();

                for (ItemStack itemStack : itemStacks) {
                    if (itemStack.isEmpty()) {
                        itemStackListNbt.add(new NbtCompound());
                    } else {
                        NbtCompound itemStackNbt = (NbtCompound) itemStack.toNbt(lookup);
                        itemStackListNbt.add(itemStackNbt);
                    }
                }
                rootTag.put(pvID, itemStackListNbt);
            }
            return rootTag;
        }

        System.err.println("Cannot serialize vaults: RegistryWrapper.WrapperLookup is not available (not in a world).");
        return null;
    }

    public static Map<String, List<ItemStack>> deserializePVS() {
        Map<String, List<ItemStack>> loadedData = new HashMap<>();
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.world == null || client.world.getRegistryManager() == null) {
            System.err.println("Cannot deserialize vaults: RegistryWrapper.WrapperLookup is not available (not in a world).");
            return loadedData; // Return empty map
        }
        RegistryWrapper.WrapperLookup lookup = client.world.getRegistryManager();

        if (!PVS_FILE.exists()) {
            System.out.println("Vaults file does not exist, starting with empty vaults.");
            return loadedData;
        }

        try (FileInputStream fis = new FileInputStream(PVS_FILE);
             DataInputStream dis = new DataInputStream(fis)) {
            NbtCompound rootTag = NbtIo.readCompressed(dis, NbtSizeTracker.of(16777216L));

            for (String PV : rootTag.getKeys()) {
                NbtElement element = rootTag.get(PV);

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
                            System.err.println("Warning: Failed to deserialize ItemStack for pv " + PV + " from NBT: " + itemStackNbt.asString());
                            itemStacks.add(ItemStack.EMPTY);
                        }
                    }
                    loadedData.put(PV, itemStacks);
                } else {
                    System.err.println("Warning: Expected NbtList of NbtCompounds for pv " + PV + ", but found different type.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedData;
    }

    public static void savePVData() {
        NbtCompound nbtData = serializePVS();
        if (nbtData != null) {
            try (FileOutputStream fos = new FileOutputStream(PVS_FILE);
                 DataOutputStream dos = new DataOutputStream(fos)) {
                net.minecraft.nbt.NbtIo.writeCompressed(nbtData, dos);
                System.out.println("PV data saved to " + PVS_FILE.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to serialize PV data, not saving.");
        }
    }

    public static void loadPVData() {
        savedPlayerVaults = (HashMap<String, List<ItemStack>>) deserializePVS();
        System.out.println("Vault data loaded. Total Amount of PVs : " + savedPlayerVaults.size());
    }

    public static void initializePVsForStartup() {
        loadPVData();
    }
    public static void savePVsForShutdown() {
        savePVData();
    }

    public static void savePlayerVaultContents(String vaultName, List<ItemStack> contents) {
        // Store a copy of the contents to prevent external modifications
        savedPlayerVaults.put(vaultName, new ArrayList<>(contents));
        // System.out.println("Saved contents for player vault '" + vaultName + "'. Total player vaults saved: " + savedPlayerVaults.size());
        // System.out.println(savedPlayerVaults);
    }
}


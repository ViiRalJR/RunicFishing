package me.viiral.runicfishing.manager;

import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.viiral.runicfishing.RunicFishing;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ItemManager {

    private final RunicFishing main;

    private ItemStack fillerItem;
    private ItemStack previousPageItem;
    private ItemStack nextPageItem;

    private Map<Integer, ItemStack> fishingRodItems = new HashMap<>();

    public ItemManager(RunicFishing main){
        this.main = main;

        this.setFillerItem(this.getItemStackFromPath("gui.filler-item", true));
        this.setNextPageItem(this.getItemStackFromPath("gui.nextpage-item", true));
        this.setPreviousPageItem(this.getItemStackFromPath("gui.previouspage-item", true));

        main.getConfig().getConfigurationSection("fishing-rods.rod-tiers").getKeys(false).forEach(fishingRodTier ->
                this.fishingRodItems.put(Integer.parseInt(fishingRodTier), this.getItemStackFromPath("fishing-rods.rod-tiers." + fishingRodTier, false))
        );
    }

    public ItemStack getItemStackFromPath(String path, boolean simple){
        ItemStack itemStack = new ItemStack(main.getConfig().getInt(path + ".material", 1));
        itemStack.setDurability((short) main.getConfig().getInt(path + ".durability", 0));

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(path + ".name", " ")));

        if(!simple) {
            List<String> lore = new ArrayList<>();
            for (String line : (List<String>) this.main.getConfig().get(path + ".lore")){
                String realLine = ChatColor.translateAlternateColorCodes('&', line);
                lore.add(realLine);
            }
            itemMeta.setLore(lore);
            Map<Enchantment, Integer> enchants = new HashMap<>();
            this.main.getConfig().getConfigurationSection(path + ".enchants").getKeys(false).forEach(enchantIdString ->
                {
                    int enchantId = Integer.parseInt(enchantIdString);
                    Enchantment enchantment = Enchantment.getById(enchantId);
                    int enchantLevel = this.main.getConfig().getInt(path + ".enchants." + enchantIdString);
                    enchants.put(enchantment, enchantLevel);
                }
            );
            enchants.forEach((enchant, level) -> itemMeta.addEnchant(enchant, level, true));

            boolean hideEnchants = this.main.getConfig().getBoolean(path + ".hide-enchants", false);
            if (hideEnchants) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            boolean hideAttributes = this.main.getConfig().getBoolean(path + ".hide-attributes", false);
            if (hideAttributes) itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            boolean unbreakableAttribute = this.main.getConfig().getBoolean(path + ".unbreakable", false);
            if (unbreakableAttribute) {
                itemMeta.spigot().setUnbreakable(true);
                itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack getItemStackFromPathWithPlaceholders(String path, Player player) {
        ItemStack itemStack = new ItemStack(main.getConfig().getInt(path + ".material", 1));
        itemStack.setDurability((short) main.getConfig().getInt(path + ".durability", 0));

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString(path + ".name", " ")));

        List<String> lore = new ArrayList<>();
        for (String line : (List<String>) this.main.getConfig().get(path + ".lore")) {
            String realLine = PlaceholderAPI.setPlaceholders(player, line);
            realLine = ChatColor.translateAlternateColorCodes('&', realLine);
            lore.add(realLine);
        }
        itemMeta.setLore(lore);

        Map<Enchantment, Integer> enchants = new HashMap<>();
        this.main.getConfig().getConfigurationSection(path + ".enchants").getKeys(false).forEach(enchantIdString ->
                {
                    int enchantId = Integer.parseInt(enchantIdString);
                    Enchantment enchantment = Enchantment.getById(enchantId);

                    int enchantLevel = this.main.getConfig().getInt(path + ".enchants." + enchantIdString);

                    enchants.put(enchantment, enchantLevel);
                }
        );
        enchants.forEach((enchant, level) -> itemMeta.addEnchant(enchant, level, true));

        boolean hideEnchants = this.main.getConfig().getBoolean(path + ".hide-enchants", false);
        if (hideEnchants) itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        boolean hideAttributes = this.main.getConfig().getBoolean(path + ".hide-attributes", false);
        if (hideAttributes) itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        boolean unbreakableAttribute = this.main.getConfig().getBoolean(path + ".unbreakable", false);
        if (unbreakableAttribute) {
            itemMeta.spigot().setUnbreakable(true);
            itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack getFishingRod(int tier, int luckLevel, int doubleCatchLevel, int crateFinderLevel, int fishNetLevel) {
        ItemStack fishingRod = this.getFishingRodItems().get(tier);
        if (fishingRod == null) return null;
        fishingRod = fishingRod.clone();

        // Lore Stuff
        ItemMeta fishingRodMeta = fishingRod.getItemMeta();
        List<String> fishingRodLore = fishingRodMeta.getLore();
        if (fishingRodLore == null) fishingRodLore = new ArrayList<>();

        List<String> newFishingRodLore = new ArrayList<>();
        for (String line : fishingRodLore) {
            line = line.replace("%luckLevel%", luckLevel + "");
            line = line.replace("%doubleCatchLevel%", doubleCatchLevel + "");
            line = line.replace("%crateFinderLevel%", crateFinderLevel + "");
            line = line.replace("%fishNetLevel%", fishNetLevel + "");
            newFishingRodLore.add(line);
        }
        fishingRodMeta.setLore(newFishingRodLore);
        fishingRod.setItemMeta(fishingRodMeta);

        // Adding NBT stuff
        fishingRod = this.setNBTTagInt(fishingRod, "tier", tier);
        fishingRod = this.setNBTTagInt(fishingRod, "luck", luckLevel);
        fishingRod = this.setNBTTagInt(fishingRod, "doubleCatch", doubleCatchLevel);
        fishingRod = this.setNBTTagInt(fishingRod, "crateFinder", crateFinderLevel);
        fishingRod = this.setNBTTagInt(fishingRod, "fishNet", fishNetLevel);

        return fishingRod;
    }

    private ItemStack addNbtCompound(ItemStack item, String compound) {
        if (item == null || item.getType() == Material.AIR || compound == null) return item;

        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(craftStack);

        NBTTagCompound tag = itemStack.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

        if (!tag.hasKey("runic")) tag.set("runic", new NBTTagCompound());
        tag.getCompound("runic").setBoolean(compound, true);

        return CraftItemStack.asCraftMirror(itemStack);
    }

    public boolean hasNbtCompound(ItemStack item, String compound) {
        if (item == null || item.getType() == Material.AIR) return false;

        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(craftStack);

        NBTTagCompound tag = itemStack.getTag();
        if (tag == null || !tag.hasKey("runic")) return false;

        NBTTagCompound nbtTagCompound = tag.getCompound("runic");
        return nbtTagCompound.hasKey(compound);
    }

    public int getNBTTagInt(ItemStack item, String compound) {
        if (item == null || compound == null) return 0;

        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(craftStack);

        NBTTagCompound tag = itemStack.getTag();
        if (tag == null || !tag.hasKey("runic")) return 0;

        NBTTagCompound nbtTagCompound = tag.getCompound("runic");
        if (nbtTagCompound.hasKey(compound)) return nbtTagCompound.getInt(compound);

        return 0;
    }

    public ItemStack setNBTTagInt(ItemStack item, String compound, int value) {
        if (item == null || compound == null) return item;

        CraftItemStack craftStack = CraftItemStack.asCraftCopy(item);
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(craftStack);

        NBTTagCompound tag = itemStack.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
            itemStack.setTag(tag);
        }

        if (!tag.hasKey("runic")) tag.set("runic", new NBTTagCompound());
        tag.getCompound("runic").setInt(compound, value);

        return CraftItemStack.asCraftMirror(itemStack);
    }


}

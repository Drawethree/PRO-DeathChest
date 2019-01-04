package sk.drawethree.deathchestpro.utils;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.chest.DeathChest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtil {

    public static ItemStack getPlayerSkull(Player player, String title, List<String> lore) {
        ItemStack skull = CompMaterial.PLAYER_HEAD.toItem();
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        if (title != null) {
            meta.setDisplayName(title);
        }
        if (lore != null) {
            meta.setLore(convertLore(lore));
        }
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getPlayerSkull(OfflinePlayer player, String title, List<String> lore) {
        ItemStack skull = CompMaterial.PLAYER_HEAD.toItem();
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(player.getName());
        meta.setDisplayName(title);
        meta.setLore(convertLore(lore));
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack loadItemFromConfig(String configName, String path) {
        CompMaterial m = CompMaterial.fromString(DeathChestPro.getFileManager().getConfig(configName).get().getString(path + ".material"));
        int amount = DeathChestPro.getFileManager().getConfig(configName).get().getInt(path + ".amount");
        String displayName = DeathChestPro.getFileManager().getConfig(configName).get().getString(path + ".displayname");
        List<String> lore = convertLore(DeathChestPro.getFileManager().getConfig(configName).get().getStringList(path + ".lore"));
        List<String> enchants = DeathChestPro.getFileManager().getConfig(configName).get().getStringList(path + ".enchants");
        return create(m, amount, displayName, lore, enchants);
    }

    public static int getInventorySizeBasedOnList(List<?> list) {
        int size = 9;
        while (list.size() > size) {
            if (size == 54) {
                break;
            }
            size += 9;
        }
        return size;
    }

    public static int getInventorySize(int i) {
        int size = 9;
        while (i > size) {
            if (size == 54) {
                break;
            }
            size += 9;
        }
        return size;
    }


    public static List<String> convertLore(List<String> list) {
        List<String> lore = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            lore.add(ChatColor.translateAlternateColorCodes('&', (String) list.get(i)));
        }
        return lore;
    }


    public static List<String> makeLore(String... string) {
        return Arrays.asList(string);
    }


    public static ItemStack create(CompMaterial material, int amount, String displayName, List<String> lore, List<String> enchantments) {
        ItemStack item = material.toItem(amount);
        ItemMeta meta = item.getItemMeta();
        if (displayName != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        }
        if (lore != null) {
            meta.setLore(convertLore(lore));
        }
        if (enchantments != null) {
            for (int i = 0; i < enchantments.size(); i++) {
                String[] enchantment = enchantments.get(i).split(":");
                meta.addEnchant(ench(enchantment[0]), Integer.valueOf(enchantment[1]), true);
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createListItem(DeathChest dc) {
        ItemStack returnItem = Items.DEATHCHEST_LIST_ITEM.getItemStack().clone();
        ItemMeta meta = returnItem.getItemMeta();
        meta.setDisplayName(meta.getDisplayName().replaceAll("%player%", dc.getPlayer().getName()));
        List<String> lore = meta.getLore();
        for (int i = 0; i < lore.size(); i++) {
            lore.set(i, lore.get(i).replaceAll("%xloc%", String.valueOf(dc.getFirstChest().getLocation().getBlockX())).
                    replaceAll("%yloc%", String.valueOf(dc.getFirstChest().getLocation().getBlockY())).
                    replaceAll("%zloc%", String.valueOf(dc.getFirstChest().getLocation().getBlockZ())).
                    replaceAll("%world%", dc.getFirstChest().getLocation().getWorld().getName()).
                    replaceAll("%locked%", String.valueOf(dc.isLocked()).toUpperCase()).
                    replaceAll("%chesttype%", dc.getType().getName()));
        }
        meta.setLore(lore);
        returnItem.setItemMeta(meta);
        return returnItem;
    }

    /*
     * replacing enchantment with string
     */
    private static Enchantment ench(String n) {
        n = n.toLowerCase();
        n = n.replace("alldamage", "DAMAGE_ALL").replace("alldmg", "DAMAGE_ALL").replace("sharpness", "DAMAGE_ALL");
        n = n.replace("arthropodsdamage", "DAMAGE_ARTHROPODS").replace("ardmg", "DAMAGE_ARTHROPODS").replace("baneofarthropods", "DAMAGE_ARTHROPODS");
        n = n.replace("undeaddamage", "DAMAGE_UNDEAD").replace("smite ", "DAMAGE_UNDEAD");
        n = n.replace("digspeed", "DIG_SPEED").replace("efficiency", "DIG_SPEED");
        n = n.replace("durability", "DURABILITY").replace("dura", "DURABILITY").replace("unbreaking ", "DURABILITY");
        n = n.replace("fireaspect", "FIRE_ASPECT").replace("fire", "FIRE_ASPECT");
        n = n.replace("knockback ", "KNOCKBACK");
        n = n.replace("blockslootbonus", "LOOT_BONUS_BLOCKS").replace("fortune", "LOOT_BONUS_BLOCKS");
        n = n.replace("mobslootbonus", "LOOT_BONUS_MOBS").replace("mobloot", "LOOT_BONUS_MOBS").replace("looting", "LOOT_BONUS_MOBS");
        n = n.replace("oxygen", "OXYGEN").replace("respiration", "OXYGEN");
        n = n.replace("protection", "PROTECTION_ENVIRONMENTAL").replace("prot", "PROTECTION_ENVIRONMENTAL");
        n = n.replace("explosionsprotection", "PROTECTION_EXPLOSIONS").replace("expprot", "PROTECTION_EXPLOSIONS").replace("blastprotection", "PROTECTION_EXPLOSIONS");
        n = n.replace("fallprotection", "PROTECTION_FALL").replace("fallprot", "PROTECTION_FALL").replace("featherfall", "PROTECTION_FALL").replace("featherfalling", "PROTECTION_FALL");
        n = n.replace("fireprotection", "PROTECTION_FIRE").replace("fireprot", "PROTECTION_FIRE");
        n = n.replace("projectileprotection", "PROTECTION_PROJECTILE").replace("projprot", "PROTECTION_PROJECTILE");
        n = n.replace("silktouch", "SILK_TOUCH");
        n = n.replace("waterworker", "WATER_WORKER").replace("aquainfinity", "WATER_WORKER");
        n = n.replace("firearrow", "ARROW_FIRE").replace("flame", "ARROW_FIRE");
        n = n.replace("arrowdamage", "ARROW_DAMAGE").replace("power", "ARROW_DAMAGE");
        n = n.replace("arrowknockback", "ARROW_KNOCKBACK").replace("arrowkb", "ARROW_KNOCKBACK").replace("punch", "ARROW_KNOCKBACK");
        n = n.replace("infinitearrows", "ARROW_INFINITE").replace("infarrows", "ARROW_INFINITE").replace("infinity", "ARROW_INFINITE");
        n = n.toUpperCase();
        Enchantment ench = Enchantment.getByName(n);
        return ench;
    }

    public static List<String> makeTeamLore(String... string) {
        return Arrays.asList(string);
    }

    public static boolean isHelmet(ItemStack i) {
        return CompMaterial.isHelmet(i.getType());
    }

    public static boolean isChestplate(ItemStack i) {
        return CompMaterial.isChestPlate(i.getType());
    }

    public static boolean isBoots(ItemStack i) {
        return CompMaterial.isBoots(i.getType());
    }

    public static boolean isLeggings(ItemStack i) {
        return CompMaterial.isLeggings(i.getType());
    }
}
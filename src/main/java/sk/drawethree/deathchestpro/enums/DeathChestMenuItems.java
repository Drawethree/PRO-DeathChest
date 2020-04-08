package sk.drawethree.deathchestpro.enums;

import org.bukkit.inventory.ItemStack;
import sk.drawethree.deathchestpro.utils.ItemUtil;

public enum DeathChestMenuItems {

    PREV_ITEM("items.prev_item"),
    NEXT_ITEM("items.next_item"),
    DEATHCHEST_LIST_ITEM("items.deathchest_item");


    private ItemStack itemStack;
    private String path;

    DeathChestMenuItems(String path) {
        this.path = path;
        this.itemStack = ItemUtil.loadItemFromConfig("items.yml", path);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }


    public static void reload() {
        for (DeathChestMenuItems i : values()) {
            i.setItemStack(ItemUtil.loadItemFromConfig("items.yml", i.path));
        }
    }


    private void setItemStack(ItemStack itemstack) {
        this.itemStack = itemstack;
    }


}

package sk.drawethree.deathchestpro.utils;

import org.bukkit.inventory.ItemStack;

public enum Items {

    PREV_ITEM("items.prev_item"),
    NEXT_ITEM("items.next_item"),
    DEATHCHEST_LIST_ITEM("items.deathchest_item");


    private ItemStack itemStack;
    private String path;

    Items(String path) {
        this.path = path;
        this.itemStack = ItemUtil.loadItemFromConfig("items.yml", path);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }


    public static void reload() {
        for (Items i : values()) {
            i.setItemStack(ItemUtil.loadItemFromConfig("items.yml", i.path));
        }
    }


    private void setItemStack(ItemStack itemstack) {
        this.itemStack = itemstack;
    }


}

package sk.drawethree.deathchestpro.utils;

import org.bukkit.inventory.ItemStack;

public enum Items {

    PREV_ITEM("items.prev_item"),
    NEXT_ITEM("items.next_item"),
    DEATHCHEST_LIST_ITEM("items.deathchest_item");


    private ItemStack itemStack;
    private String path;
    //private int slot;

    Items(String path) {
        this.path = path;
        this.itemStack = ItemUtil.loadItemFromConfig("items.yml", path);
        //this.slot = Main.getFileManager().getConfig("items.yml").get().getInt(path + ".slot");
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    /*public int getSlot() {
        return slot;
    }
    */

    public static Items getItem(ItemStack item) {
        for (Items i : values()) {
            if (i.getItemStack().equals(item)) {
                return i;
            }
        }
        return null;
    }

    public static void reload() {
        for (Items i : values()) {
            i.setItemStack(ItemUtil.loadItemFromConfig("items.yml", i.path));
            //i.setSlot(Casino.getFileManager().getConfig("items.yml").get().getInt(i.path + ".slot"));
        }
    }


    private void setItemStack(ItemStack itemstack) {
        this.itemStack = itemstack;
    }

   /* public void setSlot(int slot) {
        this.slot = slot;
    }
    */


}

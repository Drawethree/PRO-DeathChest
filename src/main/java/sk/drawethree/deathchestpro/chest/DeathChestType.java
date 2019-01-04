package sk.drawethree.deathchestpro.chest;

public enum DeathChestType {

    CHEST("Single Chest"),
    DOUBLE_CHEST("Double Chest");

    private String name;

    DeathChestType(String chest) {
        this.name = chest;
    }

    public String getName() {
        return name;
    }
}

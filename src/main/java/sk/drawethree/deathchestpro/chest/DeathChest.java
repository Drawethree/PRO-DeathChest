package sk.drawethree.deathchestpro.chest;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import sk.drawethree.deathchestpro.DeathChestPro;
import sk.drawethree.deathchestpro.managers.DeathChestManager;
import sk.drawethree.deathchestpro.utils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeathChest {

    private Player player;
    private Hologram hologram;
    private List<Chest> chests;
    private BukkitTask removeTask;
    private boolean locked;
    private ItemStack listItem;
    private DeathChestType type;
    private boolean announced;

    public DeathChest(Player p, List<ItemStack> items) {
        this.player = p;
        this.locked = p.hasPermission("deathchestpro.lock");
        this.setupChests(p.getLocation(), items);
        this.setupHologram();
        this.listItem = ItemUtil.createListItem(this);
        this.announced = false;
    }

    private void setupHologram() {
        if (DeathChestPro.getInstance().isUseHolograms()) {

            Location hologramLoc = LocationUtil.getCenter(this.chests.get(0).getLocation()).add(0, 2.5, 0);

            if (this.chests.size() == 2) {
                hologramLoc = hologramLoc.add(0.5, 0, 0);
            }
            this.hologram = HologramsAPI.createHologram(DeathChestPro.getInstance(), hologramLoc);

            if (DeathChestPro.getInstance().isDisplayPlayerHead()) {
                hologram.appendItemLine(ItemUtil.getPlayerSkull(player, null, null));
            }

            for (String s : DeathChestPro.getInstance().getHologramLines()) {
                hologram.appendTextLine(s
                        .replaceAll("%locked%", getLockedString())
                        .replaceAll("%player%", player.getName())
                        .replaceAll("%timeleft%", new Time(DeathChestPro.getInstance().getRemoveChestAfter(), TimeUnit.SECONDS).toString())
                );
            }
        }
    }

    private void setupChests(Location loc, List<ItemStack> items) {
        this.chests = new ArrayList<>();

        if (DeathChestPro.getInstance().isSpawnChestOnHighestBlock()) {
            loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
        }

        this.type = DeathChestType.CHEST;

        loc.getBlock().setType(CompMaterial.CHEST.getMaterial());

        Chest chest1 = (Chest) loc.getBlock().getState();
        this.chests.add(chest1);

        for (int i = 0; i < 27; i++) {
            try {
                chest1.getBlockInventory().addItem(items.get(i));
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            } catch (IndexOutOfBoundsException e) {
                return;
            }
        }

        if (items.size() > 27) {
            this.type = DeathChestType.DOUBLE_CHEST;
            loc.add(1, 0, 0).getBlock().setType(CompMaterial.CHEST.getMaterial());

            Chest chest2 = (Chest) loc.getBlock().getState();
            this.chests.add(chest2);

            for (int i = 27; i < items.size(); i++) {
                try {
                    chest2.getBlockInventory().addItem(items.get(i));
                } catch (ArrayIndexOutOfBoundsException e) {
                    return;
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public boolean isLocked() {
        return locked;
    }

    public ItemStack getListItem() {
        return listItem;
    }

    public void runRemoveTask() {
        this.removeTask = new BukkitRunnable() {

            int timeLeft = DeathChestPro.getInstance().getRemoveChestAfter();

            @Override
            public void run() {
                if (timeLeft == 0) {
                    removeDeathChest();
                    cancel();
                } else {
                    timeLeft--;
                    if (hologram != null) {
                        updateHologram(timeLeft);
                    }
                    if (DeathChestPro.getInstance().isDeathchestFireworks()) {
                        FireworkUtil.spawnRandomFirework(hologram.getLocation());
                    }
                }
            }
        }.runTaskTimer(DeathChestPro.getInstance(), 20L, 20L);
    }

    public List<Chest> getChests() {
        return chests;
    }

    private void updateHologram(int timeLeft) {
        for (int i = 0; i < DeathChestPro.getInstance().getHologramLines().size(); i++) {
            String line = DeathChestPro.getInstance().getHologramLines().get(i);
            if (line.contains("%timeleft%")) {
                int lineNumber = i;
                if (DeathChestPro.getInstance().isDisplayPlayerHead()) {
                    lineNumber += 1;
                }
                hologram.removeLine(lineNumber);
                hologram.insertTextLine(lineNumber, line.replaceAll("%timeleft%", new Time(timeLeft, TimeUnit.SECONDS).toString()));
            }
        }
    }

    public boolean areChestsEmpty() {
        boolean b = false;
        for (Chest c : chests) {
            if (DeathChestManager.isInventoryEmpty(c.getBlockInventory())) {
                b = true;
            } else {
                return false;
            }
        }
        return b;
    }

    public void removeChests() {
        Iterator it = chests.iterator();
        while (it.hasNext()) {
            Chest c = (Chest) it.next();
            c.getBlock().setType(CompMaterial.AIR.getMaterial());
            it.remove();
        }
    }

    public void removeDeathChest() {
        if (removeTask != null) {
            removeTask.cancel();
        }

        if (hologram != null) {
            hologram.delete();
        }

        this.removeChests();

        if (this.player.isOnline()) {
            this.player.sendMessage(Message.DEATHCHEST_DISAPPEARED.getChatMessage());
        }
        DeathChestManager.getInstance().removeDeathChest(this);
    }

    public DeathChestType getType() {
        return type;
    }

    public Chest getFirstChest() {
        return this.chests.get(0);
    }

    public Chest getSecondChest() {
        return this.chests.get(1);
    }

    public boolean isAnnounced() {
        return announced;
    }

    public void announce(Player p) {
        /*if (DeathChestPro.getInstance().isClickableMessage()) {
            TextComponent msg = new TextComponent(Message.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.chests.get(0).getLocation().getBlockX())).replaceAll("%yloc%", String.valueOf(this.chests.get(0).getLocation().getBlockY())).replaceAll("%zloc%", String.valueOf(this.chests.get(0).getLocation().getBlockZ())).replaceAll("%world%", this.chests.get(0).getLocation().getWorld().getName()));
            msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.DEATHCHEST_LOCATED_HOVER.getMessage()).create()));
            msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName() + " " + this.chests.get(0).getX() + " " + this.chests.get(0).getY() + " " + this.chests.get(0).getZ()));
            player.spigot().sendMessage(msg);*/
        p.sendMessage(Message.DEATHCHEST_LOCATED.getChatMessage().replaceAll("%xloc%", String.valueOf(this.chests.get(0).getLocation().getBlockX())).replaceAll("%yloc%", String.valueOf(this.chests.get(0).getLocation().getBlockY())).replaceAll("%zloc%", String.valueOf(this.chests.get(0).getLocation().getBlockZ())).replaceAll("%world%", this.chests.get(0).getLocation().getWorld().getName()));
        p.sendMessage(Message.DEATHCHEST_WILL_DISAPPEAR.getChatMessage().replaceAll("%time%", String.valueOf(DeathChestPro.getInstance().getConfig().getInt("remove_chest_time"))));
        this.announced = true;
    }

    private String getLockedString() {
        return locked ? Message.DEATHCHEST_LOCKED.getMessage() : Message.DEATHCHEST_UNLOCKED.getMessage();
    }

    public boolean teleportPlayer(Player p) {
        if (p.hasPermission("deathchestpro.teleport")) {
            p.teleport(this.chests.get(0).getLocation().clone().add(0, 1, 0));
            p.sendMessage(Message.DEATHCHEST_TELEPORTED.getChatMessage());
            return true;
        } else {
            p.sendMessage(Message.NO_PERMISSION.getChatMessage());
            return false;
        }
    }
}

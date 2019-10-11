package sk.drawethree.deathchestpro.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class DeathChestExpireGroup {

    public static final DeathChestExpireGroup DEFAULT_GROUP = new DeathChestExpireGroup("default", "deathchestpro.expire.default",-1, 60, 0);

    private String label;
    private String requiredPermission;
    private int unlockAfter;
    private int expireTime;
    private double teleportCost;

}

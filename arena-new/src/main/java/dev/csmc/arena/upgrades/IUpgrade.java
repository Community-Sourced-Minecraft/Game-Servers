package dev.csmc.arena.upgrades;

public interface IUpgrade {
    public int level = 0;
    public int maxLevel = 0;
    public UpgradeType upgradeType = null;

    public void upgrade();

}

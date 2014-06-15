package net.minearea.explosivemobs;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import java.io.IOException;

public class ExplosiveMobs extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException exception) {
            // Failed to submit the stats :-(
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("spawnexplosivemob")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command must be executed as a player!");
                return true;
            }

            if (!sender.hasPermission("explosivemobs.spawn")) {
                sender.sendMessage("Permission denied!");
                return true;
            }

            EntityType type = null;
            String subType = null;
            int amount = 1;
            Player target = (Player) sender;

            if (args.length == 0) {
                return false;
            }

            if (args.length >= 1)// Type specified
            {
                String[] typeParts = args[0].split(":");
                type = EntityType.fromName(typeParts[0]);
                if (type == null) {
                    sender.sendMessage("Invalid mob type!");
                    return true;
                }

                if (typeParts.length >= 2) {
                    subType = typeParts[1];
                }
            }
            if (args.length >= 2)// Amount specified
            {
                amount = Integer.parseInt(args[1]);
                if (amount < 1) {
                    sender.sendMessage("Amount must be 1 or greater!");
                    return true;
                }
                if (getConfig().getInt("maxSpawnAmount", 0) != 0 && amount > getConfig().getInt("maxSpawnAmount")) {
                    amount = getConfig().getInt("maxSpawnAmount");
                    sender.sendMessage("Mob spawning limit of " + getConfig().getInt("maxSpawnAmount") + " reached!");
                }
            }
            if (args.length >= 3)// Target player specified
            {
                if (!sender.hasPermission("explosivemobs.spawn.targetplayer")) {
                    sender.sendMessage("Permission denied!");
                    return true;
                }

                target = getServer().getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("Target player not found!");
                    return true;
                }
            }

            // Fixes some "Possible NullPointerException" warnings
            if (type == null) {
                return true;
            }

            for (int number = 0; number < amount; number++) {
                Entity entity = target.getWorld().spawnEntity(target.getLocation(), type);
                if (entity != null) {
                    switch (type) {
                        case CREEPER:
                            if (subType != null && subType.toLowerCase().equals("powered")) {
                                ((Creeper) entity).setPowered(true);
                            }
                            break;
                        case HORSE:
                            if (subType != null && !subType.isEmpty()) {
                                try {
                                    Horse.Variant variant = Horse.Variant.valueOf(subType.toUpperCase());
                                    if (variant != null) {
                                        ((Horse) entity).setVariant(variant);
                                    }
                                } catch (IllegalArgumentException exception) {
                                    sender.sendMessage("Invalid horse variant: " + subType);
                                    return true;
                                }
                            }
                            break;
                    }
                    entity.setMetadata("isPlayerSpawnedExplosiveMob", new FixedMetadataValue(this, true));
                }
            }
            sender.sendMessage("Spawned " + amount + " " + type.getName() + "(s) fed with TNT");

            return true;
        }

        return false;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        String configPath = "mobs." + entity.getType().getName();
        boolean isPlayerSpawnedExplosiveMob = entity.hasMetadata("isPlayerSpawnedExplosiveMob");

        if (entity.getKiller() == null && !isPlayerSpawnedExplosiveMob && !getConfig().getBoolean(configPath + ".triggeredByAny", getConfig().getBoolean("defaults.triggeredByAny"))) {
            return;
        }

        if (isPlayerSpawnedExplosiveMob || getConfig().getBoolean(configPath + ".enabled", getConfig().getBoolean("defaults.enabled"))) {
            Location entityLocation = entity.getLocation();
            double x = entityLocation.getX();
            double y = entityLocation.getY();
            double z = entityLocation.getZ();
            float power = (float) getConfig().getDouble(configPath + ".power", getConfig().getDouble("defaults.power"));
            boolean setFire = getConfig().getBoolean(configPath + ".setFire", getConfig().getBoolean("defaults.setFire"));
            boolean breakBlocks = getConfig().getBoolean(configPath + ".breakBlocks", getConfig().getBoolean("defaults.breakBlocks"));
            entity.getWorld().createExplosion(x, y, z, power, setFire, breakBlocks);
        }
    }
}

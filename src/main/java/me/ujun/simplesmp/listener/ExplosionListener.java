package me.ujun.simplesmp.listener;

import me.ujun.simplesmp.config.ConfigHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class ExplosionListener implements Listener {

    @EventHandler
    public void onExplosionDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        // 엔더 크리스탈 또는 리스폰 앵커가 폭발한 경우
        if (damager.getType() == EntityType.END_CRYSTAL) {
            double percent = ConfigHandler.explosionDamagePercent;
            percent = Math.max(0, Math.min(percent, 100));

            double original = event.getDamage();
            double reduced = original * (percent / 100.0);
            event.setDamage(reduced);
        }
    }

    @EventHandler
    public void onBlockExplosion(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            double percent = ConfigHandler.explosionDamagePercent;
            percent = Math.max(0, Math.min(percent, 100));

            double original = event.getDamage();
            double reduced = original * (percent / 100.0);
            event.setDamage(reduced);
        }
    }
}

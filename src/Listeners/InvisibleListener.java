package Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffectType;

import me.becja10.PvPModerator.PvPModerator;
import me.becja10.PvPModerator.PvPPlayer;

public class InvisibleListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {

		Player p = event.getPlayer();
		delayCheck(p);

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSplash(PotionSplashEvent e) {
		Player p = (e.getEntity().getShooter() instanceof Player) ? (Player) e.getEntity().getShooter() : null;
		delayCheck(p);
	}

	public void delayCheck(Player p) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PvPModerator.getInstance(), new Runnable() {
			public void run() {
				PvPPlayer pp = PvPModerator.getInstance().pvPPlayerManager.getPvPPlayerObject(p.getUniqueId());
				pp.set_isInvisible(p.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY));

			}
		}, 10L);
	}

}

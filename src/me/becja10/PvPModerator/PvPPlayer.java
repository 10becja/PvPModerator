package me.becja10.PvPModerator;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PvPPlayer {

	private UUID _id;

//	private boolean _inCombat;
//	private long _combatStarted;

	private boolean _isInvisible;
	private long _InvisibleCooldown;
	private boolean _CooldownActivate;

	private BlockedReason _reason;

	public PvPPlayer(UUID id, boolean invisible) {
		set_id(id);

		set_isInvisible(invisible);

//		_inCombat = false;
//		_combatStarted = 0L;

	}

	public void setReason(BlockedReason r, long unblockedTime) {
		_reason = r;
	}

	public String getWarnMessage(boolean isTarget, BlockedPlayer vic) {
		String ret = "";

		if (isTarget) {
			switch (_reason) {
			case NewPlayer:
				ret = ChatColor.GREEN + "You're protected from PvP because you are new. Use " + ChatColor.YELLOW
						+ "/removeprotection" + ChatColor.GREEN + " if you wish to PvP.";
				break;
			case TPEvent:
				ret = ChatColor.GREEN + "You've recently teleported and must wait before fighting.";
				break;
			default:
				break;
			}
		} else {
			if (vic == null) {
				ret = ChatColor.RED + "You cannot PvP while protected. Use " + ChatColor.YELLOW + "/removeprotection"
						+ ChatColor.RED + " if you wish to PvP";
			} else {
				switch (_reason) {
				case NewPlayer:
					ret = ChatColor.RED + "This player is new! You can't kill them yet.";
					break;
				case TPEvent:
					ret = ChatColor.RED + "This player is in tp cooldown. You can't attack them yet.";
					break;
				default:
					break;
				}
			}
		}

		return ret;
	}

	public UUID get_id() {
		return _id;
	}

	public void set_id(UUID _id) {
		this._id = _id;
	}

	public boolean is_isInvisible() {
		return _isInvisible;
	}

	public void set_isInvisible(boolean _isInvisible) {

		// if current invis and become uninvis then start cooldown
		if ((this._isInvisible) && (_isInvisible == false)) {
			this._isInvisible = _isInvisible;
			this._CooldownActivate = true;
			this._reason = BlockedReason.InvisibleCooldown;
			this._InvisibleCooldown = System.currentTimeMillis();
			PvPModerator.getInstance().invisiblePlayers.remove(this._id);
			PvPModerator.getInstance().invisibleCooldown.add(this._id);
			//Bukkit.broadcastMessage("Invis off, now on cooldown");
			new CooldownRemove().runTaskTimer(PvPModerator.getInstance(), 20, 20);
		}

		// if current not inv but becomes invis
		if ((!this._isInvisible && _isInvisible)) {
			this._isInvisible = _isInvisible;
			this._CooldownActivate = false;
			this._reason = BlockedReason.Invisible;
			new removeInvisCheck().runTaskTimer(PvPModerator.getInstance(), 20, 20);
			PvPModerator.getInstance().invisiblePlayers.add(this._id);
			PvPModerator.getInstance().invisibleCooldown.remove(this._id);
			//Bukkit.broadcastMessage("Invis now on");
		}

	}

	// Invis remover
	private class removeInvisCheck extends BukkitRunnable {

		removeInvisCheck() {
		}

		@Override
		public void run() {

			if (_isInvisible) {
				if (Bukkit.getOfflinePlayer(_id).getPlayer().isOnline()) {
					if (!Bukkit.getPlayer(_id).hasPotionEffect(PotionEffectType.INVISIBILITY)) {
						set_isInvisible(false);
					}
				}
			}

		}
	}

	// cooldown remover
	private class CooldownRemove extends BukkitRunnable {

		CooldownRemove() {
		}

		@Override
		public void run() {

			if (System.currentTimeMillis() > _InvisibleCooldown + PvPModerator.getInstance().invisibleCooldownTime) {
				_CooldownActivate = false;
				_isInvisible = false;
				PvPModerator.getInstance().invisibleCooldown.remove(_id);
				//Bukkit.broadcastMessage("Cooldown Off");
				this.cancel();
			}
		}
	}

	public long get_InvisibleCooldown() {
		return _InvisibleCooldown;
	}

	public void set_InvisibleCooldown(long _InvisibleCooldown) {
		this._InvisibleCooldown = _InvisibleCooldown;
	}

	public boolean is_CooldownActivate() {
		return _CooldownActivate;
	}

	public void set_CooldownActivate(boolean _CooldownActivate) {
		this._CooldownActivate = _CooldownActivate;
	}

}

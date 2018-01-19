package Managers;

import java.util.UUID;

import me.becja10.PvPModerator.PvPModerator;
import me.becja10.PvPModerator.PvPPlayer;

public class PvPPlayerManager {

	public PvPPlayer getPvPPlayerObject(UUID uuid) {

		for (PvPPlayer u : PvPModerator.getInstance().players) {
			//Bukkit.broadcastMessage("Player UUID " + u.get_id());
			if (u.get_id().equals(uuid)) {
				return u;
			}
		}

		return null;

	}

}

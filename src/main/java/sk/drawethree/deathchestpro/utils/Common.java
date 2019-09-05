package sk.drawethree.deathchestpro.utils;

import org.bukkit.ChatColor;

import java.util.List;

public class Common {

	public static List<String> color(List<String> stringList) {
		for (int i = 0; i < stringList.size(); i++) {
			stringList.set(i, ChatColor.translateAlternateColorCodes('&', stringList.get(i)));
		}
		return stringList;
	}
}

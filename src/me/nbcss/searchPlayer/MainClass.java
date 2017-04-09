package me.nbcss.searchPlayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;

import me.nbcss.quickGui.Operator;

public class MainClass extends JavaPlugin{
	private static JavaPlugin plugin;
	@Override
	public void onEnable(){
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
		plugin = this;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
		SearchInventory inv = new SearchInventory(player);
		Operator.openInventory(inv, player);
		return true;
	}
	public static JavaPlugin getHandle(){
		return plugin;
	}
}
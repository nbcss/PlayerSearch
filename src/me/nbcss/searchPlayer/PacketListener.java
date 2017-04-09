package me.nbcss.searchPlayer;

import java.io.UnsupportedEncodingException;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import me.nbcss.quickGui.Operator;
import me.nbcss.quickGui.elements.InventoryView;
import me.nbcss.quickGui.utils.wrapperPackets.WrapperPlayClientCustomPayload;
public class PacketListener extends PacketAdapter {
	public PacketListener(Plugin plugin) {
		super(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.CUSTOM_PAYLOAD);
	}

	@Override
	public Plugin getPlugin() {
		return super.getPlugin();
	}

	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return super.getReceivingWhitelist();
	}

	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return super.getSendingWhitelist();
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		if(event.getPacketType() != PacketType.Play.Client.CUSTOM_PAYLOAD)
			return;
		Player player = event.getPlayer();
		InventoryView view = Operator.getOpenedInventoryView(player);
		if(view == null)
			return;
		WrapperPlayClientCustomPayload packet = new WrapperPlayClientCustomPayload(event.getPacket());
		if(!packet.getChannel().equals("MC|ItemName"))
			return;
		if(!(view.getTopInventory() instanceof SearchInventory))
			return;
		event.setCancelled(true);
		SearchInventory anvil = (SearchInventory) view.getTopInventory();
		byte[] array = packet.getContents();
		try {
			String read = new String(array, "UTF-8").substring(1);
			anvil.search(read);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		
		
	}
}

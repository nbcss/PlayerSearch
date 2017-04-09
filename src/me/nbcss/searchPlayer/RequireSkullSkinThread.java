package me.nbcss.searchPlayer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import me.nbcss.quickGui.elements.Icon;
import me.nbcss.quickGui.elements.Interactable;
import me.nbcss.quickGui.elements.InventoryView;
import me.nbcss.quickGui.events.InventoryInteractEvent;
import net.md_5.bungee.api.ChatColor;

public class RequireSkullSkinThread extends MyThread {
	private final Player player;
	private final String uuid;
	private final InventoryView view;
	private boolean stop;
	public RequireSkullSkinThread(Player player, String uuid, InventoryView view){
		this.player = player;
		this.uuid = uuid;
		this.view = view;
		stop = false;
	}
	@Override
	public void run(){
		if(stop)
			return;
		execute();
	}
	
	public void execute(){
		Icon icon = view.getTopInventory().getIconElement(2);
		ItemStack head = icon.getItem();
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		List<String> lore = meta.getLore();
		try {
			URL api = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
	        BufferedReader in = new BufferedReader(new InputStreamReader(api.openStream()));
	        String inputline = in.readLine();
	        if(inputline != null){
	    		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
	        	JsonParser jsonParser = new JsonParser();
	        	JsonObject jo = (JsonObject) jsonParser.parse(inputline);
	        	JsonElement element = jo.get("properties");
	        	String texture = element.getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
	            profile.getProperties().put("textures", new Property("textures", texture));
	            Field profileField = null;
	            try {
	                profileField = meta.getClass().getDeclaredField("profile");
	                profileField.setAccessible(true);
	                profileField.set(meta, profile);
	            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
	                e1.printStackTrace();
	            }
	            lore.set(4, ChatColor.GREEN + "皮肤已加载");
	        }else{
	        	lore.set(4, ChatColor.RED + "无法加载该皮肤");
	        }
	        //icon.setInteractCode(null);
		} catch (Exception e) {
			lore.set(4, ChatColor.RED + "请求过于频繁 请稍后重试");
			icon.setInteractCode(new Interactable(){
				@Override
				public void onInteract(InventoryInteractEvent arg0) {
					lore.set(4, ChatColor.AQUA + "正在加载皮肤中...");
					meta.setLore(lore);
					head.setItemMeta(meta);
					view.updateContents(player);
					SearchInventory anvil = (SearchInventory) view.getTopInventory();
					anvil.getIconElement(2).setInteractCode(null);
					anvil.loadSkin(uuid, view);
				}
			});
		}
		if(stop)
			return;
		meta.setLore(lore);
        head.setItemMeta(meta);
		view.updateSlot(player, 2);
	}
	@Override
	public void setCancelled(boolean stop) {
		this.stop = stop;
	}
}

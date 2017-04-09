package me.nbcss.searchPlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.nbcss.quickGui.elements.Icon;
import me.nbcss.quickGui.elements.Interactable;
import me.nbcss.quickGui.elements.InventoryView;
import me.nbcss.quickGui.events.InventoryInteractEvent;
import me.nbcss.quickGui.utils.Util;
import me.nbcss.searchPlayer.MainClass;
import net.md_5.bungee.api.ChatColor;

public class SearchCacheThread extends MyThread {
	private final Player player;
	private final InventoryView view;
	private final String name;
	private boolean stop;
	public SearchCacheThread(String name, InventoryView view, Player player){
		stop = false;
		this.name = name;
		this.view = view;
		this.player = player;
	}
	@Override
	public void run(){
		if(stop)
			return;
		execute();
	}
	@Override
	public void setCancelled(boolean stop){
		this.stop = stop;
	}
	public void execute(){
		ItemStack head = Util.createItem(1, (short)0, Material.SKULL_ITEM, ChatColor.AQUA + "检索: " + ChatColor.RED + name, (String[]) null);
		Icon icon = new Icon(head);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(ChatColor.GOLD + "搜索完成");
		String uuid = null;
		String realName = null;
		String path = MainClass.getHandle().getServer().getWorldContainer().getAbsolutePath();
		File cache = new File(path, "usercache.json");
		if(!cache.exists()){
			lore.add(ChatColor.RED + "未找到数据");
			meta.setLore(lore);
			head.setItemMeta(meta);
			update(icon);
			return;
		}
		String json = "", line = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(cache));
			while((line = reader.readLine()) != null)
				json += line;
			reader.close();
		}catch(Exception e){
			lore.add(ChatColor.RED + "读取出错，请稍后重试");
			meta.setLore(lore);
			head.setItemMeta(meta);
			update(icon);
			return;
		}
		JsonArray array = new JsonParser().parse(json).getAsJsonArray();
		for(int i = 0; i < array.size(); i++){
			JsonObject obj = array.get(i).getAsJsonObject();
			String name = obj.get("name").getAsString();
			if(this.name.equalsIgnoreCase(name)){
				realName = name;
				uuid = obj.get("uuid").getAsString();
				break;
			}
		}
		if(uuid != null && realName != null){
			lore.add(ChatColor.GREEN + "已搜索到该玩家");
			lore.add(ChatColor.LIGHT_PURPLE + "name: " + ChatColor.GRAY + realName);
			lore.add(ChatColor.LIGHT_PURPLE + "uuid: " + ChatColor.GRAY + uuid);
			lore.add(ChatColor.AQUA + "点击以加载玩家皮肤");
			head.setDurability((short) 3);
			String uid = uuid;
			Interactable code = new Interactable(){
				@Override
				public void onInteract(InventoryInteractEvent e) {
					lore.set(4, ChatColor.AQUA + "正在加载皮肤中...");
					meta.setLore(lore);
					head.setItemMeta(meta);
					view.updateSlot(player, 2);
					SearchInventory anvil = (SearchInventory) view.getTopInventory();
					anvil.getIconElement(2).setInteractCode(null);
					anvil.loadSkin(uid, view);
				}
			};
			icon.setInteractCode(code);
		}else{
			lore.add(ChatColor.RED + "无法搜索到该玩家");
		}
		meta.setLore(lore);
		head.setItemMeta(meta);
		if(!stop)
			update(icon);
	}
	private void update(Icon icon){
		view.getTopInventory().setIconElement(2, icon);
		view.updateSlot(player, 2);
	}
}

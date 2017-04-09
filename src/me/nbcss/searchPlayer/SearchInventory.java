package me.nbcss.searchPlayer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.nbcss.quickGui.Operator;
import me.nbcss.quickGui.elements.Icon;
import me.nbcss.quickGui.elements.InventoryView;
import me.nbcss.quickGui.elements.inventories.AnvilInventory;
import me.nbcss.quickGui.events.CloseInventoryEvent;
import me.nbcss.quickGui.events.InventoryInteractEvent;
import me.nbcss.quickGui.utils.Util;
import net.md_5.bungee.api.ChatColor;

public class SearchInventory extends AnvilInventory {
	private static final String PREFIX = ChatColor.GREEN + "搜索✍" + ChatColor.GRAY + " | " + ChatColor.RESET;
	private static final String NETWORK = ChatColor.DARK_PURPLE + "网络搜索" + ChatColor.YELLOW + "模式";
	private static final String LOCAL = ChatColor.GREEN + "本地搜索" + ChatColor.YELLOW + "模式";
	private static final String CHECK_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
	private static final Icon INPUT = new Icon(Util.createItem(1, (short)0, Material.NAME_TAG, PREFIX, ChatColor.GOLD + "点击重置键入"));
	private static final Icon NETWORK_ICON = new Icon(Util.createItem(1, (short)0, Material.ENDER_CHEST, NETWORK, ChatColor.GOLD + "点击切换至" + LOCAL)){
		@Override
		public void onInteract(InventoryInteractEvent e){
			if(!(e.getClickedInventory() instanceof SearchInventory))
				return;
			SearchInventory inv = (SearchInventory) e.getClickedInventory();
			inv.setIconElement(1, LOCAL_ICON);
			inv.switchSearch();
		}
	};;
	private static final Icon LOCAL_ICON = new Icon(Util.createItem(1, (short)0, Material.CHEST, LOCAL, ChatColor.GOLD + "点击切换至" + NETWORK)){
		@Override
		public void onInteract(InventoryInteractEvent e){
			if(!(e.getClickedInventory() instanceof SearchInventory))
				return;
			SearchInventory inv = (SearchInventory) e.getClickedInventory();
			inv.setIconElement(1, NETWORK_ICON);
			inv.switchSearch();
		}
	};;
	private static final Icon OUT = new Icon(Util.createItem(1, (short)0, Material.BOOK_AND_QUILL, ChatColor.GOLD + "等待键入", (String[]) null));
	private static final Icon WAIT = new Icon(Util.createItem(1, (short)0, Material.FEATHER, ChatColor.GOLD + "需要至少3个字符", (String[]) null));
	private static final Icon ILLEGAL = new Icon(Util.createItem(1, (short)0, Material.BARRIER, ChatColor.RED + "不正确的字符", (String[]) null));
	private static final Icon LONG = new Icon(Util.createItem(1, (short)0, Material.BARRIER, ChatColor.RED + "字符过长-不超过16字符", (String[]) null));
	private final Player player;
	private String searching;
	private MyThread thread;
	private RequireSkullSkinThread skin;
	public SearchInventory(Player player){
		this.player = player;
		searching = null;
		this.setLeftInputIcon(INPUT);
		this.setRightInputIcon(LOCAL_ICON);
	}
	@Override
	public void onClose(CloseInventoryEvent e){
		cancelThread();
	}
	private void switchSearch(){
		if(searching == null)
			return;
		String name = searching;
		searching = null;
		search(PREFIX + name);
	}
	public void search(String line){
		InventoryView view = Operator.getOpenedInventoryView(player);
		if(!line.startsWith(PREFIX)){
			searching = null;
			cancelThread();
			updateInput(view);
			return;
		}
		String name = line.substring(PREFIX.length());
		if(searching != null)
			if(name.equalsIgnoreCase(searching)){
				view.updateSlot(player, 2);
				return;
			}
		cancelThread();
		if(line.equals(PREFIX)){
			searching = null;
			view.getTopInventory().setIconElement(2, OUT);
			view.updateSlot(player, 2);
			return;
		}
		if(name.length() < 3){
			searching = null;
			setIconElement(2, WAIT);
			view.updateSlot(player, 2);
			return;
		}if(name.length() > 16){
			searching = null;
			setIconElement(2, LONG);
			view.updateSlot(player, 2);
			return;
		}
		for(int i = 0; i < name.length(); i++){
			if(!CHECK_CHARS.contains(name.charAt(i) + "")){
				searching = null;
				setIconElement(2, ILLEGAL);
				view.updateSlot(player, 2);
				return;
			}
		}
		searching = name;
		ItemStack search = Util.createItem(1, (short)0, Material.COMPASS, ChatColor.AQUA + "检索: " + ChatColor.RED + name, ChatColor.GOLD + "搜索中, 请稍候...");
		setIconElement(2, new Icon(search));
		view.updateSlot(player, 2);
		MyThread task = null;
		if(getIconElement(1).equals(NETWORK_ICON))
			task = new SearchNetworkThread(name, view, player);
		else
			task = new SearchCacheThread(name, view, player);
		thread = task;
		task.start();
	}
	public void loadSkin(String uuid, InventoryView view){
		skin = new RequireSkullSkinThread(player, uuid, view);
		skin.start();
	}
	private void cancelThread(){
		if(thread != null)
			thread.setCancelled(true);
		thread = null;
		if(skin != null)
			skin.setCancelled(true);
		skin = null;
	}
	private void updateInput(InventoryView view){
		Icon icon = getIconElement(0);
		setIconElement(0, null);
		view.updateSlot(player, 0);
		setIconElement(0, icon);
		view.updateSlot(player, 0);
	}
}

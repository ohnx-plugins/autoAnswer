package ca.masonx.autoAnswer;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class AutoAnswer extends JavaPlugin implements Listener {
	private Map<String, String> lastChatMessage = new HashMap<String, String>();
	private Map<List<String>, String> messageMap = new HashMap<List<String>, String>();
	FileConfiguration config = this.getConfig();
	
	@Override
    public void onEnable() {
		// Register command
    	this.getCommand("autoanswer").setExecutor(this);
    	PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this, this);
        assertConfig();
    }
 
    @Override
    public void onDisable() {
    }
    
    private void assertConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
            	// Save default config
                saveDefaultConfig();
            } else {
            	// Load config
            	loadTriggers();
            }
        } catch (Exception e) {
        }
    }
    
    protected void loadTriggers() { // TRIGGERED
    	getLogger().info("Loading triggers...");
    	List<Map<?,?>> triggers = config.getMapList("triggers");
    	for (Map<?, ?> triggerMap : triggers) {
    		for (Object o : triggerMap.keySet()) {
    			String trigger = (String) o;
    			String replyValue = (String) triggerMap.get(o);
    			getLogger().info("Loaded trigger \"" + trigger +"\" with message \"" + replyValue);
    			messageMap.put(Arrays.asList(trigger.split(" ")), ChatColor.translateAlternateColorCodes('&', replyValue));
    		}
    	}
    	getLogger().info("Loaded triggers!");
    }
    
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
    	Player p = e.getPlayer();
    	String playerMessage = e.getMessage();
    	// players with permission to be ignored by autoAnswer will be ignored.
    	if (p.hasPermission("autoAnswer.ignore")) return;
    	try {
    		// player repeating the message will be allowed through
        	if (playerMessage.equals(lastChatMessage.get(p.getName()))) return;
        	for (List<String> msgs : messageMap.keySet()) {  // loop through list of all keyword strings
        		boolean containsAllKeywords = true;
        		for (String msg : msgs) { // check if player message has keywords
        			if (!playerMessage.contains(msg)) { // a keyword is not in the message so stop
        				containsAllKeywords = false;
        				break;
        			}
        		}
        		// message has all the keywords for a certain response
        		if (containsAllKeywords) {
        			p.sendMessage(messageMap.get(msgs));
        			e.setCancelled(true);
        			lastChatMessage.put(p.getName(), playerMessage);
        			break;
        		}
        	}
    	} catch (Exception err) {
    		err.printStackTrace();
    		return;
    	}    	
    }
    
	@Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (sender.hasPermission("autoAnswer.admin")) {
    		if (args.length == 0 || !(args[0].equalsIgnoreCase("reloadconfig"))) {
    			sender.sendMessage("To configure autoAnswer, please edit the config.yml file for now, then run /autoanswer reloadconfig");
    		} else {
    			loadTriggers();
    		}	
    	} else {
    		return false;
    	}
    	return true;
    }
}
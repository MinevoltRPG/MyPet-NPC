/*
 * This file is part of MyPet-NPC
 *
 * Copyright (C) 2011-2013 Keyle
 * MyPet-NPC is licensed under the GNU Lesser General Public License.
 *
 * MyPet-NPC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet-NPC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.Npc.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IconMenu implements Listener
{
    Inventory inventory;
    private int size;
    private OptionClickEventHandler handler;
    private Plugin plugin;
    private String[] optionNames;
    private List<Inventory> inventoryList = new ArrayList<Inventory>();

    public IconMenu(String name, int size, OptionClickEventHandler handler, Plugin plugin)
    {
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        optionNames = new String[size];
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        inventory = Bukkit.createInventory(null, size, name);
    }

    public IconMenu setOption(int position, ItemStack icon, String name, String[] lore)
    {
        setItemNameAndLore(icon, name, lore);
        optionNames[position] = name;
        inventory.setItem(position, icon);
        return this;
    }

    public int addOption(ItemStack icon, String name, String[] lore)
    {
        for (int i = 0 ; i < size ; i++)
        {
            if (inventory.getContents()[i] == null)
            {
                setItemNameAndLore(icon, name, lore);
                optionNames[i] = name;
                inventory.setItem(i, icon);
                return i;
            }
        }
        return -1;
    }

    public int addOption(ItemStack icon, String name, List<String> loreList)
    {
        String[] lore = new String[loreList.size()];
        for (int i = 0 ; i < lore.length ; i++)
        {
            lore[i] = loreList.get(i);
        }
        for (int i = 0 ; i < size ; i++)
        {
            if (inventory.getContents()[i] == null)
            {
                setItemNameAndLore(icon, name, lore);
                optionNames[i] = name;
                inventory.setItem(i, icon);
                return i;
            }
        }
        return -1;
    }

    public void open(Player player)
    {
        Inventory openInv = player.openInventory(inventory).getTopInventory();
        if (openInv == null)
        {
            return;
        }
        inventoryList.add(openInv);
    }

    public void destroy()
    {
        HandlerList.unregisterAll(this);
        handler = null;
        plugin = null;
        inventory.clear();
        inventoryList.clear();
    }

    @EventHandler
    void onPluginDisable(PluginDisableEvent event)
    {
        if (event.getPlugin().equals(plugin) && inventory != null)
        {
            for (HumanEntity viewer : inventory.getViewers())
            {
                viewer.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onInventoryClick(InventoryClickEvent event)
    {
        if (inventoryList.contains(event.getInventory()))
        {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && optionNames[slot] != null)
            {
                OptionClickEvent e = new OptionClickEvent((Player) event.getWhoClicked(), slot, optionNames[slot]);
                handler.onOptionClick(e);
                if (e.willClose())
                {
                    final Player p = (Player) event.getWhoClicked();
                    p.closeInventory();
                }
                if (e.willDestroy())
                {
                    destroy();
                }
            }
        }
    }

    @EventHandler
    void onInventoryClose(InventoryCloseEvent event)
    {
        inventoryList.remove(event.getInventory());
    }

    private ItemStack setItemNameAndLore(ItemStack item, String name, String[] lore)
    {
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(lore));
        item.setItemMeta(im);
        return item;
    }

    public interface OptionClickEventHandler
    {
        public void onOptionClick(OptionClickEvent event);
    }

    public class OptionClickEvent
    {
        private Player player;
        private int position;
        private String name;
        private boolean close;
        private boolean destroy;

        public OptionClickEvent(Player player, int position, String name)
        {
            this.player = player;
            this.position = position;
            this.name = name;
            this.close = true;
            this.destroy = false;
        }

        public Player getPlayer()
        {
            return player;
        }

        public int getPosition()
        {
            return position;
        }

        public String getName()
        {
            return name;
        }

        public boolean willClose()
        {
            return close;
        }

        public boolean willDestroy()
        {
            return destroy;
        }

        public void setWillClose(boolean close)
        {
            this.close = close;
        }

        public void setWillDestroy(boolean destroy)
        {
            this.destroy = destroy;
        }
    }
}

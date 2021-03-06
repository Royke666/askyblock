/*******************************************************************************
 * This file is part of ASkyBlock.
 *
 *     ASkyBlock is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ASkyBlock is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.wasteofplastic.askyblock.nms.v1_8_R1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.NBTTagString;
import net.minecraft.server.v1_8_R1.TileEntityFlowerPot;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.wasteofplastic.askyblock.nms.NMSAbstraction;
import com.wasteofplastic.org.jnbt.CompoundTag;
import com.wasteofplastic.org.jnbt.ListTag;
import com.wasteofplastic.org.jnbt.StringTag;
import com.wasteofplastic.org.jnbt.Tag;

public class NMSHandler implements NMSAbstraction {

    @Override
    public void setBlockSuperFast(Block b, int blockId, byte data, boolean applyPhysics) {
        net.minecraft.server.v1_8_R1.World w = ((CraftWorld) b.getWorld()).getHandle();
        net.minecraft.server.v1_8_R1.Chunk chunk = w.getChunkAt(b.getX() >> 4, b.getZ() >> 4);
        BlockPosition bp = new BlockPosition(b.getX(), b.getY(), b.getZ());
        int combined = blockId + (data << 12);
        IBlockData ibd = net.minecraft.server.v1_8_R1.Block.getByCombinedId(combined);
        chunk.a(bp, ibd);
        if (applyPhysics) {
            net.minecraft.server.v1_8_R1.Block block = chunk.getType(bp);
            w.update(bp, block);
        }      

    }

    @Override
    public ItemStack setBook(Tag item) {
        ItemStack chestItem = new ItemStack(Material.WRITTEN_BOOK);
        //Bukkit.getLogger().info("item data");
        //Bukkit.getLogger().info(item.toString());

        Map<String,Tag> contents = (Map<String,Tag>) ((CompoundTag) item).getValue().get("tag").getValue();
        //BookMeta bookMeta = (BookMeta) chestItem.getItemMeta();
        String author = ((StringTag)contents.get("author")).getValue();
        //Bukkit.getLogger().info("Author: " + author);
        //bookMeta.setAuthor(author);
        String title = ((StringTag)contents.get("title")).getValue();
        //Bukkit.getLogger().info("Title: " + title);
        //bookMeta.setTitle(title);

        Map<String,Tag> display = (Map<String, Tag>) (contents.get("display")).getValue();
        List<Tag> loreTag = ((ListTag)display.get("Lore")).getValue();
        List<String> lore = new ArrayList<String>();
        for (Tag s: loreTag) {
            lore.add(((StringTag)s).getValue());
        }
        //Bukkit.getLogger().info("Lore: " + lore);
        net.minecraft.server.v1_8_R1.ItemStack stack = CraftItemStack.asNMSCopy(chestItem); 
        // Pages
        NBTTagCompound tag = new NBTTagCompound(); //Create the NMS Stack's NBT (item data)
        tag.setString("title", title); //Set the book's title
        tag.setString("author", author);
        NBTTagList pages = new NBTTagList();
        List<Tag> pagesTag = ((ListTag)contents.get("pages")).getValue();
        for (Tag s: pagesTag) {
            pages.add(new NBTTagString(((StringTag)s).getValue()));
        }
        tag.set("pages", pages); //Add the pages to the tag
        stack.setTag(tag); //Apply the tag to the item
        chestItem = CraftItemStack.asCraftMirror(stack); 
        ItemMeta bookMeta = (ItemMeta) chestItem.getItemMeta();
        bookMeta.setLore(lore);
        chestItem.setItemMeta(bookMeta);
        return chestItem;
    }

    /* (non-Javadoc)
     * @see com.wasteofplastic.askyblock.nms.NMSAbstraction#setBlock(org.bukkit.block.Block, org.bukkit.inventory.ItemStack)
     * Credis: Mister_Frans (THANK YOU VERY MUCH !)
     */
    @Override
    public void setFlowerPotBlock(Block block, ItemStack itemStack) {
        Location loc = block.getLocation();
        CraftWorld cw = (CraftWorld)block.getWorld();
        BlockPosition bp = new BlockPosition(loc.getX(), loc.getY(), loc.getZ());
        TileEntityFlowerPot te = (TileEntityFlowerPot)cw.getHandle().getTileEntity(bp);
        //Bukkit.getLogger().info("Debug: flowerpot materialdata = " + (new ItemStack(potItem, 1,(short) potItemData).toString()));
        net.minecraft.server.v1_8_R1.ItemStack cis = CraftItemStack.asNMSCopy(itemStack);
        te.a(cis.getItem(), cis.getData());
        te.update();
        cw.getHandle().notify(bp);
        Chunk ch = loc.getChunk();
        cw.refreshChunk(ch.getX(), ch.getZ());  
    }

}

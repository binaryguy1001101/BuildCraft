/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.builders.blueprints.Blueprint;
import buildcraft.core.Box;
import buildcraft.core.DefaultProps;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.NetworkData;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;
import buildcraft.core.utils.Utils;

public class TileArchitect extends TileBuildCraft implements IInventory {

	public @NetworkData
	Box box = new Box();
	private ItemStack items[] = new ItemStack[2];
	private boolean isComputing = false;
	public int computingTime = 0;

	@NetworkData
	public String name = "";

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!worldObj.isRemote && isComputing) {
			if (computingTime < 200) {
				computingTime++;
			} else {
				createBlueprint();
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!box.isInitialized()) {
			IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord, zCoord);

			if (a != null) {
				box.initialize(a);
				a.removeFromWorld();

			}
		}

		if (!worldObj.isRemote && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}

		sendNetworkUpdate();
	}

	public void createBlueprint() {
		if (!box.isInitialized() || items[1] != null)
			return;

		Blueprint blueprint;

		if (items[0].getItem() instanceof ItemBlueprintTemplate) {
			blueprint = createMaskBlueprint(box);
		} else {
			blueprint = createStandardBlueprint(box);
		}

		if (!name.equals("")) {
			blueprint.setName(name);
		}

		blueprint.anchorX = xCoord - (int) box.xMin;
		blueprint.anchorY = yCoord - (int) box.yMin;
		blueprint.anchorZ = zCoord - (int) box.zMin;


		blueprint.anchorOrientation = ForgeDirection.getOrientation(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));

		BuildCraftBuilders.serverDB.add(blueprint);

		setInventorySlotContents(1, ItemBlueprint.getBlueprintItem(blueprint));
		setInventorySlotContents(0, null);
	}

	private Blueprint createMaskBlueprint(Box box) {
		Blueprint blueprint = Blueprint.create((int) box.sizeX(), (int) box.sizeY(), (int) box.sizeZ());

		/*for (int x = box.xMin; x <= box.xMax; ++x) {
			for (int y = box.yMin; y <= box.yMax; ++y) {
				for (int z = box.zMin; z <= box.zMax; ++z) {
					if (worldObj.isAirBlock(x, y, z))
						continue;
					Block block = Block.blocksList[worldObj.getBlockId(x, y, z)];
					if (block == null)
						continue;

					blueprint.setSchematic(x - box.xMin, y - box.yMin, z - box.zMin, worldObj, block);
				}
			}
		}*/

		return blueprint;
	}

	private Blueprint createStandardBlueprint(Box box) {
		Blueprint blueprint = Blueprint.create((int) box.sizeX(), (int) box.sizeY(), (int) box.sizeZ());

		for (int x = (int) box.xMin; x <= box.xMax; ++x) {
			for (int y = (int) box.yMin; y <= box.yMax; ++y) {
				for (int z = (int) box.zMin; z <= box.zMax; ++z) {
					if (worldObj.isAirBlock(x, y, z)) {
						continue;
					}

					Block block = worldObj.getBlock(x, y, z);

					if (block == null) {
						continue;
					}

					blueprint.setSchematic(x - (int) box.xMin, y - (int) box.yMin, z - (int) box.zMin, worldObj, block);
				}
			}
		}

		return blueprint;
	}

	@RPC (RPCSide.SERVER)
	public void handleClientInput(char c) {
		if (c == 8) {
			if (name.length() > 0) {
				name = name.substring(0, name.length() - 1);
			}
		} else if (Character.isLetterOrDigit(c) || c == ' ') {
			if (name.length() < BuildCraftBuilders.MAX_BLUEPRINTS_NAME_SIZE) {
				name += c;
			}
		}

		RPCHandler.rpcBroadcastPlayersAtDistance(this, "setName", DefaultProps.NETWORK_UPDATE_RANGE, name);
	}

	@RPC
	public void setName (String name) {
		this.name = name;
	}

	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return items[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result;
		if (items[i] == null) {
			result = null;
		} else if (items[i].stackSize > j) {
			result = items[i].splitStack(j);
		} else {
			ItemStack tmp = items[i];
			items[i] = null;
			result = tmp;
		}

		initializeComputing();

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		items[i] = itemstack;

		initializeComputing();

	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (items[slot] == null)
			return null;
		ItemStack toReturn = items[slot];
		items[slot] = null;
		return toReturn;
	}

	@Override
	public String getInventoryName() {
		return "Template";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot == 0 && stack != null && stack.getItem() == BuildCraftBuilders.blueprintItem;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		computingTime = nbttagcompound.getInteger("computingTime");
		isComputing = nbttagcompound.getBoolean("isComputing");

		if (nbttagcompound.hasKey("box")) {
			box.initialize(nbttagcompound.getCompoundTag("box"));
		}

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items", Utils.NBTTag_Types.NBTTagCompound.ordinal());
		items = new ItemStack[getSizeInventory()];
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 0xff;
			if (j >= 0 && j < items.length) {
				items[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}

		name = nbttagcompound.getString("name");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setInteger("computingTime", computingTime);
		nbttagcompound.setBoolean("isComputing", isComputing);

		if (box.isInitialized()) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbttagcompound.setTag("box", boxStore);
		}

		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				items[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("Items", nbttaglist);
		nbttagcompound.setString("name", name);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		if (box.isInitialized()) {
			box.deleteLasers();
		}
	}

	private void initializeComputing() {
		if (!box.isInitialized())
			return;
		else if (!isComputing) {
			if (items[0] != null && items[0].getItem() instanceof ItemBlueprint && items[1] == null) {
				isComputing = true;
				computingTime = 0;
			} else {
				isComputing = false;
				computingTime = 0;
			}
		} else {
			if (items[0] == null || !(items[0].getItem() instanceof ItemBlueprint)) {
				isComputing = false;
				computingTime = 0;
			}
		}
	}

	public int getComputingProgressScaled(int i) {
		return (computingTime * i) / 200;
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) throws IOException {
		boolean initialized = box.isInitialized();

		super.handleDescriptionPacket(packet);

		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		boolean initialized = box.isInitialized();

		super.handleUpdatePacket(packet);

		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

}

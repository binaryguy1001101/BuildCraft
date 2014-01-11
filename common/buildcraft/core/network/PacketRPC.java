package buildcraft.core.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import org.bouncycastle.crypto.util.Pack;

import cpw.mods.fml.common.network.Player;

public class PacketRPC extends BuildCraftPacket {

	public TileEntity tile;

	byte [] contents;

	public EntityPlayer sender;

	public PacketRPC () {

	}

	public PacketRPC (byte [] bytes) {
		contents = bytes;
	}

	public void setTile (TileEntity aTile) {
		tile = aTile;
	}

	@Override
	public int getID() {
		return PacketIds.RPC;
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		RPCMessageInfo info = new RPCMessageInfo();
		info.sender = sender;

		RPCHandler.receiveRPC(tile, info, data);
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.write(contents);
	}

}
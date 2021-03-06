package fr.eno.farmutils.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

public class ItemEnergyStorage extends Item implements IEnergyStorage, INBTSerializable<NBTTagCompound>
{
	private int maxStorage;
	private int energyStored;
	private int maxEnergyReceive;
	private int maxEnergyExtract;
	
	public ItemEnergyStorage(int maxStorage, int maxEnergyReceive, int maxEnergyExtract)
	{
		this.maxStorage = maxStorage;
		this.maxEnergyReceive = maxEnergyReceive;
		this.maxEnergyExtract = maxEnergyExtract;
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add(TextFormatting.LIGHT_PURPLE + "Energy : " + this.getEnergyStored() + "/" + this.getMaxEnergyStored());
	}
	
	public boolean isFullCharged()
	{
		return this.getEnergyStored() >= this.getMaxEnergyStored();
	}
	
	public int getMaxExtract()
	{
		return this.maxEnergyExtract;
	}
	
	public int getMaxReceive()
	{
		return this.maxEnergyReceive;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		this.energyStored++;
		return this.maxEnergyReceive;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		if(this.getEnergyStored() > 1)
			this.energyStored--;
		
		return this.maxEnergyExtract;
	}

	@Override
	public int getEnergyStored()
	{
		return this.energyStored;
	}

	@Override
	public int getMaxEnergyStored()
	{
		return this.maxStorage;
	}

	@Override
	public boolean canExtract()
	{
		return this.energyStored > maxEnergyExtract;
	}

	@Override
	public boolean canReceive()
	{
		return this.energyStored < this.maxStorage - this.maxEnergyReceive;
	}

	@Override
	public NBTTagCompound serializeNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("EnergyStored", this.getEnergyStored());
		nbt.setInteger("MaxStorage", this.getMaxEnergyStored());
		nbt.setInteger("MaxReceive", this.maxEnergyReceive);
		nbt.setInteger("MaxExtract", this.maxEnergyExtract);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt)
	{
		this.energyStored = nbt.getInteger("EnergyStored");
		this.maxStorage = nbt.getInteger("MaxStorage");
		this.maxEnergyReceive = nbt.getInteger("MaxReceive");
		this.maxEnergyExtract = nbt.getInteger("MaxExtract");		
	}
}

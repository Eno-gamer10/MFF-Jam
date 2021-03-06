package fr.eno.farmutils.tileentity;

import fr.eno.farmutils.items.ItemEnergyStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.IEnergyStorage;

public class TileCharger extends TileEntityLockable implements IEnergyStorage, ITickable
{
	private int energyStored;
	private int maxEnergy;
	
	private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(2, ItemStack.EMPTY);
	
	public TileCharger() {}
	
	public TileCharger(World world)
	{
		this.setWorld(world);
		this.maxEnergy = 1000;
		this.energyStored = 0;
	}
	
	public static void registerFixes(DataFixer fixer)
    {
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileMilker.class, new String[] {"Items"}));
    }
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		this.stacks = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, this.stacks);
		this.energyStored = 0;
		this.maxEnergy = 0;
		this.energyStored = compound.getInteger("EnergyStored");
		this.maxEnergy = compound.getInteger("MaxStorage");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);		
		ItemStackHelper.saveAllItems(compound, stacks);
		compound.setInteger("EnergyStored", this.energyStored);
		compound.setInteger("MaxStorage", this.maxEnergy);
		return compound;
	}

	@Override
	public void update()
	{
		if(this.getWorld().canBlockSeeSky(this.getPos()) && !this.getWorld().isRainingAt(getPos()) && this.getWorld().isDaytime() && !this.getWorld().isThundering())
		{
			if(this.getEnergyStored() < this.getMaxEnergyStored())
			{
				this.receiveEnergy(1, false);
			}
		}
		
		if(this.getStackInSlot(0) != null)
		{
			if(!(this.getStackInSlot(0).getItem() instanceof ItemEnergyStorage))
				return;
			
			ItemStack stack = this.getStackInSlot(0);
			ItemEnergyStorage item = (ItemEnergyStorage) stack.getItem();
			
			if(item.isFullCharged())
			{
				this.setInventorySlotContents(0, ItemStack.EMPTY);
				this.setInventorySlotContents(1, stack);
				return;
			}
			
			if(this.extractEnergy(2, true) > 0 && !item.isFullCharged())
			{
				item.receiveEnergy(1, false);
				this.extractEnergy(2, false);
				return;
			}
		}
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
	{
		return true;
	}
	
	@Override
	public int getSizeInventory()
	{
		return stacks.size();
	}

	@Override
	public boolean isEmpty()
	{
		for (ItemStack stack : this.stacks)
		{
			if (!stack.isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		return this.stacks.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		return ItemStackHelper.getAndSplit(this.stacks, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index)
	{
		return ItemStackHelper.getAndRemove(stacks, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		this.stacks.set(index, stack);

		if (stack.getCount() > this.getInventoryStackLimit())
		{
			stack.setCount(this.getInventoryStackLimit());
		}
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	public boolean isUsableByPlayer(EntityPlayer player)
	{
		return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player)	{}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return stack.isItemStackDamageable() && stack.getItemDamage() > 0;
	}

	public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return this.energyStored;
            case 1:
                return this.maxEnergy;                	
        }
        
		return 1;
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                this.energyStored = value;
                break;
            case 1:
                this.maxEnergy = value;
                break;
        }
    }

    public int getFieldCount()
    {
        return 2;
    }

	@Override
	public void clear()
	{
		for (int i = 0; i < this.stacks.size(); i++)
		{
			this.stacks.set(i, ItemStack.EMPTY);
		}
	}

	@Override
	public String getName()	{ return null; }

	@Override
	public boolean hasCustomName() { return false; }

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) { return null; }

	@Override
	public String getGuiID() { return null; }

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		this.energyStored += maxReceive;
		return this.maxEnergy - this.energyStored < maxReceive ? maxReceive : this.maxEnergy - this.energyStored;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		if(canExtract(maxExtract))
			this.energyStored -= maxExtract;
		
		return this.energyStored < maxExtract ? this.energyStored : maxExtract;
	}

	@Override
	public int getEnergyStored()
	{
		return this.energyStored;
	}

	@Override
	public int getMaxEnergyStored()
	{
		return this.maxEnergy;
	}
	
	public boolean canExtract(int energy)
	{
		return this.energyStored >= energy;
	}

	@Override
	public boolean canExtract()
	{
		return this.energyStored > 0;
	}

	@Override
	public boolean canReceive()
	{
		return this.energyStored < this.maxEnergy;
	}
}

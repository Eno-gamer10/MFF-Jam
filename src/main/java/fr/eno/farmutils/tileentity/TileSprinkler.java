package fr.eno.farmutils.tileentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.eno.farmutils.block.BlockBetterThanWater;
import fr.eno.farmutils.block.BlockSprinkler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class TileSprinkler extends TileEntity implements ITickable
{
	private int tick;
	private final Random random = new Random();
	
	public TileSprinkler() {}
	
	public TileSprinkler(World world)
	{
		this.setWorld(world);
		this.tick = 0;
	}
	
	@Override
	public void update()
	{
		if(world.isRemote) return;
		
		tick++;
		
		if(world.getBlockState(this.getPos().down()).getBlock() instanceof BlockBetterThanWater)
		{
			if(tick >= 10)
			{
				int value = world.getBlockState(pos).getValue(BlockSprinkler.ROTATION).intValue();
				this.world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockSprinkler.ROTATION, value == 15 ? 0 : ++value));
				this.world.spawnParticle(EnumParticleTypes.DRIP_WATER, pos.getX() + 0.5d, pos.up().getY() + 0.5d, pos.getZ() + 0.5d, 0, 0, 0, 10, 100);
				tick = 0;
			}
			
			if(tick == 5)
			{
				BlockPos pos = new BlockPos(0, 0, 0);
				
				pos = getRandomCropsPos();
				
				this.getWorld().scheduleUpdate(pos, this.getWorld().getBlockState(pos).getBlock(), 2);
			}
		}		
	}
	
	private BlockPos getRandomCropsPos()
	{
		List<BlockPos> list = getCropsPos();
		
		if(!list.isEmpty() && list != null)
			return list.get(random.nextInt(list.size() - 1));
		
		return new BlockPos(0, 0, 0);
	}
	

	private List<BlockPos> getCropsPos()
	{
		List<BlockPos> pos = new ArrayList<BlockPos>();
		
		for(BlockPos positions : this.getCropsPositions())
		{
			if(this.getWorld().getBlockState(positions.up()).getBlock() instanceof IPlantable)
			{
				pos.add(positions.up());
			}
		}
		
		return pos;
	}
	
	private List<BlockPos> getCropsPositions()
	{
		List<BlockPos> list = new ArrayList<BlockPos>();
		
		BlockPos.getAllInBox(this.getPos().add(-5, 0, -5), this.getPos().add(5, 0, 5)).forEach(blockPos ->
		{
			Block block = this.getWorld().getBlockState(blockPos).getBlock();
			if(block instanceof BlockCrops || block instanceof BlockStem)
			{
				list.add(blockPos);
			}
		});
		
		return list;
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
	{
		return false;
	}
}

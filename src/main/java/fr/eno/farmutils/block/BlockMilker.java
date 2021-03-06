package fr.eno.farmutils.block;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import fr.eno.farmutils.FarmingUtilities;
import fr.eno.farmutils.References;
import fr.eno.farmutils.tileentity.TileMilker;
import fr.eno.farmutils.utils.Tabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMilker extends Block
{
	public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");
	
	public BlockMilker()
	{
		super(Material.GROUND);
		this.setRegistryName(References.MOD_ID, "milker");
		this.setTranslationKey(this.getRegistryName().getPath());
		this.setDefaultState(this.blockState.getBaseState().withProperty(TRIGGERED, Boolean.valueOf(false)));
		this.setCreativeTab(Tabs.BLOCKS);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.addAll(Arrays.asList("With a cow above this block and a restone impulsion you can make milk.",
				"Put a bucket in the 0-8 slots, give him a redstone impulsion, and wait a cow pass above to have milk :)"));
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        if(!world.isRemote && world.canSeeSky(pos))
        {
        	TileMilker milker = (TileMilker) world.getTileEntity(pos);
        	
        	milker.milkCow();
        }
    }
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
            TileEntity tileentity = world.getTileEntity(pos);

            if (tileentity instanceof TileMilker)
            {
                player.openGui(FarmingUtilities.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }

            return true;
        }
    }
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        return true;
    }
	
	@Override
	public int tickRate(World worldIn)
    {
        return 1;
    }
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up());
        boolean flag1 = ((Boolean)state.getValue(TRIGGERED)).booleanValue();
        
        if (flag && !flag1)
        {
            TileMilker milker = (TileMilker) worldIn.getTileEntity(pos);
        	
        	milker.milkCow();
        	
            worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(true)), 4);
        }
        else if (!flag && flag1)
        {
            worldIn.setBlockState(pos, state.withProperty(TRIGGERED, Boolean.valueOf(false)), 4);
        }
    }
	
	public IBlockState getStateFromMeta(boolean meta)
    {
        return this.getDefaultState().withProperty(TRIGGERED, Boolean.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state)
    {
    	if(state.getValue(TRIGGERED).booleanValue())
    		return 1;
    	else
    		return 0;
    }
    
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {TRIGGERED});
    }
    
    @Override
	public TileEntity createTileEntity(World world, IBlockState state)
    {
		return new TileMilker(world);
    }
 
    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }
}

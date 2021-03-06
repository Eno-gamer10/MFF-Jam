package fr.eno.farmutils.items;

import fr.eno.farmutils.References;
import fr.eno.farmutils.utils.Tabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemPoweredHoe extends ItemEnergyStorage
{	
	public ItemPoweredHoe()
	{
		super(200, 2, 0);
		this.setRegistryName(References.MOD_ID, "powered_hoe");
		this.setTranslationKey(this.getRegistryName().getPath());
		this.setCreativeTab(Tabs.ITEMS);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack itemstack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack) || this.getEnergyStored() < 1)
        {
            return EnumActionResult.FAIL;
        }
        else
        {
            int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(itemstack, player, worldIn, pos);
            if (hook != 0) return hook > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;

            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();
            
            for(BlockPos posi : BlockPos.getAllInBox(pos.add(1, 0, 1), pos.add(-1, 0, -1)))
        	{
            	if (facing != EnumFacing.DOWN && worldIn.isAirBlock(posi.up()))
                {
                    if (block == Blocks.GRASS || block == Blocks.GRASS_PATH)
                    {
                        this.setBlock(itemstack, player, worldIn, posi, Blocks.FARMLAND.getDefaultState());
                        return EnumActionResult.SUCCESS;
                    }

                    if (block == Blocks.DIRT)
                    {
                        switch ((BlockDirt.DirtType)iblockstate.getValue(BlockDirt.VARIANT))
                        {
                            case DIRT:
                                this.setBlock(itemstack, player, worldIn, posi, Blocks.FARMLAND.getDefaultState());
                                return EnumActionResult.SUCCESS;
                            case COARSE_DIRT:
                                this.setBlock(itemstack, player, worldIn, posi, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
                                return EnumActionResult.SUCCESS;
    						case PODZOL:
    							this.setBlock(itemstack, player, worldIn, posi, Blocks.FARMLAND.getDefaultState());
                                return EnumActionResult.SUCCESS;
    						default:
    							break;
                        }
                    }
                }
        	}
            
            return EnumActionResult.PASS;
        }
	}
	
	protected void setBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos, IBlockState state)
    {
        world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (!world.isRemote)
        {
        	world.setBlockState(pos, state, 11);
            stack.damageItem(1, player);
            this.extractEnergy(1, false);
        }
    }
}

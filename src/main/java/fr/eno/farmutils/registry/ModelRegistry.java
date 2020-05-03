package fr.eno.farmutils.registry;

import fr.eno.farmutils.References;
import fr.eno.farmutils.init.InitItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT, modid = References.MOD_ID)
public class ModelRegistry
{	
	@SubscribeEvent
	public static void registerItemRenders(ModelRegistryEvent event)
	{		
		for(Item item : InitItems.ITEMS)
		{
			if(!item.getHasSubtypes())
			{
				registerItemRender(item, 0);
			}
			else
			{
				NonNullList<ItemStack> list = NonNullList.<ItemStack>create();
				item.getSubItems(item.getCreativeTab(), list);
				list.forEach(is -> registerSubItemRender(is.getItem(), is.getMetadata()));
			}
		}
	}
	
	private static void registerItemRender(Item item, int metadata)
	{
		ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
	
	private static void registerSubItemRender(Item item, int metadata)
	{
		ModelLoader.setCustomModelResourceLocation(item, metadata, new ModelResourceLocation(References.MOD_ID + ":" + item.getTranslationKey(new ItemStack(item, 1, metadata)).substring(5).replace(".name", ""), "inventory"));
	}
}

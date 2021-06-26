package ca.thewarmfuzzy.practicalcombat.init;

import ca.thewarmfuzzy.practicalcombat.common.item.ItemKnuckleSandwich;
import ca.thewarmfuzzy.practicalcombat.common.lib.ItemsPC;
import ca.thewarmfuzzy.practicalcombat.common.util.inventory.ItemGroupPC;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ItemsPC.KNUCKLE_SANDWICH = registerItem(ModIDs.ID_ITEM_KNUCKLE_SANDWICH, new ItemKnuckleSandwich(new Item.Properties().group(ItemGroupPC.instance)));

    }

    public static <T extends Item> T registerItem(String name, T item) {
        item.setRegistryName(name);
        ForgeRegistries.ITEMS.register(item);
        return item;
    }

}

package ca.thewarmfuzzy.practicalcombat.common.util.inventory;

import ca.thewarmfuzzy.practicalcombat.common.item.ItemKnuckleSandwich;
import ca.thewarmfuzzy.practicalcombat.common.lib.ItemsPC;
import ca.thewarmfuzzy.practicalcombat.init.ModIDs;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ItemGroupPC extends ItemGroup {

    public static final ItemGroupPC instance = new ItemGroupPC(ItemGroup.GROUPS.length, ModIDs.ID_ITEM_GROUP_PRACTICAL_COMBAT);

    private ItemGroupPC(int index, String label) {
        super(index, label);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(ItemsPC.KNUCKLE_SANDWICH);
    }

}

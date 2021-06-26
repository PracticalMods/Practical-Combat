package ca.thewarmfuzzy.practicalcombat.common.item;

import ca.thewarmfuzzy.practicalcombat.core.PracticalCombat;
import ca.thewarmfuzzy.practicalcombat.init.ModIDs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemKnuckleSandwich extends Item {

    static final int COOLDOWN = 10;

    public ItemKnuckleSandwich(Item.Properties properties) {
        super(properties
                .maxStackSize(64)
                .rarity(Rarity.UNCOMMON));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        if (player.getCooldownTracker().hasCooldown(this)) {
            return super.onItemRightClick(world, player, hand);
        }

        Integer stackSize = player.getHeldItem(hand).getCount();

        // If this doesn't come first it can drop after death
        // You're supposed to eat it like a champ
        player.getCooldownTracker().setCooldown(this, COOLDOWN);
        player.getHeldItem(hand).shrink(stackSize);

        // Why are you hitting yourself?
        player.attackEntityFrom(new EntityDamageSource("mob", player), player.getMaxHealth() * stackSize);

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add((new TranslationTextComponent(String.format("tooltip.%s.%s", PracticalCombat.MOD_ID, ModIDs.ID_ITEM_KNUCKLE_SANDWICH))).mergeStyle(TextFormatting.GRAY));
    }

}

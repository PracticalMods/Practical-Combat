package ca.thewarmfuzzy.practicalcombat.event;

import ca.thewarmfuzzy.practicalcombat.core.PracticalCombat;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatHandler {

    public static final UUID MOVE_SPEED_EFFECT_UUID = UUID.fromString("be4ebcaa-d52d-11eb-b8bc-0242ac130003");

    public static DamageSource[] DisabledDamageSources= {DamageSource.STARVE};

    /**
     * Called when any player is damaged. This occurs after damage mitigation calculations.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDamage(LivingDamageEvent event) {

        //Only run if a player was injured
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        //Cancel starvation
        DamageSource source = event.getSource();
        if ("starve" == source.damageType) event.setCanceled(true);

        //Ignore if the source wasn't an entity
        if (source.getTrueSource() == null) return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        float amount = event.getAmount();

        //If the player is full health (or more)
        if (player.getHealth() >= player.getMaxHealth()) {

            if (amount >= player.getHealth()) {
                PracticalCombat.LOGGER.info("One shot protection!");
                event.setAmount(player.getHealth() - 0.5f);
            }

        }

    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttack(LivingAttackEvent event) {
        for (DamageSource source: DisabledDamageSources) {
            if (source.damageType == event.getSource().damageType) {
                event.setCanceled(true);
                break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {

        //Ignore tick if it does not apply
        if (event.phase != TickEvent.Phase.END) return;
        if (null == event.player) return;

        PlayerEntity player = event.player;

        //Set slow
        if (player.ticksExisted % 5 == 0) {
            applyMoveSpeedModifier(player);
        }
    }


    public static void applyMoveSpeedModifier(PlayerEntity player) {
        ModifiableAttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        Integer foodLevel = player.getFoodStats().getFoodLevel();
        Double moveSpeedScalar = 0d;

        if (0 >= foodLevel) {
            moveSpeedScalar = -0.25d;
        } else if (2 >= foodLevel) {
            moveSpeedScalar = -0.13d;
        } else if (4 >= foodLevel) {
            moveSpeedScalar = -0.05d;
        } else if (18 >= foodLevel) {
            moveSpeedScalar = 0.0d;
        } else {
            moveSpeedScalar = 0.05d;
        }

        if (null == moveSpeed.getModifier(MOVE_SPEED_EFFECT_UUID)) {
            AttributeModifier moveSpeedModifier = new AttributeModifier(MOVE_SPEED_EFFECT_UUID, "Hunger Slow from Practical Combat", moveSpeedScalar, AttributeModifier.Operation.MULTIPLY_TOTAL);
            moveSpeed.applyPersistentModifier(moveSpeedModifier);
        } else if (Math.abs(moveSpeed.getModifier(MOVE_SPEED_EFFECT_UUID).getAmount() - moveSpeedScalar) >= 0.0001) {
            moveSpeed.removeModifier(MOVE_SPEED_EFFECT_UUID);
            AttributeModifier moveSpeedModifier = new AttributeModifier(MOVE_SPEED_EFFECT_UUID, "Hunger Slow from Practical Combat", moveSpeedScalar, AttributeModifier.Operation.MULTIPLY_TOTAL);
            moveSpeed.applyPersistentModifier(moveSpeedModifier);
        }

//        PracticalCombat.LOGGER.info(String.format("Move speed: %f", moveSpeed.getValue()));
    }


}

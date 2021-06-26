package ca.thewarmfuzzy.practicalcombat.event;

import ca.thewarmfuzzy.practicalcombat.core.PracticalCombat;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CombatHandlerClient {

    /**
     * The maximum number of ticks for a quick key event.
     */
    public static final Integer MAX_KEY_TICK_DURATION = 4;

    /**
     * The minimum food level required to hop.
     */
    public static final Integer MIN_HOP_FOOD_LEVEL = 6;

    /**
     * The minimum food level required to hop.
     */
    public static final Boolean HOP_ON_KEY_DOWN = true;

    /**
     * The id of the last recorded key press.
     */
    private static Integer lastKey = 0;

    /**
     * The number of time a key has been pressed and released consecutively within MAX_KEY_TICK_DURATION.
     */
    private static Hashtable<Integer, Integer> keyReleaseCounts = new Hashtable<Integer, Integer>();

    /**
     * The last keyReleaseCount that an action has been handled for.
     */
    private static Hashtable<Integer, Integer> keyActionValues = new Hashtable<Integer, Integer>();

    /**
     * The current state of watched keys.
     */
    static Hashtable<Integer, KeyState> keyStates = new Hashtable<Integer, KeyState>();

    /**
     * The current tick duration of the current key state of watched keys. Will max out at MAX_KEY_TICK_DURATION + 1.
     */
    static Hashtable<Integer, Integer> keyStateDurations = new Hashtable<Integer, Integer>();


    /**
     * The available hop directions.
     */
    enum HopDirection {
        BACK,
        LEFT,
        RIGHT
    }

    /**
     * The available key states.
     */
    enum KeyState {
        DOWN,
        UP
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        lastKey = event.getKey();
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public static void onClientTickEvent(TickEvent.ClientTickEvent event) {

        //Ignore tick if it does not apply
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world == null || minecraft.isGamePaused()) return;
        if (null == minecraft.player) return;
        if (minecraft.currentScreen != null) return;

        //Watch keys for hopping
        final GameSettings gameSettings = Minecraft.getInstance().gameSettings;
        watchKey(gameSettings.keyBindBack);
        watchKey(gameSettings.keyBindLeft);
        watchKey(gameSettings.keyBindRight);

    }

    /**
     * Triggered when a watch keybinding gets pressed twice in quick succession.
     *
     * @param keyBinding The keybinding that has been double tapped.
     */
    public static void onDoubleTap(KeyBinding keyBinding) {
        final Integer keyCode = keyBinding.getKey().getKeyCode();
        final Integer BACK_KEY_CODE = Minecraft.getInstance().gameSettings.keyBindBack.getKey().getKeyCode();
        final Integer LEFT_KEY_CODE = Minecraft.getInstance().gameSettings.keyBindLeft.getKey().getKeyCode();
        final Integer RIGHT_KEY_CODE = Minecraft.getInstance().gameSettings.keyBindRight.getKey().getKeyCode();

        if (keyCode == BACK_KEY_CODE) {
            hop(HopDirection.BACK, Minecraft.getInstance().player);
        } else if (keyCode == LEFT_KEY_CODE) {
            hop(HopDirection.LEFT, Minecraft.getInstance().player);
        } else if (keyCode == RIGHT_KEY_CODE) {
            hop(HopDirection.RIGHT, Minecraft.getInstance().player);
        }

    }

    /**
     * Watches the given keybinding for additional keypress events such as double tap.
     *
     * @param keyBinding The keybinding to watch.
     */
    public static void watchKey(KeyBinding keyBinding) {

        //Initialize
        Integer keyCode = keyBinding.getKey().getKeyCode();
        final KeyState initialState = keyStates.get(keyCode);
        KeyState state = null != keyStates.get(keyCode) ? keyStates.get(keyCode) : KeyState.UP;
        Integer keyStateDuration = null != keyStateDurations.get(keyCode) ? keyStateDurations.get(keyCode) : 0;
        Integer keyReleaseCount = null != keyReleaseCounts.get(keyCode) ? keyReleaseCounts.get(keyCode) : 0;
        Integer keyActionValue = null != keyActionValues.get(keyCode) ? keyActionValues.get(keyCode) : 0;

        //Manage key states
        if (keyBinding.isKeyDown()) {
            if (state == KeyState.UP) {
                if (keyStateDuration >= MAX_KEY_TICK_DURATION || keyBinding.getKey().getKeyCode() != lastKey) {
                    keyReleaseCount = 1;
                    keyActionValue = 0;
                    PracticalCombat.LOGGER.info(String.format("Resetting keyReleaseCount: %s", "Key UP tick duration"));
                } else {
                    keyReleaseCount++;
                }
                keyStateDuration = 0;
            }
            state = KeyState.DOWN;
        } else {
            if (state == KeyState.DOWN) {
                if (keyStateDuration >= MAX_KEY_TICK_DURATION || keyBinding.getKey().getKeyCode() != lastKey) {
                    keyReleaseCount = 0;
                    keyActionValue = 0;
                    PracticalCombat.LOGGER.info(String.format("Resetting keyReleaseCount: %s", "Key DOWN tick duration"));
                } else {
                    keyReleaseCount++;
                }
                keyStateDuration = 0;
            }
            state = KeyState.UP;
        }

        //If an action can be performed
        if (keyBinding.getKey().getKeyCode() == lastKey && keyReleaseCount > keyActionValue) {

            Integer actionModifier = HOP_ON_KEY_DOWN ? 1 : 0;
            //Handle actions here
            if ((keyReleaseCount + actionModifier) % 4 == 0) {
                onDoubleTap(keyBinding);
            }

            //Update the action value
            keyActionValue = keyReleaseCount;

        }

        //Save states
        keyStates.put(keyCode, state);
        if (state != initialState) {

            //Max out other key timers
            Iterator i = keyStateDurations.entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry pair = (Map.Entry) i.next();
                if (keyCode != pair.getKey()) {
                    keyStateDurations.put((Integer) pair.getKey(), MAX_KEY_TICK_DURATION + 1);
                }
            }

            keyStateDurations.put(keyCode, keyStateDuration);
            keyReleaseCounts.put(keyCode, keyReleaseCount);
            keyActionValues.put(keyCode, keyActionValue);
            PracticalCombat.LOGGER.info(String.format("Key State (%o): %s %o", keyCode, state, keyReleaseCount));
        } else {
            Integer tempDuration = Math.min(keyStateDuration + 1, MAX_KEY_TICK_DURATION + 1);
            keyStateDurations.put(keyCode, tempDuration);
        }

    }

    /**
     * Causes the player to hop if the conditions are met.
     *
     * @param direction The direction the player is going to hop.
     * @param player    The player that is hopping
     */
    public static void hop(HopDirection direction, ClientPlayerEntity player) {

        //The player isn't one
        if (!(player instanceof PlayerEntity)) {
            PracticalCombat.LOGGER.info("Player is not a player. How could you lie to me?");
            return;
        }

        //If the player is sneaking
        if (player.isSneaking()) {
            PracticalCombat.LOGGER.info("Player is sneaking.");
            return;
        }

        //If the player is too hungry
        if (player.getFoodStats().getFoodLevel() < MIN_HOP_FOOD_LEVEL) {
            PracticalCombat.LOGGER.info("Player is hungry.");
            return;
        }

        //If the player is not on the ground
        if (!player.isOnGround()) {
            PracticalCombat.LOGGER.info("Player is airborne.");
            return;
        }

        //If the player is riding something
        if (null != player.getRidingEntity()) {
            PracticalCombat.LOGGER.info("Player is riding something weird looking.");
            return;
        }

        //Calculate the velocity
        ModifiableAttributeInstance moveSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        final Double horizontalSpeed = 6.0d * moveSpeed.getValue();
        final Double verticalSpeed = Math.min(2.0d * moveSpeed.getValue(), 2.0d / 10d);
        Vector3d velocity = new Vector3d(0.0d, verticalSpeed, horizontalSpeed);
        Vector3d hopVelocity = new Vector3d(0.0d, 0.0d, 0.0d);
        if (direction == HopDirection.BACK) {
            hopVelocity = velocity.rotateYaw((float) -Math.toRadians(player.rotationYaw + 180));
        } else if (direction == HopDirection.LEFT) {
            hopVelocity = velocity.rotateYaw((float) -Math.toRadians(player.rotationYaw + 270));
        } else if (direction == HopDirection.RIGHT) {
            hopVelocity = velocity.rotateYaw((float) -Math.toRadians(player.rotationYaw + 90));
        }

        //Hop
        PracticalCombat.LOGGER.info(String.format("Hop %s: Speed(%f)", direction, horizontalSpeed));
        player.setVelocity(hopVelocity.x, hopVelocity.y, hopVelocity.z);
        if (player.isSprinting()) {
            player.addExhaustion(0.25f);
        } else {
            player.addExhaustion(0.1f);
        }

    }
}

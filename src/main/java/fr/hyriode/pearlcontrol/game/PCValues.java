package fr.hyriode.pearlcontrol.game;

import fr.hyriode.hyrame.game.util.value.HostValueModifier;
import fr.hyriode.hyrame.game.util.value.ValueProvider;

/**
 * Created by AstFaster
 * on 16/08/2022 at 11:16
 */
public class PCValues {

    public static final ValueProvider<Integer> SPAWN_ENDER_PEARLS = new ValueProvider<>(16).addModifiers(new HostValueModifier<>(1, Integer.class, "spawn-ender-pearls"));
    public static final ValueProvider<Integer> KILL_ENDER_PEARLS = new ValueProvider<>(4).addModifiers(new HostValueModifier<>(1, Integer.class, "kill-ender-pearls"));
    public static final ValueProvider<Boolean> ALLOWING_CAPTURE = new ValueProvider<>(true).addModifiers(new HostValueModifier<>(1, Boolean.class, "allowing-capture"));
    public static final ValueProvider<Integer> CAPTURE_TIME = new ValueProvider<>(10).addModifiers(new HostValueModifier<>(1, Integer.class, "capture-time"));
    public static final ValueProvider<Integer> KNOCKBACK_MULTIPLIER = new ValueProvider<>(1).addModifiers(new HostValueModifier<>(1, Integer.class, "knockback-multiplier"));
    public static final ValueProvider<Integer> LIVES = new ValueProvider<>(3).addModifiers(new HostValueModifier<>(1, Integer.class, "lives"));
    public static final ValueProvider<Long> RESPAWN_TIME = new ValueProvider<>(3L).addModifiers(new HostValueModifier<>(1, Long.class, "respawn-time"));

}

package fr.hyriode.pearlcontrol.util;

import fr.hyriode.pearlcontrol.HyriPearlControl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import static java.lang.Math.PI;

/**
 * Created by AstFaster
 * on 18/04/2023 at 11:17
 */
public class ParticleUtil {

    public static void animHelicoid(final Location ent, double rayon, final ParticleEffect particle, final int number, double h) {
        for (int i = 0; i < 41; i++) {
            double t = 0.3141592653589793D * i;
            final double x = rayon * Math.cos(t);
            final double m = h * t;
            final double z = rayon * Math.sin(t);
            final double x1 = rayon * Math.cos(t + PI);
            final double z1 = rayon * Math.sin(t + PI);
            Bukkit.getScheduler().runTaskLater(HyriPearlControl.get(), () -> {
                Location locfinale = ent.clone().add(x, m, z);
                Location locfinale2 = ent.clone().add(x1, m, z1);

                new ParticleBuilder(particle, locfinale)
                        .setOffset(0.07f, 0.0f, 0.07f)
                        .setSpeed(0.0f)
                        .setAmount(number)
                        .display();

                new ParticleBuilder(particle, locfinale2)
                        .setOffset(0.07f, 0.0f, 0.07f)
                        .setSpeed(0.0f)
                        .setAmount(number)
                        .display();
            }, i);
        }
    }

}

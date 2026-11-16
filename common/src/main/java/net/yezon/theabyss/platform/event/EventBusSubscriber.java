package net.yezon.theabyss.platform.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.yezon.theabyss.platform.Dist;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {
    String modid() default "";

    Dist[] value() default {Dist.CLIENT, Dist.DEDICATED_SERVER};
}

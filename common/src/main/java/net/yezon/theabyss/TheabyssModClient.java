package net.yezon.theabyss;

import net.yezon.theabyss.client.TheabyssClientSubscribers;
import net.yezon.theabyss.platform.event.AbyssEventBus;

public final class TheabyssModClient {
    private TheabyssModClient() {
    }

    public static void init() {
        for (Class<?> subscriber : TheabyssClientSubscribers.ALL) {
            AbyssEventBus.register(subscriber);
        }
    }
}

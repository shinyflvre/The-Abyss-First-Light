package net.yezon.theabyss.gravity;

public interface GravityEntity {
    int theabyss$gravityId();

    void theabyss$setGravityId(int id);

    boolean theabyss$framed();

    void theabyss$setFramed(boolean framed);

    int theabyss$blocked();

    int theabyss$lastTravel();

    void theabyss$setLastTravel(int tick);

    void theabyss$setSteer(double x, double y, double z, boolean on);
}

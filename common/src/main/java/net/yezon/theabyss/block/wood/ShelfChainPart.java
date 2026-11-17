package net.yezon.theabyss.block.wood;

import net.minecraft.util.StringRepresentable;

public enum ShelfChainPart implements StringRepresentable {
    UNCONNECTED("unconnected"),
    RIGHT("right"),
    CENTER("center"),
    LEFT("left");

    private final String name;

    ShelfChainPart(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean isConnected() {
        return this != UNCONNECTED;
    }

    public boolean isConnectionTowards(ShelfChainPart endPart) {
        return this == CENTER || this == endPart;
    }

    public boolean isChainEnd() {
        return this != CENTER;
    }

    public ShelfChainPart whenConnectedToTheRight() {
        return switch (this) {
            case UNCONNECTED, LEFT -> LEFT;
            case RIGHT, CENTER -> CENTER;
        };
    }

    public ShelfChainPart whenConnectedToTheLeft() {
        return switch (this) {
            case UNCONNECTED, RIGHT -> RIGHT;
            case CENTER, LEFT -> CENTER;
        };
    }

    public ShelfChainPart whenDisconnectedFromTheRight() {
        return switch (this) {
            case UNCONNECTED, LEFT -> UNCONNECTED;
            case RIGHT, CENTER -> RIGHT;
        };
    }

    public ShelfChainPart whenDisconnectedFromTheLeft() {
        return switch (this) {
            case UNCONNECTED, RIGHT -> UNCONNECTED;
            case CENTER, LEFT -> LEFT;
        };
    }
}

package net.yezon.theabyss.compat;

public enum TriState {
    TRUE,
    DEFAULT,
    FALSE;

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }

    public boolean toBoolean(boolean fallback) {
        return this == DEFAULT ? fallback : this == TRUE;
    }

    public static TriState of(boolean value) {
        return value ? TRUE : FALSE;
    }
}

package net.yezon.theabyss.platform.event;

public abstract class Event {
    private boolean canceled;

    public boolean isCanceled() {
        return this.canceled;
    }

    public void setCanceled(boolean canceled) {
        if (!(this instanceof ICancellableEvent)) {
            throw new UnsupportedOperationException("Event " + this.getClass().getName() + " cannot be canceled");
        }
        this.canceled = canceled;
    }
}

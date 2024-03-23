package fr.vinetos.tranquille.event;

public class SecondaryDbUpdateFinished {

    public final boolean updated;

    public SecondaryDbUpdateFinished(boolean updated) {
        this.updated = updated;
    }

}

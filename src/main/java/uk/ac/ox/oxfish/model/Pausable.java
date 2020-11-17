package uk.ac.ox.oxfish.model;

/**
 * very simple interface that certifies an object has a "pause" switch.
 * Doesn't assume anything about what it does, all it certifies is that you can flip it on or off
 */
public interface Pausable {



    public boolean isPaused() ;

    public void setPaused(boolean paused);
}

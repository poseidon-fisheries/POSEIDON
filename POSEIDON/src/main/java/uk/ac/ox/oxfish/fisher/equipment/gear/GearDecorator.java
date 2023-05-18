package uk.ac.ox.oxfish.fisher.equipment.gear;

/**
 * marker to define gear decorators in a way that's easier to walk through
 */
public interface GearDecorator extends Gear {


    public Gear getDelegate();

    public void setDelegate(Gear delegate);


}

package uk.ac.ox.oxfish.fisher.equipment.gear;

public class DecoratorGearPair {


    /**
     * the bottom decorator; can be null if gear is never decorated
     */
    private final GearDecorator deepestDecorator;

    /**
     * decorated gear, cannot be null
     */
    private final Gear decorated;

    public DecoratorGearPair(
        final GearDecorator deepestDecorator,
        final Gear decorated
    ) {
        this.deepestDecorator = deepestDecorator;
        this.decorated = decorated;
    }

    public static DecoratorGearPair getActualGear(final Gear gear) {
        if (gear instanceof GearDecorator) {
            //go deeper
            if (((GearDecorator) gear).getDelegate() instanceof GearDecorator)
                return getActualGear(((GearDecorator) gear).getDelegate());
            else
                return new DecoratorGearPair(
                    (GearDecorator) gear,
                    ((GearDecorator) gear).getDelegate()
                );
        } else
            return new DecoratorGearPair(null, gear);
    }

    public GearDecorator getDeepestDecorator() {
        return deepestDecorator;
    }

    public Gear getDecorated() {
        return decorated;
    }
}

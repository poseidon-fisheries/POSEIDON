package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DecoratorGearPair {


    @Nullable
    /**
     * the bottom decorator; can be null if gear is never decorated
     */
    private final GearDecorator deepestDecorator;

    /**
     * decorated gear, cannot be null
     */
    @NotNull
    private final Gear decorated;

    public DecoratorGearPair(@Nullable GearDecorator deepestDecorator,
                             @NotNull Gear decorated) {
        this.deepestDecorator = deepestDecorator;
        this.decorated = decorated;
    }

    @Nullable
    public GearDecorator getDeepestDecorator() {
        return deepestDecorator;
    }

    @NotNull
    public Gear getDecorated() {
        return decorated;
    }


    public static DecoratorGearPair getActualGear(Gear gear)
    {
        if(gear instanceof GearDecorator)
        {
            //go deeper
            if(((GearDecorator) gear).getDelegate() instanceof GearDecorator)
                return getActualGear(((GearDecorator) gear).getDelegate());
            else
                return new DecoratorGearPair((GearDecorator) gear,
                        ((GearDecorator) gear).getDelegate());
        }
        else
            return new DecoratorGearPair(null,gear);
    }
}

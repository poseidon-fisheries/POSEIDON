package uk.ac.ox.oxfish.fisher.equipment.gear;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class DecoratorGearPairTest {


    @Test
    public void single() {

        FixedProportionGear single = new FixedProportionGear(.1);
        DecoratorGearPair returned = DecoratorGearPair.getActualGear(single);
        assertEquals(returned.getDeepestDecorator(),null);
        assertEquals(returned.getDecorated(),single);

    }

    @Test
    public void oneDown() {

        FixedProportionGear delegate = new FixedProportionGear(.1);
        GarbageGearDecorator decorator = new GarbageGearDecorator(mock(Species.class),
                .1,
                delegate,
                true);
        DecoratorGearPair returned = DecoratorGearPair.getActualGear(decorator);
        assertEquals(returned.getDeepestDecorator(),decorator);
        assertEquals(returned.getDecorated(),delegate);

    }

    @Test
    public void twoDown() {

        FixedProportionGear delegate = new FixedProportionGear(.1);
        GarbageGearDecorator decorator = new GarbageGearDecorator(mock(Species.class),
                .1,
                delegate,
                true);
        GarbageGearDecorator decorator2 = new GarbageGearDecorator(mock(Species.class),
                .1,
                decorator,
                true);
        DecoratorGearPair returned = DecoratorGearPair.getActualGear(decorator2);
        assertEquals(returned.getDeepestDecorator(),decorator);
        assertEquals(returned.getDecorated(),delegate);

        //and I can change the tree leaf without affecting the tree structure
        Gear mock = mock(Gear.class);
        returned.getDeepestDecorator().setDelegate(mock);
        returned = DecoratorGearPair.getActualGear(decorator2);
        assertEquals(returned.getDeepestDecorator(),decorator);
        assertEquals(returned.getDecorated(),mock);

    }
}
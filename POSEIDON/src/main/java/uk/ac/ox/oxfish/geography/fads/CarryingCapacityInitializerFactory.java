package uk.ac.ox.oxfish.geography.fads;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.CarryingCapacity;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.CarryingCapacityInitializer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public interface CarryingCapacityInitializerFactory<T extends CarryingCapacity>
    extends AlgorithmFactory<CarryingCapacityInitializer<T>> {
}

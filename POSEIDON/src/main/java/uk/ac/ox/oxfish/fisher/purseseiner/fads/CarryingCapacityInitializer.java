package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;

import java.util.function.Function;

public interface CarryingCapacityInitializer<T extends CarryingCapacity>
    extends Function<MersenneTwisterFast, T> {
}

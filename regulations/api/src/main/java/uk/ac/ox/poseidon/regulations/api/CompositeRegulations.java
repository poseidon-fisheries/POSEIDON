package uk.ac.ox.poseidon.regulations.api;

import java.util.Collection;

public interface CompositeRegulations<C> extends Regulations<C> {
    Collection<Regulations<C>> getSubRegulations();
}

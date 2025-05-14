package uk.ac.ox.poseidon.agents.market;

import com.google.common.collect.Table;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.time.LocalDateTime;

public class BiomassSale extends Sale<Biomass> {
    BiomassSale(
        final LocalDateTime dateTime,
        final String id,
        final Market<Biomass> market,
        final Vessel vessel,
        final Table<Species, Biomass, Money> sold,
        final Bucket<Biomass> unsold
    ) {
        super(dateTime, id, market, vessel, sold, unsold);
    }
}

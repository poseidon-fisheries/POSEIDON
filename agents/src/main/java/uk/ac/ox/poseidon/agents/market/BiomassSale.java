package uk.ac.ox.poseidon.agents.market;

import com.google.common.collect.Table;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.biomass.Biomass;
import uk.ac.ox.poseidon.biology.species.Species;

import java.time.LocalDateTime;

public class BiomassSale extends Sale<Biomass> {
    public BiomassSale(
        LocalDateTime dateTime,
        String id,
        Market<Biomass> market,
        Vessel vessel,
        Table<Species, Biomass, Money> sold,
        Bucket<Biomass> unsold
    ) {
        super(dateTime, id, market, vessel, sold, unsold);
    }
}

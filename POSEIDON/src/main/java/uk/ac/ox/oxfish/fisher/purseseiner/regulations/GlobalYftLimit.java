package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.oxfish.regulations.ForbiddenIf;
import uk.ac.ox.oxfish.regulations.conditions.Above;
import uk.ac.ox.oxfish.regulations.conditions.AllOf;
import uk.ac.ox.oxfish.regulations.quantities.YearlyGatherer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.List;

public class GlobalYftLimit implements RegulationFactory, YearsActive {

    private List<Integer> yearsActive;
    private double limit; //tonnes

    @SuppressWarnings("unused")
    public GlobalYftLimit() {
    }

    //@SuppressWarnings("WeakerAccess")
    public GlobalYftLimit(
        final List<Integer> yearsActive,
        final double limit_tonnes
    ) {
        this.yearsActive = yearsActive;
        this.limit=limit_tonnes;
    }

    @SuppressWarnings("unused")
    public List<Integer> getYearsActive() {
        return yearsActive;
    }

    @SuppressWarnings("unused")
    public void setYearsActive(final List<Integer> yearsActive) {
        this.yearsActive = yearsActive;
    }

    public double getLimit(){return limit;}
    public void setLimit(double limit_tonnes){this.limit = limit_tonnes;}

    @Override
    public AlgorithmFactory<Regulations> get() {
        return new ForbiddenIf(
            new AllOf(
                new Above(
                    new YearlyGatherer("Sum of Yellowfin tuna catches"),
                    limit * 1000 // convert from tonnes to kg
                )
            )
        );
    }
}

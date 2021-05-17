package uk.ac.ox.oxfish.model.regs;

import com.beust.jcommander.internal.Lists;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.ArrayList;
import java.util.List;

public class ExogenousPercentagePermitFactory implements AlgorithmFactory<ExogenousPercentagePermitAllocation> {

    private List<Double> yearlyEffortAllowed = Lists.newArrayList(.1,.2,.3,.4,.5);


    @Override
    public ExogenousPercentagePermitAllocation apply(FishState fishState) {
        return new ExogenousPercentagePermitAllocation(new ArrayList<>(yearlyEffortAllowed));
    }

    public List<Double> getYearlyEffortAllowed() {
        return yearlyEffortAllowed;
    }

    public void setYearlyEffortAllowed(List<Double> yearlyEffortAllowed) {
        this.yearlyEffortAllowed = yearlyEffortAllowed;
    }
}

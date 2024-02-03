package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class YearlyFisherValue implements AlgorithmFactory<Quantity> {

    private StringParameter name;
    private IntegerParameter entriesFromLast;

    @SuppressWarnings("unused")
    public YearlyFisherValue() {
    }

    public YearlyFisherValue(
        final StringParameter name,
        final IntegerParameter entriesFromLast
    ) {
        this.name = name;
        this.entriesFromLast = entriesFromLast;
    }

    public IntegerParameter getEntriesFromLast() {
        return entriesFromLast;
    }

    public void setEntriesFromLast(final IntegerParameter entriesFromLast) {
        this.entriesFromLast = entriesFromLast;
    }

    public StringParameter getName() {
        return name;
    }

    public void setName(final StringParameter name) {
        this.name = name;
    }

    @Override
    public Quantity apply(final FishState fishState) {
        return makeQuantity(name.getValue(), entriesFromLast.getValue());
    }

    static Quantity makeQuantity(
        final String name,
        final int entriesFromLast
    ) {
        return action -> {
            final TimeSeries<Fisher> yearlyData = ((Fisher) action.getAgent()).getYearlyData();
            final DataColumn column = yearlyData.getColumn(name);
            if (column == null) {
                throw new RuntimeException("Time series not found: " + name);
            }
            return column.getDatumXStepsAgo(entriesFromLast);
        };
    }
}

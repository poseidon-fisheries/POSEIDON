package uk.ac.ox.oxfish.model.data.collectors;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.FishingRecord;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.scenario.FisherFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * listens to all trips, records effort in each and then produces the Herfindal index of concentration.
 */
public class HerfindalIndexCollector implements AdditionalStartable, TripListener {


    private static final long serialVersionUID = -5628924718776795651L;
    private Table<Integer, Integer, Double> effort;

    private Table<Integer, Integer, Double> catches;


    @Override
    public void start(final FishState model) {
        for (final Fisher fisher : model.getFishers()) {
            fisher.addTripListener(this);
        }

        for (final Map.Entry<String, FisherFactory> fisherFactory : model.getFisherFactories()) {
            fisherFactory.getValue().getAdditionalSetups().add(
                fisher -> fisher.addTripListener(HerfindalIndexCollector.this)
            );
        }

        effort = HashBasedTable.create(
            model.getMap().getWidth(),
            model.getMap().getHeight()
        );
        catches = HashBasedTable.create(
            model.getMap().getWidth(),
            model.getMap().getHeight()
        );

        //register the gatherer (which in our case will also reset the table)
        model.getYearlyDataSet().registerGatherer("Effort Herfindal",
            (Gatherer<FishState>) fishState -> {
                final double hhi = computeHerfindalIndex(HerfindalIndexCollector.this.effort);

                effort = HashBasedTable.create(
                    model.getMap().getWidth(),
                    model.getMap().getHeight()
                );

                return hhi;

            }, 1d
        );

        model.getYearlyDataSet().registerGatherer("Catch Herfindal",
            (Gatherer<FishState>) fishState -> {
                final double hhi = computeHerfindalIndex(HerfindalIndexCollector.this.catches);

                catches = HashBasedTable.create(
                    model.getMap().getWidth(),
                    model.getMap().getHeight()
                );

                return hhi;

            }, 1d
        );
    }

    private double computeHerfindalIndex(final Table<Integer, Integer, Double> table) {
        double sumOfEffort = 0;
        for (final Double effort : table.values()) {
            sumOfEffort += effort;
        }
        double hhi = 0d;
        for (final Double effort : table.values()) {
            hhi += Math.pow(effort / sumOfEffort, 2);
        }
        return hhi;
    }

    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {


        final Map<Table<Integer, Integer, Double>,
            Function<FishingRecord, Double>> tablesToUpdate =
            new HashMap<>();
        tablesToUpdate.put(
            effort,
            fishingRecord -> Double.valueOf(fishingRecord.getHoursSpentFishing())
        );
        tablesToUpdate.put(
            catches,
            fishingRecord -> Double.valueOf(fishingRecord.getFishCaught()
                .getTotalWeight())
        );

        for (final Map.Entry<Table<Integer, Integer, Double>, Function<FishingRecord, Double>> table : tablesToUpdate.entrySet()) {


            for (final Map.Entry<SeaTile, FishingRecord> fishingEntry : record.getFishingRecords()) {

                Double previousEffort =
                    table.getKey().get(
                        fishingEntry.getKey().getGridX(),
                        fishingEntry.getKey().getGridY()
                    );
                if (previousEffort == null)
                    previousEffort = 0d;

                table.getKey().put(
                    fishingEntry.getKey().getGridX(),
                    fishingEntry.getKey().getGridY(),
                    previousEffort + table.getValue().apply(fishingEntry.getValue())
                );

            }
        }


    }
}

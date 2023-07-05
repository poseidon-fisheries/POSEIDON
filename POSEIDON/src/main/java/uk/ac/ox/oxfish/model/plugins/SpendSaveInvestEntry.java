package uk.ac.ox.oxfish.model.plugins;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * all populations sharing a tag will spend a fixed amount of money and if their
 * cash balance allow it they will build a new boat.
 */
public class SpendSaveInvestEntry implements EntryPlugin {

    private static final long serialVersionUID = 119775768311783218L;
    private final double moneyNeededForANewEntry;

    private final double yearlyExpenses;


    /**
     * which population of boats are we growing; this has to be both the name of the fishery factory and a tag so that we know
     * which boats belong to it
     */
    private final String populationName;

    public boolean paused = false;
    private Stoppable stoppable;
    private boolean newEntryAllowed = true;


    public SpendSaveInvestEntry(
        final double moneyNeededForANewEntry,
        final double yearlyExpenses,
        final String populationName
    ) {
        this.moneyNeededForANewEntry = moneyNeededForANewEntry;
        this.yearlyExpenses = yearlyExpenses;
        this.populationName = populationName;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        Preconditions.checkArgument(stoppable == null, "already started!");
        stoppable = model.scheduleEveryYear(
            this,
            StepOrder.AFTER_DATA
        );
        if (!model.getEntryPlugins().contains(this))
            model.getEntryPlugins().add(this);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if (stoppable != null)
            stoppable.stop();

    }

    @Override
    public void step(final SimState simState) {
        if (isPaused())
            return;
        final FishState model = ((FishState) simState);


        int boatsToAdd = 0;
        // count the fisher as active if it has been on at least a trip in the past 365 days!
        for (final Fisher fisher : model.getFishers()) {
            if (fisher.getTagsList().contains(populationName) && fisher.hasBeenActiveThisYear()) {
                //spend your daily trip
                fisher.spendExogenously(yearlyExpenses);
                //if you have collected enough money, invest it in a new boat
                if (newEntryAllowed && fisher.getBankBalance() > moneyNeededForANewEntry) {
                    fisher.spendExogenously(moneyNeededForANewEntry);
                    assert fisher.getBankBalance() > 0;
                    boatsToAdd++;
                }
            }


        }

        for (int i = 0; i < boatsToAdd; i++)
            model.createFisher(populationName);

    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(final boolean paused) {
        this.paused = paused;
    }

    public double getMoneyNeededForANewEntry() {
        return moneyNeededForANewEntry;
    }

    public double getYearlyExpenses() {
        return yearlyExpenses;
    }


    public boolean isNewEntryAllowed() {
        return newEntryAllowed;
    }

    public void setNewEntryAllowed(final boolean newEntryAllowed) {
        this.newEntryAllowed = newEntryAllowed;
    }

    @Override
    public boolean isEntryPaused() {
        return paused;
    }

    @Override
    public void setEntryPaused(final boolean entryPaused) {
        paused = entryPaused;
    }
}

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.RecruitmentBySpawningJackKnifeMaturity;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * given a bunch of box car objects simulates a virgin population
 */
public class BoxCarSimulator {

    final private double initialRecruits;

    final private FixedBoxcarAging dailyStep;

    final private YearlyRecruitmentProcess recruitmentProcess;

    final private GrowthBinByList meristics;

    final private NaturalMortalityProcess mortalityProcess;


    public BoxCarSimulator(
            double initialRecruits,
            FixedBoxcarAging dailyStep,
            YearlyRecruitmentProcess recruitmentProcess,
            GrowthBinByList meristics, NaturalMortalityProcess mortalityProcess) {
        this.initialRecruits = initialRecruits;
        this.dailyStep = dailyStep;
        this.recruitmentProcess = recruitmentProcess;
        this.meristics = meristics;
        this.mortalityProcess = mortalityProcess;
    }

    public StructuredAbundance virginCondition(FishState state,
                                               int yearsToVirgin){
        Species species = new Species("simulated",meristics);
        dailyStep.start(species);

        //create a single local biology hosting everything
        AbundanceLocalBiology biology = new AbundanceLocalBiology(new GlobalBiology(species));
        biology.getAbundance(species).asMatrix()[0][0] = initialRecruits;

        //now step on it
        for(int year = 0; year< yearsToVirgin; year++) {
            for (int day = 0; day < 365; day++) {
                dailyStep.ageLocally(biology,
                        species,
                        state,
                        false,
                        1);
                double recruit = recruitmentProcess.recruit(species,
                        species.getMeristics(),
                        biology.getAbundance(species),
                        day,
                        1);
                mortalityProcess.cull(meristics,false,biology.getAbundance(species),1);
                biology.getAbundance(species).asMatrix()[0][0] += recruit;

            }


        }

        return biology.getAbundance(species);
    }

    public static void main(String[] args)
    {
        FixedBoxcarBertalannfyAging aging = new FixedBoxcarBertalannfyAging();
        EquallySpacedBertalanffyFactory meristics = new EquallySpacedBertalanffyFactory();
        meristics.setCmPerBin(5d);
        meristics.setMaxLengthInCm(new FixedDoubleParameter(125));
        meristics.setRecruitLengthInCm(new FixedDoubleParameter(0));
        RecruitmentBySpawningJackKnifeMaturity maturity = new RecruitmentBySpawningJackKnifeMaturity();
        maturity.setCumulativePhi(new FixedDoubleParameter(6));
        maturity.setSteepness(new FixedDoubleParameter(0.8));
        maturity.setVirginRecruits(new FixedDoubleParameter(10000));

        ProportionalMortalityProcess process = new ProportionalMortalityProcess(.2);

        FishState state = new FishState();

        BoxCarSimulator simulator = new BoxCarSimulator(
                10000,
                aging.apply(state),
                maturity.apply(state),
                meristics.apply(state),
                process);
        StructuredAbundance structuredAbundance = simulator.virginCondition(state, 1000);

        System.out.println(structuredAbundance);
    }

}

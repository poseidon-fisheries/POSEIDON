from __future__ import print_function
import subprocess
import os
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/spearmint"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/fronts"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/mpa"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/hyperstability"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/gas_prices"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/gearopt"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/hardswitch/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/itq_mileage/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/race/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/location_itq/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/sensitivity/gear_itq/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/inputs/first_paper/optimiser/tac-separated/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/kalman_tune/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/kalman_tune/kernel/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/rbf/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/rbf/itq_plan/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/rbf/itq_simple/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/kalman_plan/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/nn_simple/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/nn_plan/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/gwr/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/gwr_plan/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/goodbad_simple/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/goodbad_plan/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/kalman_plan/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/kalman_tune/kalman_simple/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/personal_tune/nn_simple/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/optimization/personal_tune/epa_simple/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161004 adaptive_tax/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161004 adaptive_tax/subsidy/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161012 pid_tax/daily_target/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161026 shapes/squares/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161026 shapes/careful/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161114 bandits/epsilon/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161114 bandits/softmax/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161114 bandits/ucb/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161114 bandits/alternatives/eei"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161114 bandits/alternatives/pso"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20161114 bandits/alternatives/gsa"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/runs/pretend_calibration/catchability"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170103 shodan_test/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170111 fit_example/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170213 expensive_policy/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170213 expensive_policy/control/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170322 cali_catch/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170322 cali_catch/profits"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170322 cali_catch/landings"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170322 cali_catch/dover"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170403 narrative/itq/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170403 narrative/tac/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170403 narrative/climate/itq/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170403 narrative/climate/tac/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170407 climate/climate/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170407 climate/base/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/tac_mixed/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/itq_mixed/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/itq_half/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/tac_half/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/mixed/tac-mixed/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/mixed/mpa_alone/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/mixed/mpa_simple/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/mixed/season_length_alone/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/half/kitchensink/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/half/kitchensink/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/half/mpa_alone/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/kitchensink/half/season_length_alone/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/common/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/common-scalable/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/total/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/total-limited/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/total-limited-nonscalable/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/single-limited/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/total-limited-2/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170526 gom_catchability/total-limited-shrimp/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/attainment"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/attainment_ignoring"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/attainment_ignoring_narrow"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/policymaking/season_length/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/attainment_single_profits"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/policymaking/sablefish_control/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/6/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/7/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/8/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/9/"


#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/200/6/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/200/7/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/200/8/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/200/5/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/200/9/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/200/10/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170615 ashleigh/heterogeneous/200/"


#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/itq/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/tac/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/tac-box/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/itq-box-2/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/itq-box-3/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/tac-box-2/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/tac-box-3/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170619 matt/general/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170630 fixed_gear/calibration/geographic"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/eei-inference/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/annealing-inference/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/eei-inference/imitation/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/attainment-hours/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/bandit-inference/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/kernel-inference/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/attainment-movement/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/tweaking/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/attainment-only/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/itq_only_calibration_noprofits/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/twostep/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/cpue_map/calibration/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/kernel-inference/ant/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/cpue_map/eei"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/cpue_map/bandit"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/cpue_map/kernel"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/attainment-only/ant/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/eei-inference/imitation/ant/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/deriso/calibration/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20170730 validation/attainment-north/"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20171127 selex_test/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20171127 selex_test/lutjanus/"



#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/indirect_inference/bayes/calibrator/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/indirect_inference/bayes/feb2018/calibration"


#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20180404 biomass_indonesia/calibration"

#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20180516 medium_713/catchability"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20180516 medium_713/biomass_catchability"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20180516 medium_713/medium_catchability"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/20180516 medium_713/adding_large/"


EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/paper3_dts/mark2/exploratory/calibration/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/paper3_dts/mark2/exploratory/clamped/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/paper3_dts/mark2/exploratory/perfect/"
#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/paper3_dts/mark2/exploratory/deriso/"


#EXPERIMENT_DIRECTORY = "/home/carrknight/code/oxfish/docs/paper3_dts/newlogbook/"


SPEARMINT_DIRECTORY = "/home/carrknight/code/Spearmint/spearmint"



def default_scorer(yamlfile,output_directory):
    '''
    given the yaml file containing the results, extract a number representing the score for the run!
    :param yamlfile: the results.yaml created by the simulation
    :return: the score of this simulation, the lower the better.
    '''
    return -float(yamlfile["FishState"]["Biomass Species 1"][-1])





# one dimensional function
def run_experiment(input2yaml,
                   experiment_title,
                   scorer=default_scorer,
                   jarfile="yamler.jar",
                   main_directory="/home/carrknight/code/oxfish/runs/optimization",
                   years_to_run=20,
                   additional_data=False,
                   policy_file = None):
    import os
    import subprocess
    os.chdir(main_directory)
    # each row is a run
    print("running input2yaml")
    input2yaml(main_directory + "/" + experiment_title + ".yaml")
    print("calling java!")
# feed it into the simulation and run it
    args = ["java", "-jar", jarfile, experiment_title + ".yaml", "--years", str(years_to_run)]
    if additional_data:
        args.append("--data")
    if policy_file is not None:
        args.append("--policy")
        args.append(policy_file)
    subprocess.call(args)
    # read up the results
    os.remove(experiment_title + ".yaml")
    print("reading results")
    import yaml
    results = yaml.load(open(main_directory + "/output/" + experiment_title + "/result.yaml"))
    result = scorer(results,main_directory + "/output/" + experiment_title +"/")
    print("result " + str(result))
    # append them in a list of outputs
    return result


# find the experiment name and create directory from it
def main():
    import sys
    print(sys.path)

    import json
    os.chdir(EXPERIMENT_DIRECTORY)
    experiment_name = json.load(open("config.json"))["experiment-name"]
    print("starting " + experiment_name)


    ##connect mongo to itx
  # subprocess.call(["mongod", "--fork", "--logpath", dbDirectory + "/log_"+experiment_name+".txt", "--dbpath", dbDirectory])

    ##now run spearmint
    os.chdir(SPEARMINT_DIRECTORY)
    with open(EXPERIMENT_DIRECTORY + '/console_' + experiment_name + '.txt', "w") as console:
        subprocess.call(["python2", "main.py", EXPERIMENT_DIRECTORY ], stdout = console, stderr = console )

    #now plot
    #output_to_r.plot()

if __name__ == "__main__":
    main()

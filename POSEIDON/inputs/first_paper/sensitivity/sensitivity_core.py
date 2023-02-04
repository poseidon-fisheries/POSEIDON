from __future__ import print_function

PROJECT_DIRECTORY = "/home/carrknight/code/oxfish/"

import itertools

cont = itertools.count()


def default_scorer(yamlfile):
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
                   main_directory= PROJECT_DIRECTORY +"runs/optimization",
                   years_to_run=20,
                   additional_data=False):
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
    subprocess.call(args)
    # read up the results
    os.remove(experiment_title + ".yaml")
    print("reading results")
    import yaml
    results = yaml.load(open(main_directory + "/output/" + experiment_title + "/result.yaml"))
    result = scorer(results)
    print("result " + str(result))
    # append them in a list of outputs
    return result


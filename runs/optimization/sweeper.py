from __future__ import division
import numpy as np


EXPERIMENT_TITLE = "indocluster_4"
INPUT_YAML = "/home/carrknight/code/oxfish/runs/optimization/indonesia_cluster1.yaml"
VALUE_RANGE = [float(x) for x in np.arange(0,1,0.01)] 
VALUE_ADDRESS = ["Indonesia", "weatherInitializer", "Constant Weather","temperature"]
RUNS_PER_VALUE = 1
DATA_COLUMNS = ["Average Cash-Flow"]
SIMULATION_YEARS = 5

MAIN_DIRECTORY = '.'
JAR_NAME = "yamler.jar"

def main():
    run_experiment.counter = 0
    run_experiments(EXPERIMENT_TITLE,
                    INPUT_YAML,
                    VALUE_RANGE,
                    VALUE_ADDRESS,
                    RUNS_PER_VALUE,
                    DATA_COLUMNS,
                    SIMULATION_YEARS)


def prepare_yaml(original_yaml, output_yaml, addressToChange, value):
    import yaml

    print("given " + original_yaml)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)


    #navigate to the correct item
    address = data
    addressToChange = addressToChange[:]
    final_step = addressToChange[-1]
    del addressToChange[-1]
    for step in addressToChange:
        address = address[step]
    #set the value
    address.update({final_step:value})



    #write to file:
    #   print(data)
    # the directory where it is outputted depends fundamentally on the caller, not the responsibility
    # of this class
    try:
        with open(output_yaml, 'w') as outfile:
            outfile.write(yaml.dump(data, default_flow_style=False))
        print("done with the output")
    except TypeError as err:
        print "Unexpected error:" + str(err)
        print "Unexpected error:" + str(err.args)




def report_yaml(datacolumns,yamlfile,years_to_run,current_run,current_value):
    '''
    given the yaml file containing the results, extract a string with all the results
    :param yamlfile: the results.yaml created by the simulation
    :return: the score of this simulation, the lower the better.
    '''
    results = []
    string = str()
    for year in range(years_to_run):
        results.append(current_value)
        results.append(current_run)
        results.append(year)
        for column in datacolumns:
	    results.append([float(x) for x in yamlfile["FishState"][column]][year])
	string = string + ','.join(map(str,results)) + "\n"
        results = []
    return string


counter = 0

# one run
def run_experiment(input2yaml, experiment_title, current_value, current_run, reporter=report_yaml, jarfile=JAR_NAME,
                   main_directory=".", years_to_run=SIMULATION_YEARS, additional_data=False, policy_file=None):
    import os
    import subprocess
    os.chdir(main_directory)
    # each row is a run
    print("running input2yaml")
    experiment_title = experiment_title + "-" + str(run_experiment.counter)
    run_experiment.counter +=1
    temp_yaml = main_directory + "/" + experiment_title
    input2yaml(temp_yaml + ".yaml")
    print("calling java!")
    # feed it into the simulation and run it
    args = ["java", "-jar", jarfile,experiment_title+ ".yaml", "--years", str(years_to_run)]
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
    #read the results
    results = yaml.load(open(main_directory + "/output/" + experiment_title + "/result.yaml"))
    #turn it into a fun line of csv
    toReturn = reporter(results)
    print(toReturn)
    # append them in a list of outputs
    return toReturn


def run_experiments(
        experiment_title,
        input_yaml,
        value_range,
        address,
        runs_per_value,
        data_columns,
        simulation_years):
    csvfile = open(str(experiment_title)+".csv","w")
    csvfile.write("value,run,year," + ",".join(data_columns))
    csvfile.write('\n')
    csvfile.flush()
    # for every possible value
    for value in value_range:
        # do it as many runs as you need
        for run in range(runs_per_value):
            preparer = lambda x: prepare_yaml(input_yaml, x, address, value)
            reporter = lambda x: report_yaml(data_columns,x,simulation_years,run,value)
            result = run_experiment(preparer, experiment_title, value, run, reporter, jarfile=JAR_NAME,
                                    main_directory=MAIN_DIRECTORY, years_to_run=simulation_years, additional_data=False,
                                    policy_file=None)
            csvfile.write(result)
            #csvfile.write('\n')
            csvfile.flush()




if __name__ == '__main__':
    # execute only if run as the entry point into the program
    main()

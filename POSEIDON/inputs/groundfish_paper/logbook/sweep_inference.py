# this file runs the basic yaml 100 times to generate realistic confidence intervals to test against!
import os
import sys
import shutil
from shutil import copyfile

import subprocess


# directory where yamler.jar is to run the simulations
main_directory="/home/carrknight/code/oxfish/runs/optimization"

# sweep directory
sweep_directory = "/home/carrknight/code/oxfish/docs/groundfish/yesgarbage/logbook/"

def run_experiment(experiment_title,
                   jarfile="yamler.jar",
                   years_to_run=5,
                   main_directory="/home/carrknight/code/oxfish/runs/optimization"):

    import yaml
    os.chdir(main_directory);
    # each row is a run
    print("running input2yaml");
    # feed it into the simulation and run it
    args = ["java", "-jar", jarfile, experiment_title + ".yaml", "--years", str(years_to_run)]
    subprocess.call(args)
    results_file = yaml.load(open(main_directory + "/output/" + experiment_title + "/result.yaml"))
    results = scorer(results_file,main_directory + "/output/" + experiment_title +"/")
    # read up the results
    os.remove(experiment_title + ".yaml")
    shutil.rmtree(main_directory + "/output/" + experiment_title +"/")
    return results


def scorer(yamlfile,output_directory):

    #fit logit to it
    # throug R
    args = ["Rscript", "/home/carrknight/code/oxfish/docs/20170730 validation/rerun/logbook/grinder-habit-inter.R",
            output_directory + "/logistic_long.csv",
            output_directory +  "mlogitFit.csv"]
    subprocess.call(args)


    try:
        with open(output_directory +  "mlogitFit.csv", 'r') as mlogit:

            mlogit.next() # skip header

            distanceLine = mlogit.next()
            print(distanceLine)
            distance = float(distanceLine.split(",")[1])
            # skip next, because it's loglikelihood
            mlogit.next()
            # this is habit
            habitLine = mlogit.next()
            print(habitLine)
            habit = float(habitLine.split(",")[1])
    #numbers from temporary summary
        return {"habit": habit, "distance":distance}
    except IOError:
         return {"habit": float('nan'), "distance":float('nan') }
    #numbers from temporary summary

import csv


#random is missing (because all the fishermen quit)
filenames = [
                #    "default.yaml", "clamped.yaml", "eei.yaml",
                #"fleetwide.yaml","nofleetwide.yaml",
                #"perfect.yaml", 
                #"random.yaml", "bandit.yaml",
                #"annealing.yaml", 
                #"intercepts.yaml", #"kernel.yaml",
                "perfect_cell.yaml"#,
                #"nofleetwide_identity.yaml"
]
for original_file in filenames:
    results = []
    with open(sweep_directory + "/" + original_file + ".csv", "wb") as f:
        f.write("habit,distance\n")
    for job_id in range(100):
        experiment_name = original_file + "_"+str(job_id)
        print(experiment_name)

        copyfile(sweep_directory + "/" + original_file ,
                 main_directory + "/" + experiment_name + ".yaml")


        result = run_experiment(experiment_name,
                                years_to_run=5)
        print result
        results.append([result["habit"],result["distance"]])
        with open(sweep_directory + "/" + original_file + ".csv", "a") as f:
            f.write(str(result["habit"]) + "," + str(result["distance"]) +"\n")
    print results


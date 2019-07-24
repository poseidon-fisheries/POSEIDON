# this file runs the basic yaml 100 times to generate realistic confidence intervals to test against!
from __future__ import with_statement
import os
import sys
import shutil
from shutil import copyfile

import subprocess



# directory where yamler.jar is to run the simulations
main_directory="/home/carrknight/code/oxfish/runs/optimization"

# sweep directory
sweep_directory = "/home/carrknight/code/oxfish/docs/groundfish/yesgarbage/identification/"

def run_experiment(experiment_title,
                   jarfile="yamler.jar",
                   years_to_run=5,
                   main_directory="/home/carrknight/code/oxfish/runs/optimization"):

    import yaml
    os.chdir(main_directory);
    # each row is a run
    print("running input2yaml");
    print(experiment_title)
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
    args = ["Rscript", "/home/carrknight/code/oxfish/docs/groundfish/yesgarbage/identification/grinder-habit-simple.R",
            output_directory + "simpleRUM.csv",
            output_directory +  "mlogitFit.csv"]
    subprocess.call(args)


    try:
        with open(output_directory +  "mlogitFit.csv", 'r') as mlogit:
            mlogit.next() # skip header

            readline = mlogit.next()
            print(readline)
            sablefish = float(readline.split(",")[1])
            readline = mlogit.next()
            revenue = float(readline.split(",")[1])
            readline = mlogit.next() #skip, likelihood
            readline = mlogit.next()
            habit = float(readline.split(",")[1])
            readline = mlogit.next()
            distance = float(readline.split(",")[1])
            readline = mlogit.next()
            dover = float(readline.split(",")[1])
    except IOError:
         return {"habit": float('nan'), "distance":float('nan'), "Sablefish_cpue": float('nan'), "revenue": float('nan'), "`Dover Sole_cpue`": float('nan') }
    #numbers from temporary summary

    return {"habit": habit, "distance":distance, "Sablefish_cpue": sablefish, "revenue": revenue, "`Dover Sole_cpue`": dover }

import csv


#random is missing (because all the fishermen quit)
filenames = [ "intercepts.yaml" ]
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
        results.append([result["Sablefish_cpue"],result["revenue"],result["distance"],result["habit"],result["`Dover Sole_cpue`"]])
        with open(sweep_directory + "/" + original_file + ".csv", "a") as f:
            f.write(str(result["Sablefish_cpue"]) + "," + str(result["revenue"]) + "," + str(result["distance"]) + "," + str(result["habit"]) + "," + str(result["`Dover Sole_cpue`"]) +"\n")
    print results

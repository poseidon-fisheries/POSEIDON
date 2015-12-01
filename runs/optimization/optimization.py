from __future__ import print_function
import GPyOpt
import GPy
import yaml
import numpy
import subprocess
import os
import itertools
import pickle


def optimize(
        title,
        input2yaml,
        bounds,
        directory="/home/carrknight/code/oxfish/runs/optimization",
        runs=20, steps_per_run=10,
        plot=True,
        jarfile="yamler.jar"):
    """
    Runs parameter optimization through over the jar file through the GPyOpt library for bayesian optimization
    :param directory:  the directory where the jar file is and where we will output our results
    :param title: the title of this optimization (important to set where to store results)
    :param input2yaml: function turning numpy arrays of parameters into the YAML file to feed to the jar
    :param runs: how many times should we run a set of optimization simulations
    :param steps_per_run: for each run how many simulations will we run?
    :param plot: True if we want to plot the acquisition and convergence functions
    :return: nothing
    """
    # set up the counter and get the right directory
    counter = itertools.count()
    os.chdir(directory)
    results_directory = directory + "/" + title + "_result/"

    # read up data if you stored some intermediate result
    try:
        initial_x = numpy.load(results_directory + title + "_X.save")
        initial_y = numpy.load(results_directory + title + "_Y.save")
    except:
        initial_x = None
        initial_y = None
    print(str(initial_y))
    # set up the optimization
    experiment = lambda x: run_experiment(x, input2yaml, title, counter=counter,
                                          jarfile=jarfile,
                                          main_directory=directory)
    optimization = GPyOpt.methods.BayesianOptimization(f=experiment,
                                                       bounds=bounds,
                                                       X=initial_x,
                                                       Y=initial_y,
                                                       normalize=True
                                                       )

    for i in range(runs):
        optimization.run_optimization(steps_per_run)
        print(optimization.x_opt)
        print(optimization.fx_opt)
        print(optimization.X)
        print(optimization.Y)
        if not os.path.exists(results_directory):
            os.makedirs(results_directory)
        with open(results_directory + title + "_best", "w") as bestout:
            print(str(optimization.x_opt) + " ---> " + str(optimization.fx_opt), file=bestout)
            optimization.X.dump(results_directory + title + "_X.save")
            optimization.Y.dump(results_directory + title + "_Y.save")
    if plot:
        optimization.plot_acquisition(filename=results_directory + title + "_acquisition.png")
        optimization.plot_convergence(filename=results_directory + title + "_convergence.png")

    with open(results_directory + title + "_model.b", "wb") as model_out:
        pickle.dump(optimization.model, model_out)


# one dimensional function
def run_experiment(input, input2yaml, experiment_title, counter,
                   jarfile="yamler.jar",
                   main_directory="/home/carrknight/code/oxfish/runs/optimization"):

    results = []
    # each row is a run
    for row in range(len(input)):
        # parse input variables and turn it into a yaml file
        experiment_name = experiment_title + "_" + str(counter.next())
        input2yaml(experiment_name + ".yaml", input[row, :])
        # feed it into the simulation and run it
        subprocess.call(["java", "-jar", jarfile, experiment_name + ".yaml"])
        # read up the results
        os.remove(experiment_name + ".yaml")
        output = open(main_directory + "/output/" + experiment_name + "/result.txt")
        result = -float(output.readline())
        print("result " + str(result))
        # append them in a list of outputs
        results.append([result])
    # return as numpy array
    print(str(numpy.array(results)))
    print(str(input))
    return numpy.array(results)


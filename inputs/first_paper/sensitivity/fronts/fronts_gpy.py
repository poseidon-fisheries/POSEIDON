import numpy
import sensitivity_core_gpy
import sklearn.linear_model as lm

original_yaml = sensitivity_core_gpy.PROJECT_DIRECTORY + "inputs/first_paper/fronts.yaml"

bounds = [{'name': 'gas_prices', 'type': 'continuous', 'domain': (0, 10)},
          {'name': 'cell_size', 'type': 'continuous', 'domain': (1, 20)},
          {'name': 'speed', 'type': 'continuous', 'domain': (0.1, 20)},
          {'name': 'capacity', 'type': 'continuous', 'domain': (100, 20000)},
          {'name': 'movement', 'type': 'continuous', 'domain': (0, 0.1)},
          {'name': 'epsilon', 'type': 'continuous', 'domain': (0, 1)},
          {'name': 'catchability', 'type': 'continuous', 'domain': (0, 0.5)},
          {'name': 'hold_size', 'type': 'continuous', 'domain': (0, 0.5)},
          ]

experiment_name = "fronts_sensitivity_gpy"


# reads the yaml file in and over-writes parameters
def build_input(filename,
                gas_prices,
                cell_size,
                speed,
                capacity,
                movement,
                epsilon,
                catchability,
                hold_size):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    data["Abstract"]["gasPricePerLiter"] = float(gas_prices)
    data["Abstract"]["mapInitializer"]["Simple Map"]["cellSizeInKilometers"] = float(cell_size)
    data["Abstract"]["speedInKmh"] = float(speed)

    data["Abstract"]["biologyInitializer"]["Diffusing Logistic"]["carryingCapacity"] = float(capacity)
    data["Abstract"]["biologyInitializer"]["Diffusing Logistic"]["differentialPercentageToMove"] = float(movement)

    data["Abstract"]["destinationStrategy"]["Imitator-Explorator"]["probability"]["Fixed Probability"][
        "explorationProbability"] = float(epsilon)
    data["Abstract"]["gear"]["Random Catchability"]["meanCatchabilityFirstSpecies"] = float(catchability)

    data["Abstract"]["holdSize"] = float(hold_size)

    print("ready to output " + filename)
    #   print(data)
    # the directory where it is outputted depends fundamentally on the caller, not the responsibility
    # of this class
    try:
        with open(filename, 'w') as outfile:
            outfile.write(str(data))
        print("done with the output")
    except TypeError as err:
        print "Unexpected error:" + str(err)
        print "Unexpected error:" + str(err.args)

    print("dumped file at: " + filename)


def scorer(yamlfile):
    # run a regression looking for a trend between time and average X towed
    xs = [float(x) for x in yamlfile["FishState Daily"]["Average X Towed"]]
    xs = [x for x in xs if not numpy.isnan(x)]

    regression = lm.LinearRegression()
    regression = regression.fit(X=numpy.array(range(len(xs))).reshape(len(xs),1),
                                y=numpy.array(xs))
    # difference between the average x before and after
    # if they go as a front the end x should be below they start x
    slope = regression.coef_[0]
    # if there is no front then the slope is negative

    # looking for where there is no front, but bayesian minimizes so we flip the sign once again
    return -slope


sensitivity_core_gpy.optimize(experiment_name,
                          build_input,
                          bounds,
                          additional_data=True,
                          results_directory= sensitivity_core_gpy.PROJECT_DIRECTORY + "runs/first_paper/sensitivity/fronts",
                          scorer=scorer,
                          years_to_run=3)

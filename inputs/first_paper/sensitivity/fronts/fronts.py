import numpy
import sklearn.linear_model as lm
import spearfish
import sys

original_yaml = "/home/carrknight/code/oxfish/inputs/first_paper/fronts.yaml"

bounds = [{'name': 'gas_prices', 'type': 'continuous', 'domain': (0, 10)},
          {'name': 'cell_size', 'type': 'continuous', 'domain': (1, 20)},
          {'name': 'speed', 'type': 'continuous', 'domain': (0.1, 20)},
          {'name': 'capacity', 'type': 'continuous', 'domain': (100, 20000)},
          {'name': 'movement', 'type': 'continuous', 'domain': (0, 0.1)},
          {'name': 'epsilon', 'type': 'continuous', 'domain': (0, 1)},
          {'name': 'catchability', 'type': 'continuous', 'domain': (0, 0.5)},
          {'name': 'hold_size', 'type': 'continuous', 'domain': (0, 0.5)},
          ]



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
    # run a regression looking for a trend between time and distance from port
    xs = [float(x) for x in yamlfile["FishState Daily"]["Average Distance From Port"]]
    #skip first month (randomness)
    xs = xs[30:]
    xs = [x for x in xs if not numpy.isnan(x)]

    regression = lm.LinearRegression()
    regression = regression.fit(X=numpy.array(range(len(xs))).reshape(len(xs),1),
                                y=numpy.array(xs))
    # we fail if the distance from port doesn't increase over time
    # so for this ANT we are looking to minimize the regression slope
    slope = regression.coef_[0]
    return slope


# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stdout.write("Starting!")

    experiment_name = "fronts_sensitivity_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['gas_prices'][0],
                                       params['cell_size'][0],
                                       params['speed'][0],
                                       params['capacity'][0],
                                       params['movement'][0],
                                       params['epsilon'][0],
                                       params['catchability'][0],
                                       params['hold_size'][0])
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name, scorer=scorer,
                                      years_to_run=3,additional_data=True)

    return result




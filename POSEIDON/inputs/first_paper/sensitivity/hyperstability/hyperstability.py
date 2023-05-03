import numpy
import sklearn.linear_model as lm
import spearfish
import sys

original_yaml = "/home/carrknight/code/oxfish/inputs/first_paper/hyperstability.yaml"


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
    #do not limit movement:
    data["Abstract"]["biologyInitializer"]["Diffusing Logistic"]["percentageLimitOnDailyMovement"] = float(1)

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
    effort = [float(x) for x in yamlfile["FishState"]["Total Effort"]]
    landings = [float(x) for x in yamlfile["FishState"]["Species 0 Landings"]]
    cpue = []
    for i in range(len(effort)):
        cpue.append(landings[i] / effort[i])

    # +1000 is just to avoid infinity.
    biomass = [float(x) for x in yamlfile["FishState"]["Biomass Species 0"]]

    cpue = numpy.log(cpue)
    final_biomass = biomass[-1]
    biomass = numpy.log(biomass)
    regression = lm.LinearRegression()
    regression = regression.fit(X=numpy.array(biomass).reshape(len(biomass),1),
                                y=cpue)
    # we want to maximize it, so we will negate the slope
    slope = regression.coef_[0]
    r_squared = regression.score(numpy.array(biomass).reshape(len(biomass),1),numpy.array(cpue))

    average_profits = float(yamlfile["FishState"]["Average Cash-Flow"][-1])

    print str(r_squared)
    return {"slope": -slope,
            "positive_landings": (final_biomass-2000), #there needs to be positive biomass all the way to the end
            "decent_fit": (r_squared-.2), #it can't be a weird thing where the fit is crazy bad
            "average_profits" : average_profits}  #must be making positive profits



def best_scorer(yamlfile):
    original_score = scorer(yamlfile)
    original_score["slope"] = -original_score["slope"]
    return original_score

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
                                      experiment_name,
                                      scorer=best_scorer,
                                      years_to_run=40, additional_data=False)

    return result

import spearfish
import sys
import math

original_yaml = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/half.yaml"


# reads the yaml file in and over-writes parameters
def build_input(filename, red, blue, season_length, height, width, x, y,
                tradeable, mpa_duration, mpa_start
                ):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    quotas = "0:" + str(float(red)) + ",1:" + str(float(blue))
    data["Abstract"]["regulation"] = dict()
    data["Abstract"]["regulation"]["Kitchen Sink"] = \
        {
            "individualTradeableQuotas": tradeable,
            "seasonLength": int(season_length),
            "yearlyQuotaMaps": quotas,
            "duration": int(mpa_duration),
            "startDay": int(mpa_start)
        }

    # you might need predictors!
    data['Abstract']['usePredictors'] = True

    # create MPA
    print("height, width:" + str(int(height)) + "," + str(int(width)))

    # we could put it in the regulation object too, but starting mpas
    # can also be placed in the scenario directly; same effect
    if int(height) > 0 and int(width) > 0:
        print("building MPA")
        data["Abstract"]["startingMPAs"] = [{
            "height": int(height),
            "width": int(width),
            "topLeftX": int(x),
            "topLeftY": int(y)
        }]

    print("ready to output " + filename)
    #   print(data)
    # the directory where it is outputted depends fundamentally on the caller, not the responsibility
    # of this class
    try:
        with open(filename, 'w') as outfile:
            outfile.write(yaml.dump(data, default_flow_style=False))
        print("done with the output")
    except TypeError as err:
        print "Unexpected error:" + str(err)
        print "Unexpected error:" + str(err.args)

    print("dumped file at: " + filename)


def scorer(yamlfile, output_directory):
    cash = -sum([float(x) for x in yamlfile["FishState"]["Species 0 Landings"]]) - float(
        yamlfile["FishState"]["Biomass Species 1"][-1])
    return {"cash": cash}


# Write a function like this called 'main'
def main(job_id, params):
    print 'Anything printed here will end up in the output directory for job #%d' % job_id
    sys.stdout.write("Starting!")

    experiment_name = "kitchensink_tac_half_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['first'][0],
                                       params['second'][0],
                                       params['length'][0],
                                       params['height'][0],
                                       params['width'][0],
                                       params['x'][0],
                                       params['y'][0],
                                       "false",
                                       params['mpa_duration'],
                                       params['start_day'],
                                       )
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name,
                                      scorer=scorer,
                                      years_to_run=20,
                                      additional_data=False)

    return result

import spearfish
import sys
import math

original_yaml = "/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/mixed.yaml"


# reads the yaml file in and over-writes parameters
def build_input(filename, height, width, x, y,
                mpa_duration
                ):
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    data["Abstract"]["regulation"] = dict()
    data["Abstract"]["regulation"]["Temporary MPA"] = \
        {
            "duration": int(mpa_duration),
            "startDay": 0
        }

    # you might need predictors!
    data['Abstract']['usePredictors'] = True

    # create MPA
    print("height, width:" + str(int(height)) + "," + str(int(width)))
    if int(height) > 0 and int(width) > 0:
        print("building MPA")
        data["Abstract"]["regulation"]["Temporary MPA"]["startingMPAs"] = [{
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

    experiment_name = "mpa-alone_" + str(job_id)

    print "parameters:"
    print params

    yaml_input = lambda x: build_input(x,
                                       params['height'][0],
                                       params['width'][0],
                                       params['x'][0],
                                       params['y'][0],
                                       params['mpa_duration']
                                       )
    result = spearfish.run_experiment(yaml_input,
                                      experiment_name,
                                      scorer=scorer,
                                      years_to_run=20,
                                      additional_data=False)

    return result

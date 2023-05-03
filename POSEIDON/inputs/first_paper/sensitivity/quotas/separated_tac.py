import sensitivity_core_gpy

bounds = [{'name': 'x', 'type': 'continuous', 'domain': (0, 2000000)},
          {'name': 'y', 'type': 'continuous', 'domain': (0, 2000000)}]

original_yaml = sensitivity_core_gpy.PROJECT_DIRECTORY + "/inputs/first_paper/sensitivity/quotas/separated.yaml"


# TACS
def build_tac_input(filename, x, y):
    '''
    sets up the yaml to feed into the java model to have ITQs running and trading
    :param filename: the name of the yaml to output
    :param x: the numpy array with the parameters. I just expect 2: quota for first and quota for second species
    :return: nothing
    '''
    import yaml

    print("given " + filename)

    with open(original_yaml, 'r') as infile:
        data = yaml.load(infile)

    data["Abstract"]["regulation"] = dict()
    data["Abstract"]["regulation"]["Multi-TAC"] = {
        "firstSpeciesQuota": float(x)  ,
        "otherSpeciesQuota": float(y)
    }
    # no need for predictors
    data['Abstract']['usePredictors'] = "false"

    print("ready to output " + filename)
    print(data)
    try:
        with open(filename, 'w') as outfile:
            outfile.write(str(data))
        print("done with the output")
    except TypeError as err:
        print "Unexpected error:" + str(err)
        print "Unexpected error:" + str(err.args)

    print("dumped file at: " + filename)


def scorer(yamlfile):
    print(yamlfile)
    print(yamlfile["FishState"]["Species 0 Landings"])
    return -float(yamlfile["FishState"]["Biomass Species 1"][-1]) - sum(yamlfile["FishState"]["Species 0 Landings"])


experiment_name = "optimal_tac_separated_nn"

sensitivity_core_gpy.optimize(experiment_name,
                              build_tac_input,
                              bounds,
                              additional_data=False,
                              results_directory=sensitivity_core_gpy.PROJECT_DIRECTORY + "runs/first_paper/quotas",
                              scorer=scorer,
                              years_to_run=20)

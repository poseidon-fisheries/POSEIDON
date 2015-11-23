import optimization
import yaml





def build_itq_input(filename, x):
    '''
    sets up the yaml to feed into the java model to have ITQs running and trading
    :param filename: the name of the yaml to output
    :param x: the numpy array with the parameters. I just expect 2: quota for first and quota for second species
    :return: nothing
    '''
    with open("base.yaml", 'r') as infile:
        data = yaml.load(infile)

    # change regulations
    data["Prototype"]["regulation"] = dict()
    data["Prototype"]["regulation"]["Multi-ITQ"] = {
        "quotaFirstSpecie": max(0, float(x[0])), #sometimes 0 is made negative and that's annoying
        "quotaOtherSpecies": max(0, float(x[1]))
    }
    # you also need to activate the predictors
    data['Prototype']['usePredictors'] = "true"

    with open(filename, 'w') as outfile:
        outfile.write(yaml.dump(data, default_flow_style=True))

bounds = [(0, 10000), (0, 10000)]
title = "mixed_itq"

optimization.optimize(title,build_itq_input,bounds)

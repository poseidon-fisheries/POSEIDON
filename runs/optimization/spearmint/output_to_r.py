from spearmint.utils.database.mongodb import MongoDB
import optparse
import json
import os
import sys
from collections import OrderedDict
import importlib
import spearmint
import yaml
from spearmint.main import parse_resources_from_config, load_jobs, remove_broken_jobs

SPACING = 500


def print_dict(d, level=1):
    if isinstance(d, dict):
        if level > 1: print ""
        for k, v in d.iteritems():
            print "  " * level, k,
            print_dict(v, level=level + 1)
    else:
        print d


import numpy as np


def cartesian(arrays, out=None):
    """
    Generate a cartesian product of input arrays.

    Parameters
    ----------
    arrays : list of array-like
        1-D arrays to form the cartesian product of.
    out : ndarray
        Array to place the cartesian product in.

    Returns
    -------
    out : ndarray
        2-D array of shape (M, len(arrays)) containing cartesian products
        formed of input arrays.

    Examples
    --------
    >>> cartesian(([1, 2, 3], [4, 5], [6, 7]))
    array([[1, 4, 6],
           [1, 4, 7],
           [1, 5, 6],
           [1, 5, 7],
           [2, 4, 6],
           [2, 4, 7],
           [2, 5, 6],
           [2, 5, 7],
           [3, 4, 6],
           [3, 4, 7],
           [3, 5, 6],
           [3, 5, 7]])

    """

    arrays = [np.asarray(x) for x in arrays]
    dtype = arrays[0].dtype

    n = np.prod([x.size for x in arrays])
    if out is None:
        out = np.zeros([n, len(arrays)], dtype=dtype)

    m = n / arrays[0].size
    out[:, 0] = np.repeat(arrays[0], m)
    if arrays[1:]:
        cartesian(arrays[1:], out=out[0:m, 1:])
        for j in xrange(1, arrays[0].size):
            out[j * m:(j + 1) * m, 1:] = out[0:m, 1:]
    return out


def get_options(expt_dir, config_file="config.json"):
    parser = optparse.OptionParser(usage="usage: %prog [options] directory")

    parser.add_option("--config", dest="config_file",
                      help="Configuration file name.",
                      type="string", default="config.json")

    expt_dir = os.path.realpath(os.path.expanduser(expt_dir))
    if not os.path.isdir(expt_dir):
        raise Exception("Cannot find directory %s" % expt_dir)
    expt_file = os.path.join(expt_dir, config_file)

    try:
        with open(expt_file, 'r') as f:
            options = json.load(f, object_pairs_hook=OrderedDict)
    except:
        raise Exception("config.json did not load properly. Perhaps a spurious comma?")
    options["config"] = config_file

    # Set sensible defaults for options
    options['chooser'] = options.get('chooser', 'default_chooser')
    if 'tasks' not in options:
        options['tasks'] = {'main': {'type': 'OBJECTIVE', 'likelihood': options.get('likelihood', 'GAUSSIAN')}}

    # Set DB address
    db_address = "localhost"
    if 'database' not in options:
        options['database'] = {'name': 'spearmint', 'address': db_address}
    else:
        options['database']['address'] = db_address

    if not os.path.exists(expt_dir):
        sys.stderr.write("Cannot find experiment directory '%s'. "
                         "Aborting.\n" % (expt_dir))
        sys.exit(-1)

    return options


def plot(config_directory="/home/carrknight/code/oxfish/runs/optimization/spearmint"):
    os.chdir("/home/carrknight/code/oxfish/runs/optimization/spearmint")
    options = get_options(config_directory,
                          config_file="config.json")
    experiment_name = str(options['experiment-name'])
    db = MongoDB()
    resources = parse_resources_from_config(options)
    resource = resources.itervalues().next()

    # load hyper parameters
    chooser_module = importlib.import_module('spearmint.choosers.' + options['chooser'])
    chooser = chooser_module.init(options)
    print "chooser", chooser

    hypers = db.load(experiment_name, "hypers")
    print "loaded hypers", hypers  # from GP.to_dict()

    jobs = load_jobs(db, experiment_name)
    remove_broken_jobs(db, jobs, experiment_name, resources)
    task_options = {task: options["tasks"][task] for task in resource.tasks}
    task_group = spearmint.main.load_task_group(db, options, resource.tasks)

    hypers = spearmint.main.load_hypers(db, experiment_name)
    print "loaded hypers", hypers  # from GP.to_dict()

    hypers = chooser.fit(task_group, hypers, task_options)
    print "\nfitted hypers:"
    print(hypers)

    lp, x = chooser.best()
    x = x.flatten()
    print "best", lp, x
    bestp = task_group.paramify(task_group.from_unit(x))
    print "expected best position", bestp

    print "chooser models:", chooser.models
    obj_model = chooser.models[chooser.objective['name']]
    grid = chooser.grid
    obj_mean, obj_var = obj_model.function_over_hypers(obj_model.predict, grid)

    import numpy as np

    bounds = dict()

    for task_name, task in task_group.tasks.iteritems():
        # make a grid, feed it to the predictor:

        dimensions = ()
        for key in options['variables'].keys():
            type = str(options['variables'][key]["type"]).strip().lower()
            keyname =  key.encode('utf-8')
            bounds[keyname] = dict()
            bounds[keyname]["type"] = type
            if type == "float":
                dimension = np.linspace(0, 1, num=SPACING)
                dimensions = dimensions + (dimension,)
                bounds[keyname]["min"] = options['variables'][key]["min"]
                bounds[keyname]["max"] = options['variables'][key]["max"]

            elif type == "int":
                min = int(options['variables'][key]["min"])
                max = int(options['variables'][key]["max"])
                dimension = np.linspace(0, 1, num=max - min)
                bounds[keyname]["min"] = min
                bounds[keyname]["max"] = max
                #  dimension = np.array([x + min for x in range(max - min + 1)])
                dimensions = dimensions + (dimension,)
            else:
                bounds[keyname]["options"] = options['variables'][key]["options"]
                assert type == "enum"
                dimension = tuple([(0, 1) for i in range(len(options['variables'][key]["options"]))])
                for t in dimension:
                    dimensions = dimensions + (t,)
                    # print(dimension)

        data = cartesian(np.array(dimensions))

        mean, variance = chooser.models[task_name].predict(data)
        mean = [task.unstandardize_mean(task.unstandardize_variance(v)) for v in mean]
        variance = [task.unstandardize_variance(np.sqrt(v)) for v in variance]

        os.chdir(config_directory)

        # unzip the data
        new_data = zip(*data.transpose().tolist())
        datum = zip(new_data, mean, variance)

        header = ",".join(options['variables'].keys()) + ",mean,variance"
        with open(experiment_name + ".csv", 'w') as fileout:
            fileout.write(header + "\n")
            for i in range(len(datum)):
                fileout.write(str(datum[i]).replace("(", "").replace(")", "").replace(",,",",") + "\n")

        with open(experiment_name + "_bounds.yaml", 'w') as outfile:
            outfile.write(yaml.dump(bounds, default_flow_style=False))

        # grid = cartesian(dimensions)
        # mean, variance = obj_model.function_over_hypers(obj_model.predict, grid)
        #
        # mean = [obj_task.unstandardize_mean(obj_task.unstandardize_variance(v)) for v in mean]
        # variance = [obj_task.unstandardize_variance(np.sqrt(v)) for v in variance]
        #
        # # xymv = [([x for x in xy], m, v) for xy, m, v in izip(new_grid, obj_mean, obj_std)]  # if .2 < xy[0] < .25]
        # with open(experiment_name + ".csv", 'w') as fileout:
        #     for i in range(len(mean)):
        #         fileout.write(str(([x for x in grid[i]], mean[i], variance[i])).replace("(", "").replace(")", "").
        #                       replace("[", "").replace("]", "") + "\n")
        xy = np.array(task.inputs)
        # function values:
        vals = task.values
        vals = np.array(vals)
        np.savetxt(experiment_name + "_" + task_name + "_runs.csv", xy, delimiter=",", fmt='%.3e')
        np.savetxt(experiment_name + "_" + task_name + "_runs_values.csv", vals, delimiter=",", fmt='%.3e')



        # plot(experiment_name="tac-separated")
        # plot(experiment_name="itq-separated")
        # plot(experiment_name="itq-mixed")
        # plot(experiment_name="unfriend")
        # plot(experiment_name="kitchensink_2")
        # plot(experiment_name="kitchensink_itq")
        # plot(experiment_name="congested_departure")


# plot(config_directory="/home/carrknight/code/oxfish/docs/20161004 adaptive_tax/subsidy")
# plot(config_directory="/home/carrknight/code/oxfish/docs/20161004 adaptive_tax/")
# plot(config_directory="/home/carrknight/code/oxfish/docs/20170103 shodan_test")
# plot(config_directory="/home/carrknight/code/oxfish/docs/20170111 fit_example")
# plot(config_directory="/home/carrknight/code/oxfish/docs/20170322 cali_catch/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170322 cali_catch/profits/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170322 cali_catch/landings/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170322 cali_catch/dover/")

#plot(config_directory="/home/carrknight/code/oxfish/docs/20170403 narrative/itq/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170403 narrative/tac/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170403 narrative/climate/tac/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170403 narrative/climate/itq/")

#plot(config_directory="/home/carrknight/code/oxfish/docs/20170407 climate/base/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170407 climate/climate/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/tac_mixed/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/itq_mixed/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/tac_half/")
#plot(config_directory="/home/carrknight/code/oxfish/docs/20170511 optimisation_remake/itq_half/")
plot(config_directory="/home/carrknight/code/oxfish/docs/20170606 cali_catchability_2/attainment_single_profits")

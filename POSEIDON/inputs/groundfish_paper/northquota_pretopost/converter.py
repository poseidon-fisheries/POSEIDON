# this file runs the basic yaml 100 times to generate realistic confidence intervals to test against!
import os
import sys
from shutil import copyfile

import subprocess

main_directory = "/home/carrknight/code/oxfish/runs/optimization"


# reads the yaml file in and over-writes parameters
def convert_pretopost(file_to_read, file_to_write,
                      deriso=False
                      ):
    import yaml

    scenario = "Simple California" if deriso else "California Map Scenario"

    print("reading " + file_to_read)

    # regulation pre-reset
    prereset = dict()
    prereset["Multiple Regulations"] = dict()
    prereset["Multiple Regulations"]["tags"] = ["all", "all"]
    mpa = dict()
    mpa["MPA by depth"] = dict()
    mpa["MPA by depth"]["maxDepth"] = 275
    mpa["MPA by depth"]["minDepth"] = 0
    prereset["Multiple Regulations"]["factories"] = ["Anarchy", mpa]

    # regulation post-reset
    regulation = dict()
    regulation["Multiple Regulations"] = dict()
    regulation["Multiple Regulations"]["tags"] = ["all", "all"]
    season = dict()
    season["Fishing Season"] = dict()
    season["Fishing Season"]["respectMPA"] = True
    season["Fishing Season"]["seasonLength"] = 120
    quotas = dict()
    quotas["Multi-Quotas from Map"] = dict()
    quotas["Multi-Quotas from Map"]["initialQuotas"] = dict()
    quotas["Multi-Quotas from Map"]["initialQuotas"]["Dover sole"] = 16865996
    quotas["Multi-Quotas from Map"]["initialQuotas"]["Longspine Thornyhead"] = 2577599.9
    quotas["Multi-Quotas from Map"]["initialQuotas"]["Sablefish"] = 3457800
    quotas["Multi-Quotas from Map"]["initialQuotas"]["Shortspine thornyhead"] = 1927800
    quotas["Multi-Quotas from Map"]["initialQuotas"]["Yelloweye rockfish"] = 67130
    quotas["Multi-Quotas from Map"]["quotaType"] = "IQ"
    regulation["Multiple Regulations"]["factories"] = [season, quotas]

    # discarding
    discarding = dict()
    discarding["Specific Discarding"] = dict()
    discarding["Specific Discarding"]["indices"] = "4"

    with open(file_to_read, 'r') as infile:
        data = yaml.load(infile)
        data[scenario]["discardingStrategy"] = discarding
        data[scenario]["regulationPreReset"] = prereset
        data[scenario]["regulationPostReset"] = regulation
        data[scenario]["hourlyTravellingCosts"] = 94.520833333
        if deriso:
            data[scenario]["derisoFileNames"] = "deriso_2007.yaml"
        else:
            data[scenario]["countFileName"] = "count_2007.csv"
        data[scenario]["gasPriceMaker"]["Gas Price from File"][
            "csvFile"] = "inputs/california/2007_gasprice.csv"
        print("ready to output " + file_to_write)
        #   print(data)
        # the directory where it is outputted depends fundamentally on the caller, not the responsibility
        # of this class
        try:
            with open(file_to_write, 'w') as outfile:
                outfile.write(yaml.dump(data, default_flow_style=False))
            print("done with the output")
        except TypeError as err:
            print "Unexpected error:" + str(err)
            print "Unexpected error:" + str(err.args)

        print("dumped file at: " + file_to_write)





# removes quotas for yelloweye and rewrites prices map
def yelloweye_price_convert(file_to_read, file_to_write,
                      deriso=False, yelloweye_price =1.0754502
                      ):
    import yaml

    scenario = "Simple California" if deriso else "California Map Scenario"

    print("reading " + file_to_read)

    with open(file_to_read, 'r') as infile:
        data = yaml.load(infile)
        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1]["Multi-Quotas from Map"][
            "initialQuotas"].pop("Yelloweye rockfish")
        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1]["Multi-Quotas from Map"]["quotaExchangedPerMatch"].pop("Yelloweye rockfish")

        data[scenario]["priceMap"] = "Dover Sole:0.6698922,Sablefish:4.3235295,Shortspine Thornyhead:1.0428510," \
                                     "Longspine Thornyhead:1.0428510,Yelloweye Rockfish:"+str(yelloweye_price)+",Others:1.7646181 "
        print("ready to output " + file_to_write)
        #   print(data)
        # the directory where it is outputted depends fundamentally on the caller, not the responsibility
        # of this class
        try:
            with open(file_to_write, 'w') as outfile:
                outfile.write(yaml.dump(data, default_flow_style=False))
            print("done with the output")
        except TypeError as err:
            print "Unexpected error:" + str(err)
            print "Unexpected error:" + str(err.args)

        print("dumped file at: " + file_to_write)


# reads the yaml file in and over-writes parameter
# this builds the north-quota variant!
def sablefish_convert(file_to_read, file_to_write,
                      deriso=False
                      ):
    import yaml

    scenario = "Simple California" if deriso else "California Map Scenario"

    print("reading " + file_to_read)

    with open(file_to_read, 'r') as infile:
        data = yaml.load(infile)
        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1]["Multi-Quotas from Map"][
            "initialQuotas"]["Sablefish"] = 1606257.5
        data[scenario]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"][
            "Shortspine Thornyhead"]["Double Normal Selectivity Gear"]["averageCatchability"] = float(
            0.00020312531999999999)
        data[scenario]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"][
            "Yelloweye Rockfish"]["Logistic Selectivity Gear"]["averageCatchability"] = float(
            0.001029348405)
        data[scenario]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"][
            "Sablefish"]["Sablefish Trawl Selectivity Gear"]["averageCatchability"] = float(0.000405)
        data[scenario]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"][
            "Dover Sole"]["Double Normal Selectivity Gear"]["averageCatchability"] = float(
            0.001215)
        data[scenario]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"][
            "Longspine Thornyhead"]["Logistic Selectivity Gear"]["averageCatchability"] = float(
            0.0006072663149999999)
        data[scenario]["holdSizePerBoat"] = float(20000.0)

        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1][
            "Multi-Quotas from Map"]["quotaExchangedPerMatch"]["Dover sole"] = int(100)
        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1][
            "Multi-Quotas from Map"]["quotaExchangedPerMatch"]["Shortspine thornyhead"] = int(100)
        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1][
            "Multi-Quotas from Map"]["quotaExchangedPerMatch"]["Sablefish"] = int(100)
        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1][
            "Multi-Quotas from Map"]["quotaExchangedPerMatch"]["Longspine Thornyhead"] = int(100)
        data[scenario]["regulationPostReset"]["Multiple Regulations"]["factories"][1][
            "Multi-Quotas from Map"]["multipleTradesAllowed"] = False

        print("ready to output " + file_to_write)
        #   print(data)
        # the directory where it is outputted depends fundamentally on the caller, not the responsibility
        # of this class
        try:
            with open(file_to_write, 'w') as outfile:
                outfile.write(yaml.dump(data, default_flow_style=False))
            print("done with the output")
        except TypeError as err:
            print "Unexpected error:" + str(err)
            print "Unexpected error:" + str(err.args)

        print("dumped file at: " + file_to_write)



filenames = [
    #"default.yaml", "clamped.yaml", "eei.yaml",
    #"perfect.yaml", "random.yaml", "bandit.yaml",
    #"annealing.yaml", "intercepts.yaml", "kernel.yaml",
    "nofleetwide_identity.yaml",
    "nofleetwide.yaml", "fleetwide.yaml", 
    #"perfect_cell.yaml"
]

source = "../northquota/"
sink = "../northquota_pretopost/"
for filename in filenames:
    convert_pretopost(source + filename,
                      sink + filename,
                      deriso=False)



# this file runs the basic yaml 100 times to generate realistic confidence intervals to test against!
import os
import sys
from shutil import copyfile

import subprocess


clamped_to_look = "/home/carrknight/code/oxfish/docs/groundfish/calibration/northquota_yesgarbage/map/best.yaml"


# reads the yaml file in and over-writes parameters
def convert_cpuemap(file_to_read, file_to_write
                      ):
    import yaml

    with open(clamped_to_look, 'r') as clampedinfile:
		  clamped = yaml.load(clampedinfile)
		  with open(file_to_read, 'r') as infile:
			  data = yaml.load(infile)
			  data["California Map Scenario"]["spatialFileName"]= "spatial_cpue.csv"
			  
			  
			  data["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Dover Sole"]["Double Normal Selectivity Gear"]["averageCatchability"]=clamped["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Dover Sole"]["Double Normal Selectivity Gear"]["averageCatchability"]
			  
			  data["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Longspine Thornyhead"]["Logistic Selectivity Gear"]["averageCatchability"]=clamped["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Longspine Thornyhead"]["Logistic Selectivity Gear"]["averageCatchability"]	
						  
			  data["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Sablefish"]["Sablefish Trawl Selectivity Gear"]["averageCatchability"]=clamped["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Sablefish"]["Sablefish Trawl Selectivity Gear"]["averageCatchability"]				
						  
			  data["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Shortspine Thornyhead"]["Double Normal Selectivity Gear"]["averageCatchability"]=clamped["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Shortspine Thornyhead"]["Double Normal Selectivity Gear"]["averageCatchability"]				
						  
			  data["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Yelloweye Rockfish"]["Logistic Selectivity Gear"]["averageCatchability"]=clamped["California Map Scenario"]["gear"]["Garbage Gear"]["delegate"]["Heterogeneous Selectivity Gear"]["gears"]\
			  ["Yelloweye Rockfish"]["Logistic Selectivity Gear"]["averageCatchability"]		
			  
			  data["California Map Scenario"]["holdSizePerBoat"] = clamped["California Map Scenario"]["holdSizePerBoat"]
			  
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
    "default.yaml", "clamped.yaml", "eei.yaml",
    "perfect.yaml", "random.yaml", "bandit.yaml",
    "annealing.yaml", "intercepts.yaml", "kernel.yaml",
    "nofleetwide_identity.yaml",
    "nofleetwide.yaml", "fleetwide.yaml", 
    "perfect_cell.yaml"
]

source = "../northquota/"
sink = "../northquota_map/"
for filename in filenames:
    convert_cpuemap(source + filename,
                      sink + filename)



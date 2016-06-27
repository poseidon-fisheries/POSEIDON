# Change Log

Temporary as we are still unversioned but I figured we'd keep track of everything that changed here

## 2016-06-27

### Added
- Kernel Regression and the interface for Geographical Regressions
- Basic elements for a possibly unworkable Hidden Markov Chain guesstimator.



## 2016-06-21

### Fixed
- Removed from use the TACOpportunityCostManager. It's a cute idea but over-engineered and I think pushes
TAC users towards very strong results. It is easier to remove than to explain anyway. It's great because
I think it captures the incentive to go catch the protected quota but I don't know how realistic it is.
- locational ITQ and TAC scenario parameters

### Added
- Sensitivity analysis for locational choices under ITQ

## 2016-06-20

### Added
- New demo inklings for a heterogeneous ITQ model
- Biology initializer with left to right 2 well-mixed species.



## 2016-06-16

### Changed
- Sensitivity analysis 



## 2016-06-15

### Added
- Another ANT test for hard-switch example. This was the last conceptual result needing an ANT. Next up is policy results.


## 2016-06-13
### Added
- Gear Adaptation Strategy which should make it possible to add/remove gear adaptation from YAML

### Changed
- ITQSplitUnmuffled now uses "Split in Half" rather than "Half Bycatch" as its biology layer
- ITQDrivesMileage now uses "trawling only" gas consumption gear. This is because the
 test had become "weak" (the test would fail about 5% of the times) after I decoupled 
 trawling consumption from movement. This would mean that movement might matter more for gas consumption
  and the effect of better mileage on catches would get noisier (correlation coefficient of about .6). 
  Switching movement gas consumption off restores much stronger correlation (.8 or .9)
- Changed gas prices in the "Gas Mileage Evolution" figure of the paper since now trawling consumption
 by default is not that bad.

### Removed
- Disfunctional/Functional Friends. People didn't much react to this 
during the workshops/conferences and it needs to be updated every time I change the meaning of exploration. 
Dump it for now and focus on it separately. 
- Gear Imitation Analysis. Superseded by Gear Adaptation Strategy

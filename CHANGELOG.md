# Change Log

Temporary as we are still unversioned but I figured we'd keep track of everything that changed here


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

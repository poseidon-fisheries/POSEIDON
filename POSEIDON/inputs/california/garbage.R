library(stringr)
library(readr)
dts <- read_csv("~/Downloads/dtstrawllandings20102014.csv")

library(dplyr)

proportions<-
  dts %>%
  mutate(simulated=str_detect(EDCSPID,"Dover sole|Rockfish|Sablefish|Thornyheads")) %>%
  group_by(YEAR,simulated) %>%
  summarise(caught=sum(MTS))

library(tidyr)  
proportions %>%
  spread(simulated,caught)  %>%
  mutate(prop = `FALSE`/`TRUE`)

dts %>%
  mutate(simulated=str_detect(EDCSPID,"Dover sole|Rockfish|Sablefish|Thornyheads")) %>%
 filter(simulated==TRUE)
  

# now find out prices in kg!
dts %>%
  mutate(price=REV/(MTS*1000)) %>%
  filter(str_detect(EDCSPID,"Dover sole|Rockfish|Sablefish|Thornyheads")) 
  
#now find it for the rest
dts %>%
  filter(!str_detect(EDCSPID,"Dover sole|Rockfish|Sablefish|Thornyheads")) %>%
  group_by(YEAR) %>%
  summarise(caught=sum(MTS)*1000,REV=sum(REV)) %>%
  mutate(price=caught/REV)

# Modification of grinder.R
# Script fitting mlogit to logbook-like data
# Useful for many different reasons

## MLOGIT
library(tidyverse)
library(mlogit)

arguments <- commandArgs(trailingOnly = TRUE)
print("going to read: ")
print( arguments[1])
print("going to write :")
print(arguments[2])
testlog <- read_csv(arguments[1])

# we are dealing with intercepts, so that if an area is never visited we need to remove it!
valid_areas<-
  unique(testlog %>% filter(year>2) %>% filter(choice=="yes") %>% select(option))

# need a single choice id (the original file has id for choice and for fisher)
testlog <-
  testlog %>% 
  filter(year>2) %>%
  filter(option %in% valid_areas$option) %>%
  mutate(id_temp=paste(id,"-",episode,sep="")) %>% 
  mutate(chid=dense_rank(id_temp))  %>% 
  #turn choice into boolean
  mutate(choice=ifelse(choice=="yes",TRUE,FALSE)) %>%
  select(-id,-episode)



#fit!
fishing<-mlogit.data(as.data.frame(testlog),
                     choice="choice",
                     shape="long",alt.var="option",
                     chid.var="chid")
#original:
reg<-mlogit(formula=choice~port_distance+habit_continuous,data=fishing)
#there is unfortunately no broom plugin for mlogit
library(broom)
coeffs<-
  rbind(tidy(reg$coefficients),c("loglikelihood",reg$logLik)) %>%
  arrange(desc(names))
print(coeffs)
write_csv(coeffs,arguments[2])

library(plyr)
library(tidyr)
library(dplyr)
library(ggplot2)



thisFile <- function() {
  cmdArgs <- commandArgs(trailingOnly = FALSE)
  needle <- "--file="
  match <- grep(needle, cmdArgs)
  if (length(match) > 0) {
    # Rscript
    return(normalizePath(sub(needle, "", cmdArgs[match])))
  } else {
    # 'source'd via R console
    return(normalizePath(sys.frames()[[1]]$ofile))
  }
}
#get directory of the script
directory<-dirname(thisFile()) 
#go up one
#print(file.path(directory,commandArgs(TRUE)[1]))
setwd(file.path(directory,commandArgs(TRUE)[1]))

#read all expensive gas files
paths <- dir("gearopt",pattern = "\\.csv", full.names = TRUE)
names(paths) <- basename(paths) #keep the relative path but name them differently which is handy when ldply gets called
gas_data<-
   ldply(paths, read.csv, stringsAsFactors = FALSE,header=FALSE,colClasses="numeric")

gas_data<-
gas_data %>% separate(.id,into=c("scenario","run"),sep="_") %>% mutate(run = factor(paste(run,scenario))) %>% 
  mutate(scenario=factor(scenario)) %>% rename(consumption=V1) 

#add days as the x of the plot
gas_data<-gas_data %>% group_by(run) %>% mutate(days=row_number())


plot1<-ggplot(data= gas_data) + geom_line(aes(y=consumption,x=days,color=scenario,group=run)) + 
  ggtitle("Gas Mileage Evolution") + ylab("Fishing Inefficiency") + xlab("Simulation Days") + 
  scale_color_discrete(labels=c("Expensive","Free"),name="Gas Prices") + theme_gray(20)









ggsave(filename="dashboard.pdf",plot=plot1,dpi=600,width=7.1,height=4)


#if you are given a secondary argument, copy paste your dashboard there!
secondaryFolder<- commandArgs(TRUE)[2]
if(!is.null(secondaryFolder))
{
  file.copy(from="dashboard.pdf",to=file.path(secondaryFolder))
  setwd(file.path(secondaryFolder))
  file.rename(from="dashboard.pdf",to=paste(commandArgs(TRUE)[1],".pdf",sep=""))
  setwd(directory)
}

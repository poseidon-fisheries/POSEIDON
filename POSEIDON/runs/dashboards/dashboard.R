library(plyr)
library(reshape2)
library(tidyr)
library(dplyr)
library(ggplot2)
library(cowplot)

#warning(commandArgs(TRUE)[1])
#warning(commandArgs(TRUE)[2])

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



#     ___                 ___       _   _       _         _   _          
#    / __|___ __ _ _ _   / _ \ _ __| |_(_)_ __ (_)_____ _| |_(_)___ _ _  
#   | (_ / -_) _` | '_| | (_) | '_ \  _| | '  \| |_ / _` |  _| / _ \ ' \ 
#    \___\___\__,_|_|    \___/| .__/\__|_|_|_|_|_/__\__,_|\__|_\___/_||_|
#   
  
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


#    _   ___              _          ___ _____ ___  
#   / | / __|_ __  ___ __(_)___ ___ |_ _|_   _/ _ \ 
#   | | \__ \ '_ \/ -_) _| / -_|_-<  | |  | || (_) |
#   |_| |___/ .__/\___\__|_\___/__/ |___| |_| \__\_\
#          /_/                                                

#now read all 1 species itq, same lldply trick as before
paths <- dir("1itq",pattern = "\\.csv", full.names = TRUE)
names(paths) <- basename(paths) 
itq_data<-
  ldply(paths, read.csv, stringsAsFactors = FALSE,header=FALSE,colClasses="numeric")

itq_data<-itq_data %>% separate(.id,into=c("scenario","run"),sep="_") %>% mutate(run = factor(paste(run,scenario))) %>% 
  mutate(scenario=factor(scenario)) %>% rename(prices=V1) %>% group_by(run) %>% mutate(days= row_number())

itq_data<-
#remove the first year of observation as by then the ITQ market is not in operation
  itq_data %>% filter(days>365) %>%
  #now turn days into date for pretty plotting
  mutate(days = as.Date("2015-01-01")+days)


levels(itq_data$scenario)
plot2<-
  ggplot(data= itq_data) + geom_line(aes(y=prices,x=days,color=scenario,group=run)) + 
  ggtitle("1 Species Quota Prices") + ylab("Last Closing Prices") + xlab("Simulation Days") + 
  scale_color_discrete(breaks=c("rare","common","hypothetical"),
                         labels=c("8000","4000","Hypothetical"),name="Yearly Quota") + theme_gray(20) + ylim(c(0,10)) 


#     ___     _    _ 
#    / __|_ _(_)__| |
#   | (_ | '_| / _` |
#    \___|_| |_\__,_|
#    


meltProperly<-function(grid,policyName){
  return(
    melt(as.matrix(grid)) %>% rename(X = Var1, Y= Var2) %>% 
      mutate(Y = extract_numeric(Y)) %>%
      mutate(X = as.numeric(X)-1, Y= as.numeric(Y)-1) %>%
      mutate(value = ifelse(X>=40,NA,value)) #remove land
      # %>% mutate(policy=policyName)
  )
  
}

meltProperlyCsv<-function(csvFile){
  return(meltProperly(read.csv(csvFile,header=FALSE),basename(csvFile)))
}

paths <- dir("grid",pattern = "^grid_\\d\\.csv", full.names = TRUE)
names(paths) <- basename(paths) #keep the relative path but name them differently which is handy when ldply gets called
gas_data<-
  ldply(paths, meltProperlyCsv)

grouped<-gas_data %>% group_by(X,Y) %>% summarize(value=sum(value))

plot3<-ggplot(data=grouped) + geom_tile(aes(x=X,y=Y,fill=value), col="gray60") + 
  scale_fill_gradient(low="white",high="red",na.value = "#00FF7F",trans="sqrt",name="Tows") +
  geom_hline(yintercept=24,col="black",lwd=0.5,lty=3) +
  ggtitle("ITQ Geography, Average 5 Runs")



#    ___  _     __              _   _               _ 
#   |   \(_)___/ _|_  _ _ _  __| |_(_)___ _ _  __ _| |
#   | |) | (_-<  _| || | ' \/ _|  _| / _ \ ' \/ _` | |
#   |___/|_/__/_|  \_,_|_||_\__|\__|_\___/_||_\__,_|_|
# 


disfunctional<-read.csv("./disfunctional/disfunctional.csv")

plot4<-ggplot(data=disfunctional) + geom_bar(aes(x=friends,y=steps),stat="identity") +
  ggtitle("Days Until Biomass is Consumed") + 
  ylab("# of Days") + ylim(0,5842)  +
  scale_x_discrete("# of Friends")




tosave<-plot_grid(plot1,plot2,plot3,plot4)

ggsave(filename="dashboard.png",plot=tosave,dpi=100,width=19.20,height=10.80)


#if you are given a secondary argument, copy paste your dashboard there!
secondaryFolder<- file.path(commandArgs(TRUE)[2],"assets","oxfish","dashboards")
if(!is.null(secondaryFolder))
{
  file.copy(from="dashboard.png",to=file.path(secondaryFolder))
  setwd(file.path(secondaryFolder))
  file.rename(from="dashboard.png",to=paste(commandArgs(TRUE)[1],".png",sep=""))
  setwd(directory)
}


knitr::opts_chunk$set(echo = FALSE, warning = FALSE, message = FALSE,dpi=300,
                      fig.width = 10, fig.height = 6)

full_directory<-"/home/carrknight/code/oxfish/docs/groundfish/yesgarbage/longrun/"
library(tidyverse)
library(stringr)


statistical_algorithms<- c("Logit","Historical","RUM - precise","RUM","RUM Fleetwide")
adaptive_algorithms<- c("EEI","EEI - Uncalibrated","Heatmap","Social Annealing")

levels=c("intercepts","clamped",
         "nofleetwide_identity","nofleetwide","fleetwide",
         "perfect","perfect_cell",
         "random","bandit","eei","default",
         "kernel","annealing")                             
labels_behaviour=c("Logit","Historical",
                   "RUM - precise","RUM","RUM Fleetwide",
                   
                   "Perfect - SA",
         "Perfect - Cell",
         "Random","Bandit",
         "EEI","EEI - Uncalibrated",
         "Heatmap","Social Annealing"
         )

draw_dashboard<-function(original,title)
{
  simulation<-
    original %>%
    filter(year>1) %>%
    group_by(run) %>%
    summarise_all(mean)
  
  # simulation$sablefish/2724935
  # 
  # simulation$hours_out / simulation$trips
  # simulation$avg_duration
  
  profits<-
    ggplot(simulation) +
    geom_histogram(aes(average_profits)) +
    geom_rect(aes(xmin=118552-2*21331,xmax=118552+2*21331,ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=118552-3*21331,y=0) +
    expand_limits(x=118552+3*21331,y=0) +
    geom_vline(xintercept=118552,lwd=2,col="red",lty=2) +
    ggtitle("Average Profits")
  #profits
  
  
  
  
  
  hours_out<-
    ggplot(simulation) +
    #  geom_histogram(aes(hours_out/24)) +
    geom_rect(aes(xmin=(999.936-2*120)/24,xmax=(999.936+2*120)/24,ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    geom_histogram(aes(actual_hours_out/24)) +
    expand_limits(x=(999.936-3*120)/24,y=0) +
    expand_limits(x=(999.936+3*120)/24,y=0) +
    geom_vline(xintercept=(999.936)/24,lwd=2,col="red",lty=2) +
    ggtitle("Days Out") +
    scale_x_continuous() +
    xlab("Average Days Spent Fishing")
  #hours_out
  
  
  
  sole<-
    ggplot(simulation) +
    geom_rect(aes(xmin=0.3325-2*.0309,
                  xmax=0.3325+2*.0309,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    geom_histogram(aes(sole/22234500)) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=0.3325,lwd=2,col="red",lty=2) +
    ggtitle("Dover Sole Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  #sole
  
  
  #sablefish
  sablefish<-
    ggplot(simulation) +
    geom_histogram(aes(sablefish/2724935)) +
    geom_rect(aes(xmin=.836-2*0.061,
                  xmax=.836+2*0.061,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=.836557879240702,lwd=2,col="red",lty=2) +
    ggtitle("Sablefish Attainment") +
    xlab("% of Yearly Quota Landed") +
    
    scale_x_continuous(labels = scales::percent)
  #sablefish
  
  
  short_thornyheads<-
    ggplot(simulation) +
    geom_histogram(aes(short_thornyheads/1481600.056)) +
    geom_rect(aes(xmin=0.525-2*.0506,
                  xmax=0.525+2*.0506,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=0.525,lwd=2,col="red",lty=2) +
    ggtitle("Shortspine Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  
  
  
  #long_thornyheads
  
  long_thornyheads<-
    ggplot(simulation) +
    geom_histogram(aes(long_thornyheads/1966250.0)) +
    geom_rect(aes(xmin=0.515-2*.0506,
                  xmax=0.515+2*.0506,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=0.515,lwd=2,col="red",lty=2) +
    ggtitle("Longspine Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  #long_thornyheads
  
  
  rockfish<-
    ggplot(simulation) +
    geom_histogram(aes(rockfish/600)) +
    geom_rect(aes(xmin=0.07-2*.02,
                  xmax=0.07+2*.02,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=0.07,lwd=2,col="red",lty=2) +
    ggtitle("Yelloweye Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  #rockfish
  
  
  ##Average duration
  duration<-ggplot(simulation) +
    geom_histogram(aes(x=avg_duration)) +
    geom_rect(aes(xmin=69.097625-2*33,
                  xmax=69.097625+2*33,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0)+
    geom_vline(xintercept=69.097625,lwd=2,col="red",lty=2) +
    
    # expand_limits(x=69.097625-3*33,y=0) +
    expand_limits(x=69.097625+3*33,y=0) +
    ggtitle("Trip's duration")  +
    xlab("Duration(hr)")
  
  
  
  ## Average distance
  distance<-ggplot(simulation) +
    #geom_histogram(aes(x=weighted_distance)) +
    geom_histogram(aes(x=avg_distance)) +
    expand_limits(x=0,y=0)+
    geom_vline(xintercept=90,lwd=2,col="red",lty=2) +
    expand_limits(x=90.88762+3*33,y=0) +
    ggtitle("Trip's distance from port")  +
    geom_rect(aes(xmin=90.88762-2*32,
                  xmax=90.88762+2*32,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    xlab("Distance(km)")
  
  
  
  #error
  library(gridExtra)
  return(
    grid.arrange(profits,hours_out,short_thornyheads,long_thornyheads,sablefish,rockfish,sole,
                 duration,distance,
                 top=title)
  )
  
}

filename<-"clamped.csv"
original <- read_csv(paste(full_directory,filename,sep="")) 
draw_dashboard(original,"Step 1 Calibration Error")


filename<-"eei.csv"
original <- read_csv(paste(full_directory,filename,sep=""))
draw_dashboard(original,"Step 2 Calibration Error - EEI")


#### CALIBRATION

#full_directory <- "/home/carrknight/code/oxfish/docs/20170730 validation/best-inference/20170822_dryrun/"
logbook_directory <- paste(full_directory,
                           "../logbook/",sep="")

#create helper to keep track of the name!
read_csv_filename <- function(filename){
  ret <- read_csv(filename)
  ret$Source <- filename #EDIT
  ret
}

summarise_with_error<-function(original,initialYear=1,finalYear=5,
                               north_quota=FALSE #sensitivity test, with north quota 
)
{
  sablefish_max_quota<- ifelse(north_quota,1606257,2724935)
  simulation<-
    original %>%
    filter(year>initialYear) %>%
    filter(year<=finalYear) %>%
    group_by(run) %>%
    summarise_at(vars(-starts_with("name")#,-"run"
    ),mean) %>%
    mutate(
      error = 
        abs(average_profits-118552)/21331 +
        abs(actual_hours_out-999.936)/120.382023907226 +
        abs(sole/22234500-0.3325)/.0309569593683445 +
        abs(sablefish/sablefish_max_quota-.836557879240702)/0.06181 +
        abs(short_thornyheads/1481600.056-0.525)/.0506622805119022 +
        abs(long_thornyheads/1966250-0.515)/.0506622805119022 +
        abs(rockfish/600-0.07)/.02 +
        abs(avg_duration-69.097625)/33 +
        abs(avg_distance-90.88762)/32
    )
  return(simulation)
}

# get all runs!


logbook_errors<-
  list.files(path=logbook_directory,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=full_directory) %>%
  dplyr::select(-path) %>%
  mutate(name=basename(name) ) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,".yaml","") ) %>%
  mutate(name=str_replace_all(name,"/","") )


logbook_errors<-
  logbook_errors %>%
  mutate(error = (abs((habit-0.212637158885711) / 0.0130763512467314)  
                  + abs((distance + 0.0210724717780951) / 0.00139610268440124) ))

logbook_errors$name <-
  factor(logbook_errors$name, levels=levels, 
         labels=labels_behaviour)


### DASHBOARD ERRORS
#full_directory<-"/home/carrknight/code/oxfish/docs/groundfish/"

dashboard_errors<-
  list.files(path=full_directory,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=full_directory) %>%
  dplyr::select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") ) 

dashboard_errors$name<-
  factor(dashboard_errors$name, levels=levels, 
         labels=labels_behaviour)

dashboard_errors<-(dashboard_errors %>% group_by(name) %>% do(summarise_with_error(.)))

dashboard_errors<-dashboard_errors


errors<-
  bind_rows("Logbook Error"=logbook_errors,
            "Outcome Error"=dashboard_errors,
            .id="type")  %>%
  mutate(decision_type=case_when(
    name %in% statistical_algorithms ~ "Statistical",
    name %in% adaptive_algorithms ~ "Adaptive",
    TRUE ~ "Other"
  ))

errors$type<-factor(errors$type,levels = c("Logbook Error","Outcome Error"))

cb_palette <- c("#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442",
                "#0072B2", "#D55E00", "#CC79A7","#000000")




ggplot(errors) +
  geom_boxplot(aes(x=name,y=error,fill=name)) +
  ylim(0,80) +
  facet_grid(~type, scales = "free_x") +
  scale_fill_discrete(guide=FALSE) +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  #geom_vline(xintercept=c(2.5,4.5),lty=2) +
  coord_flip() +
  ylab("Error") +
  xlab("Heuristic") +
  ggtitle("Calibration (in-sample) Errors") +
  theme_gray(20)


## additional cali

uncalibrated_profits<-
  list.files(path=full_directory,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=full_directory) %>%
  dplyr::select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") )  %>%
  filter(name != "kernel2") %>%
  filter(name != "truly_perfect")

uncalibrated_profits$name<-
  factor(uncalibrated_profits$name, levels=levels, 
         labels=labels_behaviour)

## profits

ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,average_profits,fill=name))  +
  ylim(x=0,y=200000) +
  geom_hline(yintercept=134405.5,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Average Profits per Boat per Year ($)") +
  xlab("Heuristic") + 
  theme_bw(20)
## average hours out
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,actual_hours_out,fill=name))  +
  ylim(x=0,y=1200) +
  geom_hline(yintercept=799.44,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Average Hours Out") +
  xlab("Heuristic") + 
  theme_bw(20)
## sole
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,sole/22234500,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=6717.13*1000/22234500,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Dover Sole Quota Attainment (%)") +
  xlab("Heuristic") + 
  theme_bw(20)
##       sablefish_error=abs(sablefish-1392.2001808742*1000)/2724935/0.06181  ,
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,sablefish/2724935,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=(1392.2001808742*1000)/2724935,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Sablefish Quota Attainment (%)") +
  xlab("Heuristic")

## long_thornheads_error =abs(long_thornyheads-713.991403686*1000)/1966250/.0506622805119022,
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,long_thornyheads/1966250,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=(713.991403686*1000)/1966250,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Long Thornyheads Quota Attainment (%)") +
  xlab("Heuristic")


## short_thornyheads_error = abs( short_thornyheads-734.20456815*1000 )/1481600.056/.050662280,
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,short_thornyheads/1481600.056,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=(734.20456815*1000)/1481600.056,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Short Thornyheads Quota Attainment (%)") +
  xlab("Heuristic")


ggplot(uncalibrated_profits %>% filter(year>5)) +
  geom_boxplot(aes(name,rockfish/600,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=0.07,lwd=2,lty=2,col="red") +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  ylab("Yelloweye Quota Attainment (%)") +
  xlab("Heuristic")  + 
  theme_bw(20)

## VALIDATION



dashboards<-
  list.files(path=full_directory,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=full_directory) %>%
  dplyr::select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") ) 

dashboards$name<-  factor(dashboards$name, levels=levels, 
                          labels=labels_behaviour)

summarise_with_error_final<-function(original,yearToFilter=c(6,7),
                                     north_quota=FALSE #sensitivity test, with north quota 
                                     )
{
  sablefish_max_quota<- ifelse(north_quota,1606257,2724935)
  # average profits:  118,023 (126,432)
  simulation<-
    original %>%
    filter(year %in% yearToFilter) %>%
    group_by(run) %>%
    summarise_at(vars(-starts_with("name")),mean) %>%
    mutate(
      profit_error=abs(average_profits-134405.5)/21331 ,
      hours_out_error=abs(actual_hours_out-799.44)/120.382023907226 ,
      sole_error=abs(sole-6717.13*1000)/22234500/.0309569593683445 ,
      sablefish_error=abs(sablefish-1392.2001808742*1000)/sablefish_max_quota/0.06181  ,
      long_thornheads_error =abs(long_thornyheads-713.991403686*1000)/1966250/.0506622805119022,
      short_thornyheads_error = abs( short_thornyheads-734.20456815*1000 )/1481600.056/.050662280,
      rockfish_error = abs(rockfish - 0.0421841*1000)/600/.02 
      
    ) %>%
    
    mutate(
      error = 
        profit_error+
        hours_out_error +
        sole_error +
        sablefish_error +
        long_thornheads_error +
        short_thornyheads_error +
        rockfish_error
    )
  return(simulation)
}
errors<-(dashboards %>% group_by(name) %>% do(summarise_with_error_final(.)))




ggplot(errors) +
  geom_boxplot(aes(x=name,y=error,fill=name)) + coord_flip() +
  scale_fill_discrete(guide=FALSE) + 
  ylab("Validation Error") +
  xlab("Heuristic") +
  ggtitle("2015-2016 Validation Error") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  theme_bw(20)


#this should probably go to a table in the appendix
errors %>% group_by(name) %>% summarise_at(vars(ends_with("error")),median)


## t-test


fm1 <- aov(error ~ name, data = errors)
TukeyHSD(fm1,ordered=FALSE,conf.level = 1-0.05/78) #all plots!

pairwise.t.test(
  errors %>% pull(error),
  errors %>% pull(name),
  p.adjust.method = "bonferroni",
  paired=FALSE
)

## ABC


## standard rejection

abc_ready<-errors %>% filter(year>5) %>%
  dplyr::select(average_profits,actual_hours_out,sole,sablefish,long_thornyheads,short_thornyheads,rockfish,name)
# dplyr::select(profit_error,hours_out_error,sole_error,sablefish_error,long_thornheads_error,short_thornyheads_error,rockfish_error,name)

library(abc)

# test<-cv4postpr(abc_ready %>% pull(name) %>% as.vector(), 
#           abc_ready %>% ungroup() %>% dplyr::select(-name) ,
#           nval=50,method="rejection",tol=.1)




target<-c(
  134405.5,799.44,6717.13*1000,1392*1000,713.99*1000,734.20*1000,0.0421841*1000
)
done<-postpr(target,index = abc_ready %>% pull(name) %>% as.vector(), 
             abc_ready %>% ungroup() %>% dplyr::select(-name),
             method="rejection",
             tol=.1)
done<-summary(done)



## abc RF

library(abcrf)

rffit<-abcrf::abcrf(name~.,data=abc_ready %>% ungroup())
names(target)<-
  c("average_profits","actual_hours_out","sole","sablefish","long_thornyheads","short_thornyheads","rockfish")
target %>% t() %>% as.data.frame()
predict(rffit,obs=target %>% t() %>% as.data.frame(),training= abc_ready %>% ungroup()) -> done2
votes<-done2$vote/sum(done2$vote)

# abc_ready %>% pull(name) %>% unique() %>% levels()

## elastic net
library(glmnetUtils)
ernesto<-
  glmnetUtils::cv.glmnet(
    formula=as.formula("name~."),
    data=abc_ready %>% ungroup(),
    family="multinomial"
  )

predict(ernesto,newdata=target %>% t() %>% as.data.frame(),type="response",s="lambda.1se") ->done3
coef(ernesto,s="lambda.1se")


rbind(
  
  done$Prob %>% t() %>% as.data.frame(),
  (done2$vote/sum(done2$vote)) %>% as_data_frame(),
  done3[,,1] %>% t() %>% as_data_frame()
) %>% mutate(method=c("ABC","RF","Elastic Nets")) %>% knitr::kable(digits=3)



### EARLIER START


sensitivity_directory<- paste(full_directory,"../pretopost/",sep="")

dashboards<-
  list.files(path=sensitivity_directory,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=sensitivity_directory) %>%
  select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"_withscript","") ) %>%
  mutate(name=str_replace_all(name,"/","") ) 

dashboards$name<-factor(dashboards$name, levels=levels, 
                        labels=labels_behaviour)
sensitivity_errors<-
  (dashboards %>% group_by(name) %>% do(summarise_with_error(.,initialYear = 4,finalYear = 9)))

sensitivity_validation_errors<-
  dashboards %>% group_by(name) %>%  do(summarise_with_error_final(.,yearToFilter = 10))


errors<-
  bind_rows("Calibration Error"=sensitivity_errors,
            "Validation Error"=sensitivity_validation_errors,
            .id="type")

errors$type<-factor(errors$type,levels = c("Calibration Error","Validation Error"))

ggplot(errors) +
  geom_boxplot(aes(x=name,y=error,fill=name)) +
  ylim(0,80) +
  facet_grid(~type, scales = "free_x") +
  scale_fill_discrete(guide=FALSE) +
  coord_flip() +
  ylab("Error") +
  xlab("Heuristic") +
  ggtitle("Errors - 2007 Starting Date")


### OTHER VALIDATION


dashboards<-
  list.files(path=full_directory,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=full_directory) %>%
  dplyr::select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") ) %>%
  filter(name == "bandit" | name == "kernel" | name == "eei"  | name == "default"   )

dashboards$name<-factor(dashboards$name, levels=levels, 
                        labels=labels_behaviour)

simulation<-
  dashboards %>%
  filter(year>1) %>%
  filter(year<=5) %>%
  group_by(name,run) %>%
  summarise_all(mean) %>%
  mutate(year = year + 2009) 



simulation_price<-
  simulation %>%
  dplyr::select(run,name,sable_price,yelloweye_price,short_price,long_price,doversole_price) %>%
  gather("Species","Quota Price",-name,-run) 

simulation_price$Species<-factor(simulation_price$Species,levels=c("sable_price",
                                                                   "yelloweye_price",
                                                                   "short_price",
                                                                   "long_price",
                                                                   "doversole_price"),
                                 labels = c("Sablefish","Yelloweye","Shortspine","Longspine","Dover Sole"))


simulation_price<-
  simulation_price %>%
  ungroup() %>%
  mutate(target = recode(Species,
                         `Sablefish`="2.1825764",
                         "Yelloweye"="66.69",
                         "Shortspine"="0.11",
                         "Longspine"="0.11",
                         "Dover Sole"="0.11" ))
simulation_price$target<-as.numeric(as.character(simulation_price$target))



ggplot(simulation_price) +
  geom_boxplot(aes(x=name,y=`Quota Price`,fill=name),col="black")  +
  expand_limits(x=0,y=0)+ expand_limits(x=4.3235295,y=0)+
  geom_hline(aes(yintercept=target),lwd=2,col="red",lty=2) +
  scale_fill_discrete(guide=FALSE) +
  facet_wrap(~Species,scales = "free_x",ncol=1) +
  ggtitle("Simulated Quota Prices")  +
  ylab("Quota Price($/kg)") +
  xlab("Decision-Making Algorithm") +
  coord_flip() 

ggplot(dashboards %>% filter(year>1)) +
  geom_line(aes(x=year+2009,y=actual_profits/(actual_hours_out/24),group=run,col=name)) +
  scale_color_discrete(guide=FALSE) +
  ggtitle("Simulated Profits Time Series") +
  ylab("Profits per day out ($ / day)") +
  xlab("Simulation Years") +
  facet_wrap(~name)


ggplot(dashboards %>% filter(year>1)) +
  geom_line(aes(x=year + 2009,y=as.integer(active_fishers),group=run,col=name)) +
  ggtitle("# of active fishers")+
  geom_hline(yintercept=47,lwd=2,lty=2,col="red") +
  facet_wrap(~name) +
  ylab("Active Fishers") +
  scale_color_discrete(guide=FALSE) +
  xlab("Year")


locations<-
  read.csv("~/code/oxfish/inputs/california/dts_ports_2010.csv")  %>%
  rename(port=Port)

end_ports<-
  read.csv("~/code/oxfish/inputs/california/dts_ports_2014.csv")  %>%
  rename(port=Port)


original<-
  dashboards %>%
  filter(name=="EEI")

# original<-read_csv("/home/carrknight/code/oxfish/docs/20170730 validation/policies/eei_toscale.csv")


port_profits_early<-
  original %>% 
  filter(year==2) %>% 
  dplyr::select(contains("_fishers")) %>% gather("port","fishers") %>%
  separate(port,c("port","name"),sep="_") %>%
  dplyr::select(-name) %>%
  mutate(port=str_replace_all(port,"Coo.s.Bay","Coo's Bay")) %>%
  mutate(port=str_replace_all(port,"\\."," ")) %>%
  mutate(port=as.factor(port)) 


port_profits_early<-
  inner_join(port_profits_early,locations,by="port")  %>%
  arrange(desc(Northings)) %>%
  mutate(port = reorder(port,Northings))

labels<-levels(port_profits_early$port)

labels[labels=="San Francisco"] = "San Francisco*"
labels[labels=="Monterey"] = "Monterey*"

plot1<-
  ggplot(port_profits_early)+
  geom_boxplot(aes(port,fishers,fill=port)) +
  coord_flip() +
  ylim(0,16)+
  scale_fill_discrete(guide=FALSE) +
  ylab("# of Active Fishers") +
  scale_x_discrete(labels=labels) +
  xlab("Port") +
  annotate('text',x=1.5,y=10,label="* Catcher-Vessel Report quotes\n'<3' boats in SF and Monterey\nwe use 2 as a default value",col="black") 
#expand_limits(x=2,y=5)

port_profits_late<-
  original %>% 
  filter(year==5) %>% 
  select(contains("_fishers")) %>% gather("port","fishers") %>%
  separate(port,c("port","name"),sep="_") %>%
  select(-name) %>%
  filter(port!="average") %>%
  filter(port!="actual") %>%
  filter(port!="active") %>%
  mutate(port=str_replace_all(port,"Coo.s.Bay","Coo's Bay")) %>%
  mutate(port=str_replace_all(port,"\\."," ")) %>%
  mutate(port=as.factor(port)) 

port_profits_late<-
  left_join(port_profits_late,locations,by="port")  %>%
  arrange(desc(Northings)) %>%
  mutate(port = reorder(port,Northings))

plot2<-ggplot(port_profits_late)+
  geom_boxplot(aes(port,fishers,fill=port)) +
  coord_flip() +
  ylim(0,16)+
  scale_fill_discrete(guide=FALSE) +
  geom_point(data=end_ports,aes(x=port,y=Fishers),col="red",size=3.5,shape=15) +
  ylab("# of Active Fishers") +
  scale_x_discrete(labels=labels) +
  xlab("Port") +
  annotate('text',x=1.5,y=10,label="* Catcher-Vessel Report quotes\n'<3' boats in SF and Monterey\nwe use 2 as a default value",col="black") +
  xlab("Port")

grid.arrange(plot1 + 
               ggtitle("Initial Active Fishers"),
             plot2 +
               ggtitle("Active Fishers in year 2014"))


sensitivity_directory<- paste(full_directory,"../pretopost/",sep="")

## ANT 1
original <- read_csv(paste(full_directory,"clamped.csv",sep=""))
ant <- read_csv(paste(full_directory,"../../ants/ant_historical/ant_worse.csv",sep=""))
original<-summarise_with_error(original)
ant<-summarise_with_error(ant)
original$name<-"Calibrated"
ant$name<-"Active Nonlinear Test"

test<-bind_rows(original,ant)
ggplot(test) +
  geom_density(aes(error,fill=name),col="black") +
  facet_grid(name~.) +
  xlab("Outcome Error") +
  scale_fill_discrete(guide=FALSE) +
  ylab("Frequency") +
  ggtitle("Step 1 Sensitivity")


#wilcox.test(original$error,ant$error)

## ANT 2

original <- read_csv(paste(full_directory,"eei.csv",sep=""))
ant <- read_csv(paste(full_directory,"../../ants/ant_eei/ant_worse.csv",sep=""))
original<-summarise_with_error(original)
ant<-summarise_with_error(ant)
original$name<-"Calibrated"
ant$name<-"Active Nonlinear Test"

test<-bind_rows(original,ant)
ggplot(test) +
  geom_density(aes(error,fill=name),col="black") +
  facet_grid(name~.) +
  xlab("Outcome Error") +
  scale_fill_discrete(guide=FALSE) +
  ylab("Frequency") +
  ggtitle("Explore-Exploit-Imitate, Total Sensitivity")

#wilcox.test(original$error,ant$error)
#abs(mean(original$error)-mean(ant$error))/mean(original$error)


## ANT 3

original <- read_csv(paste(full_directory,"kernel.csv",sep=""))
ant <- read_csv(paste(full_directory,"ant/kernel-ant.csv",sep=""))
original<-summarise_with_error(original)
ant<-summarise_with_error(ant)
original$name<-"Calibrated"
ant$name<-"Active Nonlinear Test"

test<-bind_rows(original,ant)
ggplot(test) +
  geom_density(aes(error,fill=name),col="black") +
  facet_grid(name~.) +
  xlab("Outcome Error") +
  scale_fill_discrete(guide=FALSE) +
  ylab("Frequency") +
  ggtitle("Heat-mapping Agent, Total Sensitivity")

#wilcox.test(original$error,ant$error)
#abs(mean(original$error)-mean(ant$error))/mean(original$error)


### CPUE Map

directory_path <- paste(full_directory,sep = "")


dashboard_errors<-
  list.files(path=directory_path,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=directory_path) %>%
  select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") ) %>%
  filter(name != "kernel2") %>%
  filter(name != "truly_perfect")

dashboard_errors$name<-
  factor(dashboard_errors$name, levels=levels, 
         labels=labels_behaviour)

dashboard_errors<-(dashboard_errors %>% group_by(name) %>% do(summarise_with_error(.)))


directory_path <- paste(full_directory,"../map/",sep = "")


sensitivity_errors<-
  list.files(path=directory_path,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=directory_path) %>%
  select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"_withscript","") ) %>%
  mutate(name=str_replace_all(name,"/","") ) %>%
  filter(name!="mpaed_eei_log_350_preitq") %>%
  filter(name!="mpaed2_150_120blocked_clamped_preitq_old") %>%
  filter(name!="mpaed2_150_120blocked_intercepts_preitq" )  %>%
  filter(name!="backup_kernel" )  %>%
  filter(name != "kernel2") %>%
  filter(name != "truly_perfect")


sensitivity_errors$name<-
  factor(sensitivity_errors$name, levels=levels, 
         labels=labels_behaviour)

sensitivity_errors<-
  (sensitivity_errors %>% group_by(name) %>% do(summarise_with_error_final(.,)))


errors<-
  bind_rows(
    "Fishing Outcome Error"=dashboard_errors,
    "Fishing Outcome Error - 2007 Start" = sensitivity_errors,
    .id="type")

cb_palette <- c("#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442",
                "#0072B2", "#D55E00", "#CC79A7","#000000")

ggplot(errors) +
  geom_boxplot(aes(x=name,y=error,fill=name)) +
  #facet_grid(~type, scales = "free_y") +
  scale_fill_discrete(guide=FALSE) +
  coord_flip() +
  ylab("Error") +
  xlab("Algorithm") +
  ggtitle("Distance from data with CPUE map")

uncalibrated_profits<-
  list.files(path=directory_path,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=directory_path) %>%
  dplyr::select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") )  %>%
  filter(name != "kernel2") %>%
  filter(name != "truly_perfect")

uncalibrated_profits$name<-
  factor(uncalibrated_profits$name, levels=levels, 
         labels=labels_behaviour)

ggplot(uncalibrated_profits %>% filter(year>=5)) +
  geom_boxplot(aes(name,average_profits,fill=name))  +
  ylim(x=0,y=200000) +
  geom_hline(yintercept=134405.5,lwd=2,lty=2,col="red") +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Average Profits per Boat per Year ($)") +
  xlab("Algorithm")


#cpue shortandsable
uncalibrated_profits<-
  list.files(path=directory_path,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=directory_path) %>%
  select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") )

# uncalibrated_profits$name<-
#   factor(uncalibrated_profits$name, levels=levels, 
#          labels=labels_behaviour)

uncalibrated_profits<-
  uncalibrated_profits %>%
  filter(name %in% c("eei","kernel","perfect","default","eei2")) %>%
  mutate(map="CPUE")

uncalibrated_profits$name<-
  factor(uncalibrated_profits$name, levels=levels, 
         labels=labels_behaviour)

calibrated_profits<-
  list.files(path=full_directory,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=full_directory) %>%
  dplyr::select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") )  %>%
  filter(name != "kernel2") %>%
  filter(name != "truly_perfect")

calibrated<-
  calibrated_profits %>%
  filter(name %in% c("eei","kernel","perfect","default","eei2")) %>%
  dplyr::mutate(map="EFH")

calibrated$name<-
  factor(calibrated$name, levels=levels, 
         labels=labels_behaviour)

shortspine_prices<-
  bind_rows(calibrated,uncalibrated_profits) %>%
  group_by(run,map,name) %>%
  filter(year>1)  %>%
  mutate(short_price = ifelse(is.na(short_price),0,short_price)) %>%
  summarise(short_price=mean((short_price)))

ggplot(shortspine_prices ) +
  geom_boxplot(aes(name,short_price,fill=name))  +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  facet_grid( map~. ) +
  geom_hline(yintercept=0.05,lwd=2,col="red",lty=2) +
 # geom_hline(yintercept=1.0428510,lty=2) +
  ylab("Shortspine quota price ($/kg)") + 
  xlab("Algorithms")

### KING OF THE NORTH


### DASHBOARD ERRORS
directory_path <- paste(full_directory,"../northquota/",sep = "")


dashboard_errors<-
  list.files(path=directory_path,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=directory_path) %>%
  select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") ) %>%
  filter(name != "kernel2") %>%
  filter(name != "truly_perfect")

dashboard_errors$name<-
  factor(dashboard_errors$name, levels=levels, 
         labels=labels_behaviour)


validation_errors<-(dashboard_errors %>% group_by(name) %>% do(summarise_with_error_final(.,
                                                                                          north_quota = TRUE)))
dashboard_errors<-(dashboard_errors %>% group_by(name) %>% do(summarise_with_error(.,
                                                                                   north_quota = TRUE)))


errors<-
  bind_rows(
    "Calibration Error"=dashboard_errors,
    "Validation Error" = validation_errors,
    .id="type")

ggplot(errors) +
  geom_boxplot(aes(x=name,y=error,fill=name)) +
  ylim(0,80) +
  facet_grid(~type) +
  scale_fill_discrete(guide=FALSE) +
  coord_flip() +
  ylab("Error") +
  xlab("Algorithm") +
  ggtitle("Distance from data - North Quota Only")


### king of the north quota

## additional cali

uncalibrated_profits<-
  list.files(path=directory_path,
             pattern="*.csv",
             full.names = TRUE) %>%
  map_df(~read_csv_filename(.)) %>%
  separate(Source,c("path","name"),
           sep=directory_path) %>%
  dplyr::select(-path) %>%
  mutate(name=str_replace_all(name,".csv","") ) %>%
  mutate(name=str_replace_all(name,"/","") )  %>%
  filter(name != "kernel2") %>%
  filter(name != "truly_perfect")

uncalibrated_profits$name<-
  factor(uncalibrated_profits$name, levels=levels, 
         labels=labels_behaviour)

## profits

ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,average_profits,fill=name))  +
  ylim(x=0,y=200000) +
  geom_hline(yintercept=134405.5,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Average Profits per Boat per Year ($)") +
  xlab("Heuristic") + 
  theme_bw(20) +
  ggtitle("North Quota Error")
## average hours out
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,actual_hours_out,fill=name))  +
  ylim(x=0,y=1200) +
  geom_hline(yintercept=799.44,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Average Hours Out") +
  xlab("Heuristic") + 
  theme_bw(20)+
  ggtitle("North Quota Error")
## sole
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,sole/22234500,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=6717.13*1000/22234500,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Dover Sole Quota Attainment (%)") +
  xlab("Heuristic") + 
  theme_bw(20) +
  ggtitle("North Quota Error")
##       sablefish_error=abs(sablefish-1392.2001808742*1000)/2724935/0.06181  ,
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,sablefish/1606257,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=(1392.2001808742*1000)/1606257,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Sablefish Quota Attainment (%)") +
  xlab("Heuristic") +
  ggtitle("North Quota Error")

## long_thornheads_error =abs(long_thornyheads-713.991403686*1000)/1966250/.0506622805119022,
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,long_thornyheads/1966250,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=(713.991403686*1000)/1966250,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Long Thornyheads Quota Attainment (%)") +
  xlab("Heuristic")


## short_thornyheads_error = abs( short_thornyheads-734.20456815*1000 )/1481600.056/.050662280,
ggplot(uncalibrated_profits %>% filter(year>5) %>% filter(year<=7)) +
  geom_boxplot(aes(name,short_thornyheads/1481600.056,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=(734.20456815*1000)/1481600.056,lwd=2,lty=2,col="red") +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  ylab("Short Thornyheads Quota Attainment (%)") +
  xlab("Heuristic")


ggplot(uncalibrated_profits %>% filter(year>5)) +
  geom_boxplot(aes(name,rockfish/600,fill=name))  +
  ylim(x=0,y=1) +
  geom_hline(yintercept=0.07,lwd=2,lty=2,col="red") +
  coord_flip() +
  scale_fill_discrete(guide=FALSE) +
  annotate("rect",xmin=0,xmax=5.5,ymin=-Inf,ymax=Inf,fill="red",lty=2,alpha=.1) +
  annotate("rect",xmin=5.5,xmax=8.5,ymin=-Inf,ymax=Inf,fill="green",lty=2,alpha=.1) +
  annotate("rect",xmin=8.5,xmax=Inf,ymin=-Inf,ymax=Inf,fill="blue",lty=2,alpha=.1) +
  ylab("Yelloweye Quota Attainment (%)") +
  xlab("Heuristic")  + 
  theme_bw(20)






draw_dashboard<-function(original,title,targets=target,
                         north_quota=TRUE)
{
  simulation<-
    original %>%
    filter(year>1) %>%
    group_by(run) %>%
    summarise_all(mean)
  
  # simulation$sablefish/2724935
  # 
  # simulation$hours_out / simulation$trips
  # simulation$avg_duration
  
  profits<-
    ggplot(simulation) +
    geom_histogram(aes(average_profits)) +
    geom_rect(aes(xmin=targets[1]-2*21331,xmax=targets[1]+2*21331,ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=targets[1]-3*21331,y=0) +
    expand_limits(x=targets[1]+3*21331,y=0) +
    geom_vline(xintercept=targets[1],lwd=2,col="red",lty=2) +
    ggtitle("Average Profits")
  #profits
  
  
  
  
  
  hours_out<-
    ggplot(simulation) +
    #  geom_histogram(aes(hours_out/24)) +
    geom_rect(aes(xmin=(targets[2]-2*120)/24,
                  xmax=(targets[2]+2*120)/24,ymin=0,ymax=+Inf),
              alpha=.1,fill="blue",data=simulation[1,]) +
    geom_histogram(aes(actual_hours_out/24)) +
    expand_limits(x=(targets[2]-3*120)/24,y=0) +
    expand_limits(x=(targets[2]+3*120)/24,y=0) +
    geom_vline(xintercept=(targets[2])/24,lwd=2,col="red",lty=2) +
    ggtitle("Days Out") +
    scale_x_continuous() +
    xlab("Average Days Spent Fishing")
  #hours_out
  
  
  
  sole<-
    ggplot(simulation) +
    geom_rect(aes(xmin=targets[3]/22234500-2*.0309,
                  xmax=targets[3]/22234500+2*.0309,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    geom_histogram(aes(sole/22234500)) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=targets[3]/22234500,lwd=2,col="red",lty=2) +
    ggtitle("Dover Sole Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  #sole
  
  targets[4]<-ifelse(north_quota,targets[4]/1606257,targets[4]/2724935)
  #sablefish
  sablefish<-
    ggplot(simulation) +
    geom_histogram(aes(ifelse(north_quota,sablefish/1606257,sablefish/2724935))) +
    geom_rect(aes(xmin=targets[4]-2*0.061,
                  xmax=targets[4]+2*0.061,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=targets[4],lwd=2,col="red",lty=2) +
    ggtitle("Sablefish Attainment") +
    xlab("% of Yearly Quota Landed") +
    
    scale_x_continuous(labels = scales::percent)
  #sablefish
  
  targets[5]<-targets[5]/1481600.056
  short_thornyheads<-
    ggplot(simulation) +
    geom_histogram(aes(short_thornyheads/1481600.056)) +
    geom_rect(aes(xmin=targets[5]-2*.0506,
                  xmax=targets[5]+2*.0506,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=targets[5],lwd=2,col="red",lty=2) +
    ggtitle("Shortspine Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  
  
  
  #long_thornyheads
  targets[6]<-targets[6]/1966250.0
  
  long_thornyheads<-
    ggplot(simulation) +
    geom_histogram(aes(long_thornyheads/1966250.0)) +
    geom_rect(aes(xmin=targets[6]-2*.0506,
                  xmax=targets[6]+2*.0506,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=targets[6],lwd=2,col="red",lty=2) +
    ggtitle("Longspine Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  #long_thornyheads
  
  targets[7]<-targets[7]/600
  rockfish<-
    ggplot(simulation) +
    geom_histogram(aes(rockfish/600)) +
    geom_rect(aes(xmin=targets[7]-2*.02,
                  xmax=targets[7]+2*.02,
                  ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
    expand_limits(x=0,y=0) + expand_limits(x=1,y=1) +
    geom_vline(xintercept=targets[7],lwd=2,col="red",lty=2) +
    ggtitle("Yelloweye Attainment") +
    xlab("% of Yearly Quota Landed") +
    scale_x_continuous(labels = scales::percent)
  #rockfish
  
  # don't have these for out of sample!!!!
  # ##Average duration
  # duration<-ggplot(simulation) +
  #   geom_histogram(aes(x=avg_duration)) +
  #   geom_rect(aes(xmin=69.097625-2*33,
  #                 xmax=69.097625+2*33,
  #                 ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
  #   expand_limits(x=0,y=0)+
  #   geom_vline(xintercept=69.097625,lwd=2,col="red",lty=2) +
  #   
  #   # expand_limits(x=69.097625-3*33,y=0) +
  #   expand_limits(x=69.097625+3*33,y=0) +
  #   ggtitle("Trip's duration")  +
  #   xlab("Duration(hr)")
  # 
  # 
  # 
  # ## Average distance
  # distance<-ggplot(simulation) +
  #   #geom_histogram(aes(x=weighted_distance)) +
  #   geom_histogram(aes(x=avg_distance)) +
  #   expand_limits(x=0,y=0)+
  #   geom_vline(xintercept=90,lwd=2,col="red",lty=2) +
  #   expand_limits(x=90.88762+3*33,y=0) +
  #   ggtitle("Trip's distance from port")  +
  #   geom_rect(aes(xmin=90.88762-2*32,
  #                 xmax=90.88762+2*32,
  #                 ymin=0,ymax=+Inf),alpha=.1,fill="blue",data=simulation[1,]) +
  #   xlab("Distance(km)")
  # 
  # 
  
  #error
  library(gridExtra)
  return(
    grid.arrange(profits,hours_out,short_thornyheads,long_thornyheads,sablefish,rockfish,sole,
                # duration,distance,
                 top=title)
  )
  
}

filename<-"eei.csv"
original <- read_csv(paste(directory_path,filename,sep="")) 
draw_dashboard(original %>% filter(year>5),"EEI - Validation Error")



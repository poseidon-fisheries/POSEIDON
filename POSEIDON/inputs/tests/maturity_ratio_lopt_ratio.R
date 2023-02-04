library(tidyverse)

lengths<-
  c(45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,75,81)

number<-
  c(1,1,3,2,8,15,22,20,38,37,52,61,69,69,67,73,82,66,69,58,38,49,36,20,12,16,7,5,2,1,1)

VAR_A<-0.02
VAR_B<-2.94
L_INF<-81
VBK<-0.4946723



fake_population<-
  data.frame(
  lengths,
  number
) 

### here we get 0.995
fake_population %>%
  mutate(is_mature = lengths >=48) %>%
  group_by(is_mature) %>%
  summarise(tot_number=sum(number)) %>%
  mutate(percentage=tot_number/sum(tot_number))

### here we get 
fake_population %>%
  ##linf has since changed a bit which moved lopt to 67
  ##but here we are using old data!
  mutate(is_mature = lengths >=64) %>%
  group_by(is_mature) %>%
  summarise(tot_number=sum(number)) %>%
  mutate(percentage=tot_number/sum(tot_number))

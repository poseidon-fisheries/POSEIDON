#!/bin/bash          

echo $1 $2

#draw the dashboard (and copy it to the website)
Rscript ./runs/dashboards/dashboard.R $1 $2 > rscript_log.txt

#copy test reports to folder as well
reportDirectory="$2/assets/oxfish/reports/$1/"
echo $reportDirectory
cp -rf ./build/reports/tests/  $reportDirectory

#append to masterlist
echo "$1" >> $2/assets/oxfish/dashboards/dashboards.txt




#add to git
cd $2
git add $2/assets/oxfish/*
git commit -m "added dashboard $1"
git push



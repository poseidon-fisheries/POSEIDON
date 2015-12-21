#!/usr/bin/env bash

### Basically run the tests many times and output the csv file each time. Then hopefully R will take care of it
## and show the % of failure for each test
for i in {1..100};
do
    gradle cleanTest test
    gradle testCSV
done

cd ./runs/dashboards/
Rscript -e "rmarkdown::render('multitest.Rmd')"
cp ./multitest.html ./../../multitest.html

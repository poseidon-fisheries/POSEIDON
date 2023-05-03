import os
import shutil
import subprocess

r_command = 'Rscript'
gradle_command = 'gradle'


def main():
    for i in range(100):
        subprocess.call([gradle_command, "cleanTest", "test"])
        subprocess.call([gradle_command, "testCSV"])

    os.chdir("./runs/dashboards/")
    subprocess.call([r_command, "-e" "rmarkdown::render('multitest.Rmd')"])
    shutil.copyfile("./multitest.html","./../../multitest.html")

if __name__ == '__main__':
    main()

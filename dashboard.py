import os
import shutil
import subprocess
import sys

# this is the way you start the RScript; if you are on windows you need to change it to .exe and perhaps give it the full path
r_command = 'Rscript'
git_command = 'git'

def main():
    assert len(sys.argv) == 3
    print sys.argv[0] + "," + sys.argv[1]

    # run the R script
    subprocess.call([r_command, "./runs/dashboards/dashboard.R",
                     sys.argv[1], sys.argv[2]])

    # copy paste
    assert os.path.isdir("./build/reports/tests/")
    reportDirectory = sys.argv[2] + "/assets/oxfish/reports/" + sys.argv[1] + "/"
    shutil.copytree("./build/reports/tests/", reportDirectory)

    with open(sys.argv[2] + "/assets/oxfish/dashboards/dashboards.txt", "a") as masterlist:
        masterlist.write(sys.argv[1] + "\n")

    os.chdir(sys.argv[2])
    subprocess.call([git_command,"add","assets/oxfish/*"])
    subprocess.call([git_command,"commit","-m",' "added dashboard ' + sys.argv[1] + ' "'])
    subprocess.call([git_command,"push"])


if __name__ == "__main__":
    main()

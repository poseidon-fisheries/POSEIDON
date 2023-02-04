/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.experiments;

import com.google.common.io.PatternFilenameFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by carrknight on 9/12/16.
 */
public class RenameJob {

    public static void main(String[] args){

        Path mainDirectory = Paths.get("runs", "social_tuning");

        File[] files = mainDirectory.toFile().listFiles(new PatternFilenameFilter(".+plan.csv"));
        System.out.println(Arrays.toString(files));
        for(File file : files)
        {
            System.out.println(file.getName());
            String newName = file.getName().replace("plan", "");
            String[] components = newName.split("_");
            assert components.length==2;
            newName = components[0]+"-plan_" + components[1];
            Path newFile = mainDirectory.resolve(newName);
            System.out.println(newFile.getFileName());
            file.renameTo(newFile.toFile());
        }


    }
}

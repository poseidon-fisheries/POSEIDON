package uk.ac.ox.oxfish.regulations;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.utility.BasicFactorySupplier;
import uk.ac.ox.oxfish.utility.FactorySupplier;

@AutoService(FactorySupplier.class)
public class ForbiddenAreasFromShapeFilesFactorySupplier
    extends BasicFactorySupplier<ForbiddenAreasFromShapeFiles> {
    public ForbiddenAreasFromShapeFilesFactorySupplier() {
        super(ForbiddenAreasFromShapeFiles.class);
    }
}

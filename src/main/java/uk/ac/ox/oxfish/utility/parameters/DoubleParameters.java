package uk.ac.ox.oxfish.utility.parameters;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Just a collection of the doubleParameters that are available; useful for gui and instantiation
 * Created by carrknight on 6/7/15.
 */
public class DoubleParameters
{

    public static final Map<String,Supplier<DoubleParameter>> DOUBLE_PARAMETERS;
    public static final Map<Class<? extends DoubleParameter>,String> DOUBLE_PARAMETERS_NAME;
    static {
        final HashMap<String, Supplier<DoubleParameter>> parameters = new HashMap<>();
        final HashMap<Class<? extends DoubleParameter>,String> names = new HashMap<>();
        parameters.put("Fixed", () -> new FixedDoubleParameter(0));
        names.put(FixedDoubleParameter.class,"Fixed");
        parameters.put("Normal", () -> new NormalDoubleParameter(0,1));
        names.put(NormalDoubleParameter.class,"Normal");
        parameters.put("Uniform", () -> new UniformDoubleParameter(0,1));
        names.put(UniformDoubleParameter.class,"Uniform");
        parameters.put("Select", () -> new SelectDoubleParameter("0 1"));
        names.put(SelectDoubleParameter.class,"Select");
        parameters.put("Sin", () -> new SinusoidalDoubleParameter(1,0.01));
        names.put(SelectDoubleParameter.class,"Sin");

        parameters.put("Conditional", () -> new ConditionalDoubleParameter(false,new FixedDoubleParameter(0)));
        names.put(ConditionalDoubleParameter.class,"Conditional");



        DOUBLE_PARAMETERS = Collections.unmodifiableMap(parameters);
        DOUBLE_PARAMETERS_NAME = Collections.unmodifiableMap(names);

    }



}

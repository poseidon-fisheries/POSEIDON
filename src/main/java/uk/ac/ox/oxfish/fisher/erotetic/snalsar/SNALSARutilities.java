package uk.ac.ox.oxfish.fisher.erotetic.snalsar;

/**
 * Created by carrknight on 5/26/16.
 */
public class SNALSARutilities
{

    private SNALSARutilities() {}

    /**
     * whether a choice is safe or not.
     */
    public static String SAFE_FEATURE = "Safe Feature";

    /**
     * Expected (?) profits at location
     */
    public static final String PROFIT_FEATURE = "Profit Feature";

    /**
     *  whether it is legal or not to fish here. By default as long as it is above 0 it is considered legal
     */
    public static final String LEGAL_FEATURE = "Legal Feature";


    /**
     * whether it is socially appropriate or not to fish here. By default as long as it is above 0 it is considered appropriate
     */
    public static final String SOCIALLY_APPROPRIATE_FEATURE = "Socially Appropriate Feature";



    /**
     * threshold below which profits are usually considered a failure
     */
    public static final String FAILURE_THRESHOLD = "Non-Failure Threshold Feature";



    /**
     * threshold above which profits are considered acceptable
     */
    public static final String ACCEPTABLE_THRESHOLD = "Acceptable Threshold Feature";




}

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.collect.ForwardingMap;
import uk.ac.ox.oxfish.biology.Species;

import java.util.Map;

public class RecruitmentProcesses extends ForwardingMap<Species, RecruitmentProcess> {

    private final Map<Species, RecruitmentProcess> delegate;

    public RecruitmentProcesses(final Map<Species, RecruitmentProcess> delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Map<Species, RecruitmentProcess> delegate() {
        return delegate;
    }
}

#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};

#end
#parse("File Header.java")

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ${NAME}Factory extends GlobalScopeFactory<${NAME}> {
    @Override
    protected ${NAME} newInstance(final Simulation simulation) {
        return new ${NAME}();
    }
}
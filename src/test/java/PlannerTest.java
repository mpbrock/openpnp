import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openpnp.model.Configuration;
import org.openpnp.model.Job;
import org.openpnp.spi.JobProcessor;
import org.openpnp.spi.Machine;

import com.google.common.io.Files;

public class PlannerTest {
    @Test
    public void testPlannerJob() throws Exception {
        File workingDirectory = Files.createTempDir();
        workingDirectory = new File(workingDirectory, ".openpnp");
        System.out.println("Configuration directory: " + workingDirectory);

        // Copy the required configuration files over to the new configuration
        // directory.
        FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/PlannerTest/machine.xml"),
                new File(workingDirectory, "machine.xml"));
        FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/PlannerTest/packages.xml"),
                new File(workingDirectory, "packages.xml"));
        FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/PlannerTest/parts.xml"),
                new File(workingDirectory, "parts.xml"));

        // And the job files
        FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/PlannerTest/planner.board.xml"),
                new File(workingDirectory, "planner.board.xml"));
        FileUtils.copyURLToFile(ClassLoader.getSystemResource("config/PlannerTest/planner.job.xml"),
                new File(workingDirectory, "planner.job.xml"));
        Configuration.initialize(workingDirectory);
        Configuration.get().load();

        Machine machine = Configuration.get().getMachine();

        Job job = Configuration.get().loadJob(new File(workingDirectory, "planner.job.xml"));

        JobProcessor jobProcessor = machine.getPnpJobProcessor();
        machine.setEnabled(true);
        jobProcessor.initialize(job);
        while (jobProcessor.next());
    }
}

package org.codehaus.modello.maven;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.build.BuildContext;
import org.codehaus.plexus.testing.PlexusTest;
import org.codehaus.plexus.testing.PlexusTestConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PlexusTest
public class AbstractModelloSourceGeneratorMojoTest implements PlexusTestConfiguration {

    @Inject
    private BuildContext buildContext;

    @Inject
    private ModelloCore modelloCore;

    private static class ModelloSourceGeneratorMojoTest extends AbstractModelloSourceGeneratorMojo {

        private final Properties projectProperties;

        ModelloSourceGeneratorMojoTest(
                BuildContext buildContext, ModelloCore modelloCore, Properties projectProperties) {
            super(buildContext, modelloCore);
            this.projectProperties = projectProperties;
        }

        @Override
        protected String getGeneratorType() {
            return null;
        }

        @Override
        public MavenProject getProject() {
            Model model = new Model();
            model.setProperties(projectProperties);
            MavenProject project = new MavenProject();
            project.setModel(model);
            return project;
        }
    }

    @Override
    public void customizeConfiguration(ContainerConfiguration containerConfiguration) {
        containerConfiguration.setClassPathScanning("cache");
    }

    private void executeJavaSourceTest(Properties projectProperties, String expexted) {
        ModelloSourceGeneratorMojoTest modelloSourceGeneratorMojoTest =
                new ModelloSourceGeneratorMojoTest(buildContext, modelloCore, projectProperties);
        Map<String, Object> properties = new HashMap<>();

        modelloSourceGeneratorMojoTest.customizeParameters(properties);

        assertEquals(properties.get(ModelloParameterConstants.OUTPUT_JAVA_SOURCE), expexted);
    }

    @Test
    public void testJavaSourceDefault() {
        executeJavaSourceTest(new Properties(), ModelloParameterConstants.OUTPUT_JAVA_SOURCE_DEFAULT);
    }

    @Test
    public void testJavaSourceFromRelease() {
        Properties projectProperties = new Properties();
        projectProperties.setProperty("maven.compiler.release", "11");
        projectProperties.setProperty("maven.compiler.source", "xxx");
        projectProperties.setProperty("maven.compiler.target", "xxx");

        executeJavaSourceTest(projectProperties, "11");
    }

    @Test
    public void testJavaSourceFromSource() {
        Properties projectProperties = new Properties();
        projectProperties.setProperty("maven.compiler.source", "11");
        projectProperties.setProperty("maven.compiler.target", "xxx");

        executeJavaSourceTest(projectProperties, "11");
    }

    @Test
    public void testJavaSourceFromTarget() {
        Properties projectProperties = new Properties();
        projectProperties.setProperty("maven.compiler.target", "11");

        executeJavaSourceTest(projectProperties, "11");
    }
}

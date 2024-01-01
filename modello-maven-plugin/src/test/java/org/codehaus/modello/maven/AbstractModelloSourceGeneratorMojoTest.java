package org.codehaus.modello.maven;

import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.codehaus.modello.ModelloParameterConstants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractModelloSourceGeneratorMojoTest {

    private class ModelloSourceGeneratorMojoTest extends AbstractModelloSourceGeneratorMojo {

        private final Properties projectProperties;

        ModelloSourceGeneratorMojoTest(Properties projectProperties) {
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

    private void executeJavaSourceTest(Properties projectProperties, String expexted) {
        ModelloSourceGeneratorMojoTest modelloSourceGeneratorMojoTest =
                new ModelloSourceGeneratorMojoTest(projectProperties);
        Properties properties = new Properties();

        modelloSourceGeneratorMojoTest.customizeParameters(properties);

        assertEquals(properties.getProperty(ModelloParameterConstants.OUTPUT_JAVA_SOURCE), expexted);
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

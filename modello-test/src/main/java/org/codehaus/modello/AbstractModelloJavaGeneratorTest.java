package org.codehaus.modello;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.modello.verifier.VerifierException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.codehaus.plexus.testing.PlexusExtension.getTestPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Base class for unit-tests of Modello plugins that generate java code.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @see #compileGeneratedSources() compileGeneratedSources() method to compile generated sources
 * @see #verifyCompiledGeneratedSources(String) verifyCompiledGeneratedSources(String) method to run a Verifier
 *      class against compiled generated code
 * @see org.codehaus.modello.verifier.Verifier Verifier base class for verifiers
 */
public abstract class AbstractModelloJavaGeneratorTest extends AbstractModelloGeneratorTest {
    private List<URL> urls = new ArrayList<URL>();

    private List<String> classPathElements = new ArrayList<String>();

    protected AbstractModelloJavaGeneratorTest(String name) {
        super(name);
    }

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        FileUtils.deleteDirectory(getOutputClasses());

        assertTrue(getOutputClasses().mkdirs());
    }

    protected File getOutputDirectory() {
        return new File(super.getOutputDirectory(), "sources");
    }

    protected File getOutputClasses() {
        return new File(super.getOutputDirectory(), "classes");
    }

    protected void compileGeneratedSources() throws IOException {
        compileGeneratedSources(getName(), 8);
    }

    protected void compileGeneratedSources(int minJavaSource) throws IOException {
        compileGeneratedSources(getName(), minJavaSource);
    }

    protected void compileGeneratedSources(String verifierId, int minJavaSource) throws IOException {
        String runtimeVersion = System.getProperty("java.specification.version");
        if (runtimeVersion.startsWith("1.")) {
            runtimeVersion = runtimeVersion.substring(2);
        }
        int runtimeSource = Integer.parseInt(runtimeVersion);

        String javaSource;
        // review when Java will drop support for Java 8 as source
        if (runtimeSource <= 21) {
            javaSource = Integer.toString(Math.max(minJavaSource, 8));
        } else {
            javaSource = Integer.toString(Math.max(minJavaSource, 8));
        }

        compileGeneratedSources(verifierId, javaSource);
    }

    private void compileGeneratedSources(String verifierId, String javaSource) throws IOException {
        File generatedSources = getOutputDirectory();
        File destinationDirectory = getOutputClasses();

        List<String> classPath = new ArrayList<>();
        classPath.add(getTestPath("target/classes"));
        classPath.add(getTestPath("target/test-classes"));
        classPath.addAll(resolveTestClasspath());

        List<File> sourceDirectories = new ArrayList<>();
        File verifierDirectory = getTestFile("src/test/verifiers/" + verifierId);
        if (verifierDirectory.canRead()) {
            sourceDirectories.add(verifierDirectory);
        }
        sourceDirectories.add(generatedSources);

        List<File> sourceFiles = new ArrayList<>();
        for (File sourceDirectory : sourceDirectories) {
            sourceFiles.addAll(findJavaSources(sourceDirectory));
        }

        // javac up to Java 8 refuses a -d that does not already exist; later versions create it
        Files.createDirectories(destinationDirectory.toPath());

        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        assertNotNull(javac, "No java compiler available - the tests need a JDK, not a JRE");

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = javac.getStandardFileManager(diagnostics, null, null)) {
            List<String> options = Arrays.asList(
                    "-g",
                    "-classpath",
                    String.join(File.pathSeparator, classPath),
                    "-d",
                    destinationDirectory.getAbsolutePath(),
                    "-source",
                    javaSource,
                    "-target",
                    javaSource);

            javac.getTask(
                            null,
                            fileManager,
                            diagnostics,
                            options,
                            null,
                            fileManager.getJavaFileObjectsFromFiles(sourceFiles))
                    .call();
        }

        List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
                .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                .collect(Collectors.toList());

        assertEquals(0, errors.size(), "There was compilation errors: " + errors);
    }

    private static List<File> findJavaSources(File directory) throws IOException {
        if (!directory.isDirectory()) {
            return new ArrayList<>();
        }
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Returns this module's test classpath, which is what the verifiers and the generated sources are compiled
     * against.
     * <p>
     * Surefire normally hands the forked JVM a manifest-only "booter" jar rather than a real classpath, so
     * {@code java.class.path} on its own is a single jar whose {@code Class-Path} manifest entry holds the actual
     * entries. Expanding that keeps this in step with whatever the POM declares, with no build step to copy jars
     * around and no second list to maintain here.
     */
    private static List<String> resolveTestClasspath() {
        List<String> classPath = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path", "").split(File.pathSeparator)) {
            if (entry.isEmpty()) {
                continue;
            }
            classPath.add(entry);
            // a jar may carry its own Class-Path, so keep both it and whatever it points at
            classPath.addAll(expandManifestClassPath(new File(entry)));
        }
        return classPath;
    }

    private static List<String> expandManifestClassPath(File jar) {
        if (!jar.isFile() || !jar.getName().endsWith(".jar")) {
            return new ArrayList<>();
        }
        try (JarFile jarFile = new JarFile(jar)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return new ArrayList<>();
            }
            String classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
            if (classPath == null || classPath.trim().isEmpty()) {
                return new ArrayList<>();
            }
            List<String> entries = new ArrayList<>();
            for (String entry : classPath.trim().split("\\s+")) {
                try {
                    entries.add(Paths.get(URI.create(entry)).toString());
                } catch (IllegalArgumentException | java.nio.file.FileSystemNotFoundException e) {
                    // a relative entry, resolved against the jar itself
                    entries.add(new File(jar.getParentFile(), entry).getAbsolutePath());
                }
            }
            return entries;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the manifest of " + jar, e);
        }
    }

    /**
     * Run a verifier class in a classloader context where compiled generated sources are available
     *
     * @param verifierClassName the class name of the verifier class
     */
    protected void verifyCompiledGeneratedSources(String verifierClassName) {
        addClassPathFile(getOutputClasses());

        addClassPathFile(getTestFile("target/classes"));

        addClassPathFile(getTestFile("target/test-classes"));

        // the verifier runs in a classloader with no parent, so it needs the test classpath spelled out
        for (String entry : resolveTestClasspath()) {
            File file = new File(entry);
            if (file.exists()) {
                addClassPathFile(file);
            }
        }

        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), null);

        Thread.currentThread().setContextClassLoader(classLoader);

        try {
            Class<?> clazz = classLoader.loadClass(verifierClassName);

            Method verify = clazz.getMethod("verify", new Class[0]);

            try {
                verify.invoke(clazz.getDeclaredConstructor().newInstance(), new Object[0]);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        } catch (Throwable throwable) {
            throw new VerifierException("Error verifying modello tests: " + throwable.getMessage(), throwable);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCCL);
        }
    }

    protected void addClassPathFile(File file) {
        assertTrue(file.exists(), "File doesn't exists: " + file.getAbsolutePath());

        try {
            urls.add(file.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        classPathElements.add(file.getAbsolutePath());
    }

    protected void printClasspath(URLClassLoader classLoader) {
        URL[] urls = classLoader.getURLs();

        for (URL url : urls) {
            System.out.println(url);
        }
    }

    protected void assertGeneratedFileExists(String filename) {
        File file = new File(getOutputDirectory(), filename);

        assertTrue(file.canRead(), "Missing generated file: " + file.getAbsolutePath());

        assertTrue(file.length() > 0, "The generated file is empty.");
    }

    protected List<String> getClassPathElements() {
        return classPathElements;
    }
}

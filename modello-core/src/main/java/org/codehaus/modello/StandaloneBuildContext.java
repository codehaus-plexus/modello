package org.codehaus.modello;

/*
 * Copyright (c) 2026, Codehaus.org
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.build.BuildContext;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.io.CachingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link BuildContext} for running Modello outside a Maven build: from {@link ModelloCli}, the Ant task, an
 * embedder or a unit test. Every build is a full build, files are written straight to disk, and messages go to the
 * log.
 * <p>
 * The {@code BuildContext} implementations that plexus-build-api ships expect a Maven session, and an IDE such as
 * m2e binds its own, so this class is not a component: it is registered only where Modello starts its own
 * container, through {@link #register(PlexusContainer)}.
 *
 * @since 2.9.1
 */
public class StandaloneBuildContext implements BuildContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandaloneBuildContext.class);

    private final Map<String, Object> values = new ConcurrentHashMap<>();

    /**
     * Registers a new instance as the default {@link BuildContext} of a container that Modello started itself.
     *
     * @param container the container to register the instance in
     */
    public static void register(PlexusContainer container) {
        container.addComponent(new StandaloneBuildContext(), BuildContext.class, "default");
    }

    @Override
    public boolean hasDelta(String relpath) {
        return true;
    }

    @Override
    public boolean hasDelta(File file) {
        return true;
    }

    @Override
    public boolean hasDelta(List<String> relpaths) {
        return true;
    }

    @Override
    public void refresh(File file) {
        // nothing outside the file system to notify
    }

    @Override
    public OutputStream newFileOutputStream(File file) throws IOException {
        return new CachingOutputStream(file.toPath());
    }

    @Override
    public Scanner newScanner(File basedir) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(basedir);
        return scanner;
    }

    @Override
    public Scanner newDeleteScanner(File basedir) {
        return new EmptyScanner(basedir);
    }

    @Override
    public Scanner newScanner(File basedir, boolean ignoreDelta) {
        return newScanner(basedir);
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void setValue(String key, Object value) {
        if (value == null) {
            values.remove(key);
        } else {
            values.put(key, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key) {
        return (T) values.get(key);
    }

    @Override
    public void addWarning(File file, int line, int column, String message, Throwable cause) {
        addMessage(file, line, column, message, SEVERITY_WARNING, cause);
    }

    @Override
    public void addError(File file, int line, int column, String message, Throwable cause) {
        addMessage(file, line, column, message, SEVERITY_ERROR, cause);
    }

    @Override
    public void addMessage(File file, int line, int column, String message, int severity, Throwable cause) {
        String text = file.getAbsolutePath() + " [" + line + ':' + column + "]: " + message;
        if (severity == SEVERITY_ERROR) {
            LOGGER.error(text, cause);
        } else if (severity == SEVERITY_WARNING) {
            LOGGER.warn(text, cause);
        } else {
            LOGGER.debug(text, cause);
        }
    }

    @Override
    public void removeMessages(File file) {
        // messages only go to the log
    }

    @Override
    public boolean isUptodate(File target, File source) {
        return target != null
                && target.exists()
                && source != null
                && source.exists()
                && target.lastModified() > source.lastModified();
    }

    /** A full build never reports deleted files. */
    private static final class EmptyScanner implements Scanner {
        private final File basedir;

        EmptyScanner(File basedir) {
            this.basedir = basedir;
        }

        @Override
        public void setIncludes(String[] includes) {}

        @Override
        public void setExcludes(String[] excludes) {}

        @Override
        public void addDefaultExcludes() {}

        @Override
        public void scan() {}

        @Override
        public String[] getIncludedFiles() {
            return new String[0];
        }

        @Override
        public String[] getIncludedDirectories() {
            return new String[0];
        }

        @Override
        public File getBasedir() {
            return basedir;
        }

        @Override
        public void setFilenameComparator(Comparator<String> filenameComparator) {}
    }
}

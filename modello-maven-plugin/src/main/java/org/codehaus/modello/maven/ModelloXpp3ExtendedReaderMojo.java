package org.codehaus.modello.maven;

import javax.inject.Inject;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.plexus.build.BuildContext;

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

/**
 * Creates an XPP3 extended reader from the model. An extended reader populates the parsed model with metadata about the
 * line/column from which the data was read if the model supports this.
 *
 * @since 1.4
 * @author Benjamin Bentmann
 */
@Mojo(name = "xpp3-extended-reader", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class ModelloXpp3ExtendedReaderMojo extends ModelloXpp3ReaderMojo {

    @Inject
    public ModelloXpp3ExtendedReaderMojo(BuildContext buildContext, ModelloCore modelloCore) {
        super(buildContext, modelloCore);
    }

    @Override
    protected String getGeneratorType() {
        return "xpp3-extended-reader";
    }
}

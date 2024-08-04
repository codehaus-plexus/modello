package org.codehaus.modello.core;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import javax.inject.Inject;

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
@PlexusTest
class DefaultModelloCoreTest {

    @Inject
    ModelloCore modello;

    @Test
    void modelWithDuplicateClasses() throws Exception {
        try {
            modello.loadModel(getTestFile("src/test/resources/models/duplicate-classes.mdo"));

            fail("Expected ModelloRuntimeException.");
        } catch (ModelloRuntimeException ex) {
            assertEquals("Duplicate class: MyClass.", ex.getMessage());
        }
    }

    @Test
    void modelWithDuplicateFields() throws Exception {
        try {
            modello.loadModel(getTestFile("src/test/resources/models/duplicate-fields.mdo"));

            fail("Expected ModelloRuntimeException.");
        } catch (ModelloRuntimeException ex) {
            assertEquals("Duplicate field in MyClass: MyField.", ex.getMessage());
        }
    }

    @Test
    void modelWithDuplicateAssociations() throws Exception {
        try {
            modello.loadModel(getTestFile("src/test/resources/models/duplicate-associations.mdo"));

            fail("Expected ModelloRuntimeException.");
        } catch (ModelloRuntimeException ex) {
            assertEquals("Duplicate field in MyClass: MyAssociation.", ex.getMessage());
        }
    }

    @Test
    void recursion() throws Exception {
        modello.loadModel(getTestFile("src/test/resources/models/recursion.mdo"));
    }
}

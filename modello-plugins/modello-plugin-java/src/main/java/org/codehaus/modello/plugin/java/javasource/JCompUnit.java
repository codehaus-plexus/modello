/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@codehaus.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.codehaus.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2002 (C) Intalio, Inc. All Rights Reserved.
 *
 * Contributors:
 * --------------
 * Gary Shea (shea@gtsdesign.com)   - Original Author
 * Keith Visco (kvisco@intalio.com) - Changed JCompElement references to
 *                                    JStructure, some additional tweaking
 *                                    to get it working with the current
 *                                    Javasource package.
 *
 * $Id$
 */
package org.codehaus.modello.plugin.java.javasource;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.plexus.util.WriterFactory;

/**
 * A representation of the Java Source code for a Java compilation
 * unit. This is
 * a useful utility when creating in memory source code.
 * This package was modelled after the Java Reflection API
 * as much as possible to reduce the learning curve.
 * @author <a href="mailto:shea@gtsdesign.com">Gary Shea</a>
 * @version $Revision$ $Date$
 **/
public class JCompUnit {

    /**
     * The Id for Source control systems
     * I needed to separate this line to prevent CVS from
     * expanding it here! ;-)
     **/
    private static final String DEFAULT_HEADER = "$" + "Id$";

    private JComment header = null;

    /**
     * The package for this JCompUnit
     **/
    private String packageName = null;

    /**
     * The file to which this JCompUnit will be saved
     **/
    private String fileName = null;

    /**
     * The set of top-level classes that live in this compilation unit.
     **/
    // private TypeList classes = null;
    private List<JClass> classes = null;

    /**
     * The set of top-level interfaces that live in this compilation unit.
     **/
    // private TypeList interfaces = null;
    private List<JInterface> interfaces = null;

    /**
     * Creates a new JCompUnit
     * @param packageName the name of the package for this JCompUnit.
     * If packageName is null or empty, no 'package' line will be generated.
     * @param fileName the name of the file in which this JCompUnit
     * will be stored
     **/
    public JCompUnit(String packageName, String fileName) {
        this.packageName = packageName;
        this.fileName = fileName;
        init();
    } // -- JCompUnit

    /**
     * Creates a new JCompUnit with the given JClass (which must have
     * been created with either a full class name or package/local
     * name) as the public class.  Package and file name are taken
     * from jClass.
     * @param jClass the public class for this JCompUnit.
     **/
    public JCompUnit(JClass jClass) {
        this.packageName = jClass.getPackageName();

        // The outer name is the package plus the simple name of the
        // outermost enclosing class.  The file name is just the
        // simple name of the outermost enclosing class, so the
        // package name part must be stripped off.

        /*
          Commented out until inner-class support has been added.
          kvisco - 20021211

        String outer = jClass.getOuterName();
        int lastDot = outer.lastIndexOf(".");
        String filePrefix;
        if (lastDot != -1) {
            filePrefix = outer.substring (lastDot + 1);
        } else {
            filePrefix = outer;
        }
        */
        String filePrefix = jClass.getLocalName();

        this.fileName = filePrefix + ".java";
        init();
        classes.add(jClass);
    } // -- JCompUnit

    /**
     * Creates a new JCompUnit with the given JInterface as public interface
     * Package and file name are taken from jInterface.
     * @param jInterface the public interface for this JCompUnit.
     **/
    public JCompUnit(JInterface jInterface) {
        this.packageName = jInterface.getPackageName();
        this.fileName = jInterface.getLocalName() + ".java";
        init();
        interfaces.add(jInterface);
    } // -- JCompUnit

    private void init() {
        classes = new ArrayList<JClass>();
        interfaces = new ArrayList<JInterface>();
    }

    /**
     * Adds the given JStructure (either a JInterface or
     * a JClass) to this JCompUnit.
     *
     * @param jStructure the JStructure to add
     * @exception java.lang.IllegalArgumentException when the given
     * JStructure has the same name of an existing JStructure
     * or if the class of jStructure is unknown.
     */
    public void addStructure(JStructure jStructure) throws IllegalArgumentException {
        if (jStructure instanceof JInterface) addInterface((JInterface) jStructure);
        else if (jStructure instanceof JClass) addClass((JClass) jStructure);
        else {
            String err = "Unknown JStructure subclass '" + jStructure.getClass().getName() + "'.";
            throw new IllegalArgumentException(err);
        }
    } // -- addStructure

    /**
     * Adds a JClass which should be printed in this file.
     *
     * @param jClass the JClass to add
     **/
    public void addClass(JClass jClass) {
        classes.add(jClass);
    } // -- addClass

    /**
     * Adds a JInterface which should be printed in this file.
     *
     * @param jInterface the JInterface to add
     **/
    public void addInterface(JInterface jInterface) {
        interfaces.add(jInterface);
    } // -- addInterface

    /**
     * returns a array of String containing all import classes/packages,
     * also imports within the same package of this object.
     * @return a array of String containing all import classes/packages,
     * also imports within the same package of this object.
     */
    public SortedSet<String> getImports() {

        SortedSet<String> allImports = new TreeSet<String>();

        // add imports from classes
        for (JClass jClass : classes) {
            Enumeration<String> e = jClass.getImports();
            while (e.hasMoreElements()) {
                allImports.add(e.nextElement());
            }
        }

        for (JInterface jInterface : interfaces) {
            Enumeration<String> e = jInterface.getImports();
            while (e.hasMoreElements()) {
                allImports.add(e.nextElement());
            }
        }

        return allImports;
    }

    /**
     * Returns the name of the file that this JCompUnit would be
     * printed as, given a call to #print.
     *
     * @param destDir the destination directory. This may be null.
     * @return the name of the file that this JCompUnit would be
     * printed as, given a call to #print.
     **/
    public String getFilename(String destDir) {

        String filename = new String(fileName);

        // -- Convert Java package to path string
        String javaPackagePath = "";
        if ((packageName != null) && (packageName.length() > 0)) {
            javaPackagePath = packageName.replace('.', File.separatorChar);
        }

        // -- Create fully qualified path (including 'destDir') to file
        File pathFile;
        if (destDir == null) pathFile = new File(javaPackagePath);
        else pathFile = new File(destDir, javaPackagePath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }

        // -- Prefix filename with path
        if (pathFile.toString().length() > 0) filename = pathFile.toString() + File.separator + filename;

        return filename;
    } // -- getFilename

    /**
     * Returns the name of the package that this JCompUnit is a member of
     * @return the name of the package that this JCompUnit is a member of,
     * or null if there is no current package name defined
     **/
    public String getPackageName() {
        return this.packageName;
    } // -- getPackageName

    protected static String getPackageFromClassName(String className) {
        int idx = -1;
        if ((idx = className.lastIndexOf('.')) > 0) {
            return className.substring(0, idx);
        }
        return null;
    } // -- getPackageFromClassName

    /**
     * Prints the source code for this JClass in the current directory
     * with the default line seperator of the the runtime platform.
     * @see #print(java.lang.String, java.lang.String)
     **/
    public void print() {
        print(null, null);
    } // -- print

    /**
     * Prints the source code for this JClass
     * with the default line seperator of the the runtime platform.
     * @param destDir the destination directory to generate the file.
     * @see #print(java.lang.String, java.lang.String)
     **/
    public void print(String destDir) {
        print(destDir, null);
    } // -- print

    /**
     * Prints the source code for this JCompUnit.
     * @param destDir the destination directory to generate the file.
     * @param lineSeparator the line separator to use at the end of each line.
     * If null, then the default line separator for the runtime platform will
     * be used.
     **/
    public void print(String destDir, String lineSeparator) {

        // -- open output file
        String filename = getFilename(destDir);

        File file = new File(filename);
        JSourceWriter jsw = null;
        try {
            jsw = new JSourceWriter(WriterFactory.newPlatformWriter(file));
        } catch (java.io.IOException ioe) {
            System.out.println("unable to create compilation unit file: " + filename);
            return;
        }

        if (lineSeparator == null) {
            lineSeparator = System.getProperty("line.separator");
        }
        jsw.setLineSeparator(lineSeparator);
        print(jsw);
        jsw.flush();
        jsw.close();
    } // -- print

    /**
     * Prints the source code for this JClass.
     * @param jsw the JSourceWriter to print to.
     **/
    public void print(JSourceWriter jsw) {

        // Traverse the nested class and interface heirarchy and
        // update the names to match the compilation unit.

        resolveNames();
        StringBuilder buffer = new StringBuilder();

        // -- write file header
        if (header != null) header.print(jsw);
        else {
            jsw.writeln("/*");
            jsw.writeln(" * " + DEFAULT_HEADER);
            jsw.writeln("*/");
        }
        jsw.writeln();
        jsw.flush();

        // -- print package name
        if ((packageName != null) && (packageName.length() > 0)) {

            buffer.setLength(0);
            buffer.append("package ");
            buffer.append(packageName);
            buffer.append(';');
            jsw.writeln(buffer.toString());
            jsw.writeln();
        }

        // -- print imports
        jsw.writeln("  //---------------------------------------------/");
        jsw.writeln(" //- Imported classes, interfaces and packages -/");
        jsw.writeln("//---------------------------------------------/");
        jsw.writeln();
        SortedSet<String> allImports = getImports();
        String compUnitPackage = getPackageName();
        for (String importName : allImports) {
            String importsPackage = JStructure.getPackageFromClassName(importName);
            if (importsPackage != null && !importsPackage.equals(compUnitPackage)) {
                jsw.write("import ");
                jsw.write(importName);
                jsw.writeln(';');
            }
        }
        jsw.writeln();

        // Print the public elements, interfaces first, then classes.
        // There should only be one public element, but if there are
        // more we let the compiler catch it.
        printStructures(jsw, true);

        // Print the remaining non-public elements, interfaces first.
        printStructures(jsw, false);

        jsw.flush();
    } // -- print

    /**
     * Print the source code for the contained JClass objects.
     * @param jsw the JSourceWriter to print to.
     * @param printPublic if true, print only public classes; if
     * false, print only non-public classes.
     **/
    public final void printStructures(JSourceWriter jsw, boolean printPublic) {

        // -- print class information
        // -- we need to add some JavaDoc API adding comments

        boolean isFirst = true;

        // SortedSet interfaceList = interfaces.sortedOnFullName();
        for (JInterface jInterface : interfaces) {
            if (jInterface.getModifiers().isPublic() == printPublic) {
                if (isFirst) {
                    Header.print(jsw, printPublic);
                    isFirst = false;
                }
                jInterface.print(jsw, true);
                jsw.writeln();
            }
        }

        // SortedSet classList = classes.sortedOnFullName();
        for (JClass jClass : classes) {
            if (jClass.getModifiers().isPublic() == printPublic) {
                if (isFirst) {
                    Header.print(jsw, printPublic);
                    isFirst = false;
                }
                jClass.print(jsw, true);
                jsw.writeln();
            }
        }
    } // -- printElements(JSourceWriter, int)

    /**
     * Sets the header comment for this JCompUnit
     * @param comment the comment to display at the top of the source file
     * when printed
     **/
    public void setHeader(JComment comment) {
        this.header = comment;
    } // -- setHeader

    // -------------------/
    // - Private Methods -/
    // -------------------/

    /**
     * Update the names of nested classes and interfaces.
     **/
    private void resolveNames() {

        /*
          Commented out until support for inner-classes is added
          kvisco - 20021211

        for (int i = 0; i < classes.size(); i++) {
            JClass jClass = (JClass) classes.get(i);
            jClass.resolveNames(packageName, null);
        }
        for (int i = 0; i < interfaces.size(); i++) {
            JInterface jInterface = (JInterface) interfaces.get(i);
            jInterface.resolveNames(packageName, null);
        }
        */
    } // -- resolveNames
} // -- JCompUnit

/**
 * Print the headers delineating the public and non-public elements of
 * the compilation unit.
 **/
class Header {

    private static String[] publicHeader = {
        "  //-----------------------------/", " //-  Public Class / Interface -/", "//-----------------------------/",
    };
    private static String[] nonPublicHeader = {
        "  //-------------------------------------/",
        " //-  Non-Public Classes / Interfaces  -/",
        "//-------------------------------------/",
    };

    /**
     * Print the specified header to the given Writer.
     * @params JSourceWriter an open JSourceWriter
     * @params boolean if true print the public header, otherwise
     * print the non-public header.
     **/
    protected static void print(JSourceWriter jsw, boolean printPublic) {
        String[] header = printPublic ? publicHeader : nonPublicHeader;
        for (String aHeader : header) {
            jsw.writeln(aHeader);
        }
        jsw.writeln();
    }
} // -- Header

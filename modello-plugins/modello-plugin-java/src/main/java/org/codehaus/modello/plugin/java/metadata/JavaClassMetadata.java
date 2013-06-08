package org.codehaus.modello.plugin.java.metadata;

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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.modello.metadata.ClassMetadata;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class JavaClassMetadata
    implements ClassMetadata
{
    public static final String ID = JavaClassMetadata.class.getName();

    public static final String CLONE_NONE = "none";
    public static final String CLONE_SHALLOW = "shallow";
    public static final String CLONE_DEEP = "deep";

    public static final List<String> CLONE_MODES;

    static
    {
        CLONE_MODES = new ArrayList<String>();
        CLONE_MODES.add( CLONE_NONE );
        CLONE_MODES.add( CLONE_SHALLOW );
        CLONE_MODES.add( CLONE_DEEP );
    }

    private boolean abstractMode;

    private boolean enabled;

    private String cloneMode;

    private String cloneHook;

    /**
     * @since 1.8
     */
    private boolean generateToString = false;

    /**
     * @since 1.8
     */
    private boolean generateBuilder = false;

    /**
     * @since 1.8
     */
    private boolean generateStaticCreators = false;

    public void setAbstract( boolean abstractMode )
    {
        this.abstractMode = abstractMode;
    }

    public boolean isAbstract()
    {
        return abstractMode;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( boolean generate )
    {
        this.enabled = generate;
    }

    public String getCloneMode()
    {
        return cloneMode;
    }

    public void setCloneMode( String cloneMode )
    {
        this.cloneMode = cloneMode;
    }

    public String getCloneHook()
    {
        return cloneHook;
    }

    public void setCloneHook( String cloneHook )
    {
        this.cloneHook = cloneHook;
    }

    public boolean isGenerateToString()
    {
        return generateToString;
    }

    public void setGenerateToString( boolean generateToString )
    {
        this.generateToString = generateToString;
    }

    public boolean isGenerateBuilder()
    {
        return generateBuilder;
    }

    public void setGenerateBuilder( boolean generateBuilder )
    {
        this.generateBuilder = generateBuilder;
    }

    public boolean isGenerateStaticCreators()
    {
        return generateStaticCreators;
    }

    public void setGenerateStaticCreators( boolean generateStaticCreators )
    {
        this.generateStaticCreators = generateStaticCreators;
    }

}

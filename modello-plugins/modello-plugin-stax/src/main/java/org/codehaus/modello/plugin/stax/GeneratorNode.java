package org.codehaus.modello.plugin.stax;

import org.codehaus.modello.model.ModelAssociation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/*
 * Copyright (c) 2006, Codehaus.org
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

class GeneratorNode
{
    private final String to;

    private boolean referencableChildren;

    private List<GeneratorNode> children = new LinkedList<GeneratorNode>();

    private ModelAssociation association;

    private boolean referencable;

    private Map<String, GeneratorNode> nodesWithReferencableChildren = new HashMap<String, GeneratorNode>();

    private List<String> chain;

    GeneratorNode( String to, GeneratorNode parent )
    {
        this( to, parent, null );
    }

    GeneratorNode( ModelAssociation association, GeneratorNode parent )
    {
        this( association.getTo(), parent, association );
    }

    private GeneratorNode( String to, GeneratorNode parent, ModelAssociation association )
    {
        this.to = to;
        this.association = association;
        this.chain = parent != null ? new ArrayList<String>( parent.getChain() ) : new ArrayList<String>();
        this.chain.add( to );
    }

    public boolean isReferencableChildren()
    {
        return referencableChildren;
    }

    public void setReferencableChildren( boolean referencableChildren )
    {
        this.referencableChildren = referencableChildren;
    }

    public void addChild( GeneratorNode child )
    {
        children.add( child );
        if ( child.referencableChildren )
        {
            nodesWithReferencableChildren.put( child.to, child );
        }
    }

    public List<GeneratorNode> getChildren()
    {
        return children;
    }

    public String toString()
    {
        return "to = " + to + "; referencableChildren = " + referencableChildren + "; children = " + children;
    }

    public String getTo()
    {
        return to;
    }

    public ModelAssociation getAssociation()
    {
        return association;
    }

    public void setAssociation( ModelAssociation association )
    {
        this.association = association;
    }

    public void setReferencable( boolean referencable )
    {
        this.referencable = referencable;
    }

    public boolean isReferencable()
    {
        return referencable;
    }

    public Map<String, GeneratorNode> getNodesWithReferencableChildren()
    {
        return nodesWithReferencableChildren;
    }

    public void addNodesWithReferencableChildren( Map<String, GeneratorNode> allChildNodes )
    {
        this.nodesWithReferencableChildren.putAll( allChildNodes );
    }

    public List<String> getChain()
    {
        return chain;
    }
}

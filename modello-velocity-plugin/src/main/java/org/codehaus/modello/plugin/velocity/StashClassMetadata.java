/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.codehaus.modello.plugin.velocity;

import org.codehaus.modello.metadata.ClassMetadata;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public class StashClassMetadata
    implements ClassMetadata
{
    private boolean storable;

    public void setStorable( boolean storable )
    {
        this.storable = storable;
    }

    public boolean isStorable()
    {
        return storable;
    }
}

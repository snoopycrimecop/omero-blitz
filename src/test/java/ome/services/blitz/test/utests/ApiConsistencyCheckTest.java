/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;


import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ApiConsistencyCheckTest {

    @Test
    public void testCreateContextAndHopeItDoesntExplode() {
        ome.system.OmeroContext ctx = new ome.system.OmeroContext(new String[]{
                "classpath:omero/test.xml",
                "classpath:ome/services/blitz-servantDefinitions.xml",
                "classpath:ome/services/blitz-graph-rules.xml"});
        Assert.assertNotNull(ctx);
    }

}
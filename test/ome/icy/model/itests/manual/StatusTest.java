/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;
import ome.services.blitz.Status;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class StatusTest extends MockedBlitzTest {

    BlitzServerFixture fixture;

    @Override
    @AfterMethod
    public void tearDown() throws Exception {
        fixture.tearDown();
        fixture = null;
    }

    @Test
    public void testStatus() throws Exception {

        fixture = new BlitzServerFixture();

        String[] args = new String[] {};
        Status status = new Status(args);
        status.run();
    }

}

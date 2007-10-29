/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import java.util.List;

import ome.icy.fixtures.BlitzServerFixture;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;

import org.testng.annotations.Test;

public class RegisteredServicesTest extends MockedBlitzTest {

    @Test
    public void testkeepAllAliveAndkeepAliveWorkAfterPause() throws Exception {

        fixture = new BlitzServerFixture(200 /* not under test */, 2);
        fixture.methodCall();

        ServiceFactoryPrx session = fixture.createSession();

        RenderingEnginePrx prx1 = session.createRenderingEngine();
        RenderingEnginePrx prx2 = session.createRenderingEngine();

        List<String> idsA = session.activeServices();

        prx1.close();

        List<String> idsB = session.activeServices();

        assertTrue(idsA.size() == 2);
        assertTrue(idsB.size() == 1);

    }

}

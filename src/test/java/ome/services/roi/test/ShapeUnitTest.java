/*
 *   $Id$
 *   
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi.test;

import ome.services.roi.GeomTool;
import omero.model.SmartLineI;
import omero.model.SmartShape.Util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
@Test(groups = { "rois" })
public class ShapeUnitTest {

    GeomTool geomTool = new GeomTool(null, null, null);

    @Test
    public void testDiscriminators() throws Exception {
        Assert.assertEquals("text", geomTool.discriminator("Text"));
        Assert.assertEquals("text", geomTool.discriminator("TextI"));
        Assert.assertEquals("text", geomTool.discriminator("omero.model.Text"));
        Assert.assertEquals("text", geomTool.discriminator("omero.model.TextI"));
        Assert.assertEquals("text", geomTool.discriminator("omero::model::Text"));
        Assert.assertEquals("text", geomTool.discriminator("::omero::model::Text"));
        Assert.assertEquals("mask", geomTool.discriminator("Mask"));
        Assert.assertEquals("mask", geomTool.discriminator("MaskI"));
        Assert.assertEquals("mask", geomTool.discriminator("omero.model.Mask"));
        Assert.assertEquals("mask", geomTool.discriminator("omero.model.MaskI"));
        Assert.assertEquals("mask", geomTool.discriminator("omero::model::Mask"));
        Assert.assertEquals("mask", geomTool.discriminator("::omero::model::Mask"));
    }

    
    @Test
    public void testGeometryOfLineGood() throws Exception {
        SmartLineI l = (SmartLineI) geomTool.ln(0, 0, 1, 1);
        Assert.assertTrue(Util.checkNonNull(l.asPoints()));
    }

    @Test(expectedExceptions = AssertionError.class)
    public void testGeometryOfLineBad() throws Exception {
        SmartLineI l = (SmartLineI) geomTool.ln(0, 0, 1, 1);
        l.setY2(null);
        Assert.assertFalse(Util.checkNonNull(l.asPoints()));
    }
}

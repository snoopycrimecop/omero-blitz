/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2019 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package omero;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



@Test(groups = "unit", timeOut = 5000)
public class ClientDefaultRouterTest {

  @DataProvider
  public Object[][] clientHosturlParams() {
    return new Object[][]{
        {
          new String[]{"--omero.host=example.org"},
          "OMERO.Glacier2/router:ssl -p 4064 -h example.org",
        },
        {
            new String[]{"--omero.host=example.org", "--omero.port=12345"},
            "OMERO.Glacier2/router:ssl -p 12345 -h example.org",
        },

        {
            new String[]{"--omero.host=ssl://example.org"},
            "OMERO.Glacier2/router:ssl -p 4064 -h example.org",
        },
        {
            new String[]{"--omero.host=ssl://example.org:12345"},
            "OMERO.Glacier2/router:ssl -p 12345 -h example.org",
        },

        {
            new String[]{"--omero.host=tcp://example.org"},
            "OMERO.Glacier2/router:tcp -p 4063 -h example.org",
        },
        {
            new String[]{"--omero.host=tcp://example.org:12345"},
            "OMERO.Glacier2/router:tcp -p 12345 -h example.org",
        },

        {
            new String[]{"--omero.host=wss://example.org"},
            "OMERO.Glacier2/router:wss -p 443 -h example.org",
        },
        {
            new String[]{"--omero.host=wss://example.org:12345"},
            "OMERO.Glacier2/router:wss -p 12345 -h example.org",
        },
        {
            new String[]{"--omero.host=wss://example.org/omero"},
            "OMERO.Glacier2/router:wss -p 443 -h example.org -r /omero",
        },
        {
            new String[]{"--omero.host=wss://example.org:12345/omero"},
            "OMERO.Glacier2/router:wss -p 12345 -h example.org -r /omero",
        },

        {
            new String[]{"--omero.host=ws://example.org"},
            "OMERO.Glacier2/router:ws -p 80 -h example.org",
        },
        {
            new String[]{"--omero.host=ws://example.org:12345"},
            "OMERO.Glacier2/router:ws -p 12345 -h example.org",
        },
        {
            new String[]{"--omero.host=ws://example.org/omero"},
            "OMERO.Glacier2/router:ws -p 80 -h example.org -r /omero",
        },
        {
            new String[]{"--omero.host=ws://example.org:12345/omero"},
            "OMERO.Glacier2/router:ws -p 12345 -h example.org -r /omero",
        },

    };
  }

  @Test
  public void testHost() {
    client c = new client("example.org");
    Assert.assertEquals(c.getProperty("Ice.Default.Router"), "OMERO.Glacier2/router:ssl -p 4064 -h example.org");
  }

  @Test
  public void testHostPort() {
    client c = new client("example.org", 12345);
    Assert.assertEquals(c.getProperty("Ice.Default.Router"), "OMERO.Glacier2/router:ssl -p 12345 -h example.org");
  }

  @Test(dataProvider = "clientHosturlParams")
  public void testHosturl(String[] args, String expected) {
    client c = new client(args);
    Assert.assertEquals(c.getProperty("Ice.Default.Router"), expected);
  }
}

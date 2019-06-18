/*
 *   Copyright 2019 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.UUID;

import omero.api.*;
import omero.grid.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

/**
 * Blitz-specific wrapper for table proxies to facilitate the
 * cleanup of files on session exit.
 */
public class TableCloserI extends AbstractAmdServant
    implements _StatefulServiceInterfaceOperations {

    private static Logger log = LoggerFactory.getLogger(TableCloserI.class);

    private final TablePrx table;

    private final ServiceFactoryI sf;

    private final Ice.Identity id;

    private final StatefulServiceInterfacePrx self;

    public TableCloserI(ServiceFactoryI sf, TablePrx tablePrx, Ice.Identity id)
            throws omero.ServerError {
        super(null, null);
        this.table = tablePrx;
        this.sf = sf;
        this.id = id;
        this.self = StatefulServiceInterfacePrxHelper.uncheckedCast(
                sf.registerServant(this.id, new _StatefulServiceInterfaceTie(this)));
        sf.allow(this.self);
    }

    public StatefulServiceInterfacePrx getProxy() {
        return self;
    }

    // DELEGATION

    @Override
    public void close_async(AMD_StatefulServiceInterface_close __cb, Current __current) {
        try {
            table.close();
            __cb.ice_response();
        } catch (Exception e) {
            __cb.ice_exception(e);
        } catch (Throwable t) {
            __cb.ice_exception(new RuntimeException("unknown throwable", t));
        }
    }
}

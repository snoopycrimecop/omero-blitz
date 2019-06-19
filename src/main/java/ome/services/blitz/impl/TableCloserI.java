/*
 *   Copyright 2019 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import Ice.Current;
import omero.RType;
import omero.ServerError;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrxHelper;
import omero.api._StatefulServiceInterfaceOperations;
import omero.api._StatefulServiceInterfaceTie;
import omero.grid.*;
import omero.model.OriginalFile;

import java.util.Map;

/**
 * Blitz-specific wrapper for table proxies to facilitate the
 * cleanup of files on session exit.
 */
public class TableCloserI extends AbstractCloseableAmdServant
    implements _StatefulServiceInterfaceOperations, _TableOperations {

    /**
     * Proxy to the remote table which should only be used internally to this instance.
     */
    private final TablePrx table;

    /**
     * Proxy to this instance which will be passed to clients.
     */
    private final TablePrx self;

    private final Ice.Identity tableId;

    private final Ice.Identity statefulId;

    private final ServiceFactoryI sf;

    public TableCloserI(ServiceFactoryI sf, TablePrx tablePrx, Ice.Identity id)
            throws omero.ServerError {
        super(null, null);
        this.table = tablePrx;
        this.sf = sf;
        this.tableId = id;
        this.statefulId = new Ice.Identity(id.name + "-closer", id.category);
        this.self = TablePrxHelper.uncheckedCast(
                sf.registerServant(tableId, new _TableTie(this)));
        StatefulServiceInterfacePrx closer = StatefulServiceInterfacePrxHelper.uncheckedCast(
                sf.registerServant(statefulId, new _StatefulServiceInterfaceTie(this)));
        sf.allow(self);
        sf.allow(closer);
    }

    public TablePrx getProxy() {
        return self;
    }

    // CLOSEABLE

    @Override
    protected void preClose(Current current) throws Throwable {
        try {
            this.table.close();
        } finally {
            sf.unregisterServant(tableId);
        }
    }

    @Override
    protected void postClose(Current current) {

    }

    // DELEGATION

    private Map<String, String> check(Current __current) {
        if (__current != null) {
            return __current.ctx;
        }
        return null;
    }

    @Override
    public OriginalFile getOriginalFile(Current __current) throws ServerError {
        return table.getOriginalFile(check(__current));
    }

    @Override
    public Column[] getHeaders(Current __current) throws ServerError {
        return table.getHeaders(check(__current));
    }

    @Override
    public long getNumberOfRows(Current __current) throws ServerError {
        return table.getNumberOfRows(check(__current));
    }

    @Override
    public long[] getWhereList(String condition, Map<String, RType> variables, long start, long stop, long step, Current __current) throws ServerError {
        return table.getWhereList(condition, variables, start, stop, step, check(__current));
    }

    @Override
    public Data readCoordinates(long[] rowNumbers, Current __current) throws ServerError {
        return table.readCoordinates(rowNumbers, check(__current));
    }

    @Override
    public Data read(long[] colNumbers, long start, long stop, Current __current) throws ServerError {
        return table.read(colNumbers, start, stop, check(__current));
    }

    @Override
    public Data slice(long[] colNumbers, long[] rowNumbers, Current __current) throws ServerError {
        return table.slice(colNumbers, rowNumbers, check(__current));
    }

    @Override
    public void addData(Column[] cols, Current __current) throws ServerError {
        table.addData(cols, check(__current));
    }

    @Override
    public void update(Data modifiedData, Current __current) throws ServerError {
        table.update(modifiedData, check(__current));
    }

    @Override
    public Map<String, RType> getAllMetadata(Current __current) throws ServerError {
        return table.getAllMetadata(check(__current));
    }

    @Override
    public RType getMetadata(String key, Current __current) throws ServerError {
        return table.getMetadata(key, check(__current));
    }

    @Override
    public void setAllMetadata(Map<String, RType> dict, Current __current) throws ServerError {
        table.setAllMetadata(dict, check(__current));
    }

    @Override
    public void setMetadata(String key, RType value, Current __current) throws ServerError {
        table.setMetadata(key, value, check(__current));
    }

    @Override
    public void initialize(Column[] cols, Current __current) throws ServerError {
        table.initialize(cols, check(__current));
    }

    @Override
    public int addColumn(Column col, Current __current) throws ServerError {
        return table.addColumn(col, check(__current));
    }

    @Override
    public void delete(Current __current) throws ServerError {
        table.delete(check(__current));
    }

}

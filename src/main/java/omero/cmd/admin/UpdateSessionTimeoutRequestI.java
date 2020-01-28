/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.cmd.admin;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import Ice.Communicator;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionManager;
import ome.system.ServiceFactory;
import omero.RLong;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.cmd.UpdateSessionTimeoutRequest;
import omero.util.ObjectFactoryRegistry;

@SuppressWarnings("serial")
public class UpdateSessionTimeoutRequestI extends UpdateSessionTimeoutRequest
    implements IRequest {

    public static class Factory extends ObjectFactoryRegistry {
        private final ObjectFactory factory;
        public Factory(final CurrentDetails current,
                final SessionManager sessionManager,
                final SecuritySystem securitySystem,
                final long maxUserTimeToLive,
                final long maxUserTimeToIdle) {
            factory = new ObjectFactory(ice_staticId()) {
                @Override
                public Ice.Object create(String name) {
                    return new UpdateSessionTimeoutRequestI(
                            current, sessionManager, securitySystem,
                            maxUserTimeToLive, maxUserTimeToIdle);
                }};
            }

        @Override
        public Map<String, ObjectFactory> createFactories(Communicator ic) {
            return new ImmutableMap.Builder<String, ObjectFactory>()
                    .put(ice_staticId(), factory).build();
        }
    }

    protected Helper helper;

    protected LocalQuery query;

    protected LocalUpdate update;

    protected final CurrentDetails current;

    protected final SessionManager manager;

    protected final SecuritySystem security;

    protected boolean updated = false;

    protected final long maxUserTimeToLive;

    protected final long maxUserTimeToIdle;

    public UpdateSessionTimeoutRequestI(CurrentDetails current,
            SessionManager manager, SecuritySystem security,
            long maxUserTimeToLive, long maxUserTimeToIdle) {
        this.current = current;
        this.manager = manager;
        this.security = security;
        this.maxUserTimeToLive = maxUserTimeToLive;
        this.maxUserTimeToIdle = maxUserTimeToIdle;
    }

    //
    // CMD API
    //

    @Override
    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        this.helper.setSteps(1);
        ServiceFactory sf = this.helper.getServiceFactory();
        query = (LocalQuery) sf.getQueryService();
        update = (LocalUpdate) sf.getUpdateService();
    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        return updateSession();
    }

    @Override
    public void finish() throws Cancel {
        // no-op
    }

    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (helper.isLast(step)) {
            manager.reload(session);
            helper.setResponseIfNull(new OK());
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    //
    // IMPLEMENTATION
    //

    protected Session updateSession() {
        Session s = helper.getServiceFactory().getQueryService()
                .findByQuery("select s from Session s where s.uuid = :uuid",
                new Parameters().addString("uuid", session));

        if (s == null) {
            // we assume that if the session is visible, then
            // the current user should be able to edit it.
            throw helper.cancel(new ERR(), null, "no-session");
        }

        boolean isAdmin = current.getCurrentEventContext().isCurrentUserAdmin();
        if (!isAdmin && maxUserTimeToLive != 0
                && timeToLive.getValue() > maxUserTimeToLive) {
            timeToLive = omero.rtypes.rlong(maxUserTimeToLive);
            helper.info("Attempt to modify timeToLive beyond maximum");
        }
        if (!isAdmin && timeToIdle.getValue() > maxUserTimeToIdle) {
            timeToIdle = omero.rtypes.rlong(maxUserTimeToIdle);
            helper.info("Attempt to modify timeToIdle beyond maximum");
        }
        updated |= updateField(s, Session.TIMETOLIVE, timeToLive, isAdmin);
        updated |= updateField(s, Session.TIMETOIDLE, timeToIdle, isAdmin);
        if (updated) {
            security.runAsAdmin(new AdminAction(){
                @Override
                public void runAsAdmin() {
                    update.flush();
                }});
            return s;
        } else {
            throw helper.cancel(new ERR(), null, "no-update-performed",
                    "session", session);
        }
    }

    protected boolean updateField(Session s, String field, RLong value,
            boolean isAdmin) {

        if (value == null) {
            return false;
        }

        long target = value.getValue();
        long current = ((Long) s.retrieve(field)).longValue();
        long diff = target - current;
        if (!isAdmin && diff != 0 && target <= 0) {
            throw helper.cancel(new ERR(), null, "non-admin-disabling",
                    "field", field,
                    "target", ""+target,
                    "current", ""+current);
        }

        helper.info("Modifying %s from %s to %s for %s",
                field, current, target, session);
        s.putAt(field, target);
        return true;
    }
}

/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.fire;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import ome.services.messages.DestroySessionMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.ChannelException;
import org.jgroups.ChannelFactory;
import org.jgroups.JChannelFactory;
import org.jgroups.View;
import org.jgroups.blocks.ReplicatedHashMap;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Uses the ReplicatedHashMap building block, which subclasses java.util.HashMap
 * and overrides the methods that modify the hashmap (e.g. put()). Those methods
 * are multicast to the group, whereas read-only methods such as get() use the
 * local copy. A ReplicatedtHashMap is created given the name of a group; all
 * hashmaps with the same name find each other and form a group.
 * 
 * @author Bela Ban
 * @version $Id: ReplicatedHashMapDemo.java,v 1.1 2007/07/23 09:27:51 belaban
 *          Exp $
 */
public class Ring implements ReplicatedHashMap.Notification<String, String>, ApplicationListener {

    private final static Log log = LogFactory.getLog(Ring.class);
    
    private final static String groupname = "Runtime"+Runtime.getRuntime().hashCode();
    
    ReplicatedHashMap<String, String> map = null;

    public Ring(ChannelFactory factory, String props, boolean persist)
            throws ChannelException {

        map = new ReplicatedHashMap<String, String>(groupname, factory, props,
                persist, 10000);
        map.addNotifier(this);
    }

    public Ring() throws ChannelException {
        this(new JChannelFactory(), "session_ring.xml", false);
    }

    // Our usage
    // =========================================================================
    
    public void put(String sessionUuid, String proxy) {
        map.put(sessionUuid, proxy);
    }
    
    public boolean containsKey(String sessionUuid) {
        return map.containsKey(sessionUuid);
    }
    
    public String get(String sessionUuid) {
        return map.get(sessionUuid);
    }
    
    public Set<String> values() {
        return new HashSet(map.values());
    }
    
    public int size() {
        return map.size();
    }
    
    // Events
    // =========================================================================
    
    public void onApplicationEvent(ApplicationEvent arg0) {
        if (arg0 instanceof DestroySessionMessage) {
            map.remove(((DestroySessionMessage) arg0).getSessionId());
        } else if (arg0 instanceof ContextClosedEvent) {
            log.info("Closing server with sessions:"+arg0);
        }
    }
    
    // Notification interface
    // =========================================================================

    public void contentsCleared() {
        log.info("Conents cleared.");
    }

    public void contentsSet(Map<String, String> arg0) {
        log.info("New contents: "+arg0);
    }

    public void entryRemoved(String arg0) {
        log.info("Entry removed: "+ arg0);
    }

    public void entrySet(String arg0, String arg1) {
        log.info("Entry added: "+ arg0);
    }

    public void viewChange(View arg0, Vector arg1, Vector arg2) {
        // Noop
    }
    
    // Main
    // =========================================================================
    
    public static void main(String[] args) throws Exception {
        Ring ring = new Ring();
        log.info(ring.map);
    }
    
}
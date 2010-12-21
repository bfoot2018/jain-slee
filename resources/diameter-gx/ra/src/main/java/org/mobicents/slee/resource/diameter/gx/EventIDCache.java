/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.slee.resource.diameter.gx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.slee.EventTypeID;
import javax.slee.facilities.EventLookupFacility;
import javax.slee.resource.FireableEventType;

import net.java.slee.resource.diameter.base.events.AbortSessionRequest;
import net.java.slee.resource.diameter.base.events.AccountingRequest;
import net.java.slee.resource.diameter.base.events.CapabilitiesExchangeRequest;
import net.java.slee.resource.diameter.base.events.DeviceWatchdogRequest;
import net.java.slee.resource.diameter.base.events.DisconnectPeerRequest;
import net.java.slee.resource.diameter.base.events.ReAuthRequest;
import net.java.slee.resource.diameter.base.events.SessionTerminationRequest;
import net.java.slee.resource.diameter.cca.events.CreditControlMessage;

import org.jdiameter.api.Message;

/**
 * Caches event IDs for the Diameter CCA RAs.
 *
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:carl-magnus.bjorkell@emblacom.com"> Carl-Magnus Björkell </a>
 */
public class EventIDCache {

    private static final String BASE_PACKAGE_PREFIX = "net.java.slee.resource.diameter.base.events.";
    private static final String GX_PACKAGE_PREFIX = "net.java.slee.resource.diameter.gx.events.";
    public static Map<Integer, String> eventNames = new ConcurrentHashMap<Integer, String>();

    static {
        final Map<Integer, String> eventsTemp = new HashMap<Integer, String>();

        eventsTemp.put(CreditControlMessage.commandCode, GX_PACKAGE_PREFIX + "GxCreditControl");

        // We support Base messages too, just in case...
        eventsTemp.put(AbortSessionRequest.commandCode, BASE_PACKAGE_PREFIX + "AbortSession");
        eventsTemp.put(AccountingRequest.commandCode, BASE_PACKAGE_PREFIX + "Accounting");
        eventsTemp.put(CapabilitiesExchangeRequest.commandCode, BASE_PACKAGE_PREFIX + "CapabilitiesExchange");
        eventsTemp.put(DeviceWatchdogRequest.commandCode, BASE_PACKAGE_PREFIX + "DeviceWatchdog");
        eventsTemp.put(DisconnectPeerRequest.commandCode, BASE_PACKAGE_PREFIX + "DisconnectPeer");
        eventsTemp.put(ReAuthRequest.commandCode, BASE_PACKAGE_PREFIX + "ReAuth");
        eventsTemp.put(SessionTerminationRequest.commandCode, BASE_PACKAGE_PREFIX + "SessionTermination");

        eventNames = Collections.unmodifiableMap(eventsTemp);
    }
    public static final String ERROR_ANSWER = BASE_PACKAGE_PREFIX + "ErrorAnswer";
    public static final String EXTENSION_DIAMETER_MESSAGE = BASE_PACKAGE_PREFIX + "ExtensionDiameterMessage";
    private static final String VENDOR = "java.net";
    private static final String VERSION = "0.8";
    private ConcurrentHashMap<String, FireableEventType> eventIds = new ConcurrentHashMap<String, FireableEventType>();

    public EventIDCache() {
    }

    /**
     *
     * @param eventLookupFacility
     * @param message
     * @return
     */
    public FireableEventType getEventId(final EventLookupFacility eventLookupFacility, final Message message) {

        FireableEventType eventID;

        // Error is always the same.
        if (message.isError()) {
            eventID = getEventId(eventLookupFacility, ERROR_ANSWER);
        } else {
            final int commandCode = message.getCommandCode();
            final boolean isRequest = message.isRequest();

            final String eventName = eventNames.get(commandCode);

            if (eventName != null) {
                eventID = getEventId(eventLookupFacility, eventName + (isRequest ? "Request" : "Answer"));
            } else {
                eventID = getEventId(eventLookupFacility, EXTENSION_DIAMETER_MESSAGE);
            }
        }

        return eventID;
    }

    /**
     *
     * @param eventLookupFacility
     * @param eventName
     * @return
     */
    private FireableEventType getEventId(final EventLookupFacility eventLookupFacility, final String eventName) {

        FireableEventType eventType = eventIds.get(eventName);
        if (eventType == null) {
            try {
                eventType = eventLookupFacility.getFireableEventType(new EventTypeID(eventName, VENDOR, VERSION));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (eventType != null) {
                eventIds.put(eventName, eventType);
            }
        }
        return eventType;
    }
}
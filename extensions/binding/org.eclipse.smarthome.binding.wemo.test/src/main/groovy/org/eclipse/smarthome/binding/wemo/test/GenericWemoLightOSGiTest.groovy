/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.wemo.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import javax.servlet.http.HttpServlet

import org.eclipse.smarthome.binding.wemo.WemoBindingConstants
import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.Channel
import org.eclipse.smarthome.core.thing.ChannelUID
import org.eclipse.smarthome.core.thing.Thing
import org.eclipse.smarthome.core.thing.ThingStatus
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder
import org.eclipse.smarthome.core.thing.type.ChannelKind
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant

/**
 * Generic test class for all WemoLight related tests that contains methods and constants used across the different test classes
 *
 * @author Svilen Valkanov - Initial contribution
 */
class GenericWemoLightOSGiTest extends GenericWemoOSGiTest {

    // Thing information
    def THING_TYPE_UID = WemoBindingConstants.THING_TYPE_MZ100
    def BRIDGE_TYPE_UID = WemoBindingConstants.THING_TYPE_BRIDGE
    def WEMO_LIGHT_ID = THING_TYPE_UID.getId()
    def WEMO_BRIDGE_ID = BRIDGE_TYPE_UID.getId()
    def DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_STATE
    def DEFAULT_TEST_CHANNEL_TYPE = "Switch"

    // UPnP service information
    def BRIDGE_MODEL_NAME = WEMO_BRIDGE_ID
    def DEVICE_MODEL_NAME = WEMO_LIGHT_ID
    def SERVICE_ID = 'bridge'
    def SERVICE_NUMBER = '1'
    def SERVLET_URL = "${DEVICE_CONTROL_PATH}${SERVICE_ID}${SERVICE_NUMBER}"

    Bridge bridge;
    HttpServlet servlet;

    protected Bridge createBridge(ThingTypeUID bridgeTypeUID) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.UDN, DEVICE_UDN)

        ThingUID bridgeUID = new ThingUID(bridgeTypeUID, WEMO_BRIDGE_ID);

        bridge = BridgeBuilder.create(bridgeTypeUID, bridgeUID)
                .withConfiguration(configuration)
                .build();

        managedThingProvider.add(bridge)
        return bridge;
    }

    protected Thing createDefaultThing(ThingTypeUID thingTypeUID) {
        return createThing(thingTypeUID, DEFAULT_TEST_CHANNEL, DEFAULT_TEST_CHANNEL_TYPE)
    }

    @Override
    protected Thing createThing(ThingTypeUID thingTypeUID, String channelID, String itemAcceptedType) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.DEVICE_ID, WEMO_LIGHT_ID)

        ThingUID thingUID = new ThingUID(thingTypeUID, TEST_THING_ID);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID)
        Channel channel = ChannelBuilder.create(channelUID, itemAcceptedType).withType(DEFAULT_CHANNEL_TYPE_UID).withKind(ChannelKind.STATE).withLabel("label").build();
        ThingUID bridgeUID = new ThingUID(BRIDGE_TYPE_UID, WEMO_BRIDGE_ID);

        thing = ThingBuilder.create(thingTypeUID, thingUID)
                .withConfiguration(configuration)
                .withChannel(channel)
                .withBridge(bridgeUID)
                .build();

        managedThingProvider.add(thing)

        createItem(channelUID,DEFAULT_TEST_ITEM_NAME,itemAcceptedType)
        return thing;
    }


    protected void removeThing() {
        if(thing != null) {
            Thing removedThing = thingRegistry.remove(thing.getUID())
            assertThat(removedThing, is(notNullValue()))
        }

        waitForAssert {
            assertThat thing.getStatus(), is(ThingStatus.UNINITIALIZED)
        }

        if(bridge != null) {
            Bridge bridgeThing = thingRegistry.remove(bridge.getUID())
            assertThat bridgeThing, is(notNullValue())
        }

        waitForAssert {
            assertThat bridge.getStatus(), is(ThingStatus.UNINITIALIZED)
        }

        waitForAssert {
            Set<UpnpIOParticipant> participants  = upnpIOService.participants;
            assertThat participants.size(), is(0)
        }

        itemRegistry.remove(DEFAULT_TEST_ITEM_NAME)
        waitForAssert {
            assertThat itemRegistry.getAll().size(), is(0)
        }
    }
}

/*
 * Copyright (C) 2024 adminavvimpa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mainardisoluzioni.labellinkbuddy;

import static com.mainardisoluzioni.labellinkbuddy.PyPanda.IP_ADDRESS;
import static com.mainardisoluzioni.labellinkbuddy.PyPanda.NAMESPACE_INDEX;
import static com.mainardisoluzioni.labellinkbuddy.PyPanda.TCP_PORT;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

/**
 *
 * @author adminavvimpa
 */
public class HeartbeatPlc {
    private OpcUaClient client;
    
    private static final int HEARTBEAT_NODE_IDENTIFIER = 9;
    
    public void sendHeartbeat() throws InterruptedException, ExecutionException, UaException {
        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints("opc.tcp://" + IP_ADDRESS +":" + String.valueOf(TCP_PORT)).get();
        EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(0), IP_ADDRESS, TCP_PORT);

        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(configPoint)
                .setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                .setApplicationUri("urn:eclipse:milo:examples:client")
                .setRequestTimeout(uint(5000));

        client = OpcUaClient.create(cfg.build());
        
        client.connect().get();
        
        List<WriteValue> writeValues = new ArrayList<>();
        writeValues.add(
                new WriteValue(
                        new NodeId(NAMESPACE_INDEX, HEARTBEAT_NODE_IDENTIFIER),
                        AttributeId.Value.uid(),
                        null, // indexRange
                        DataValue.valueOnly(new Variant(true))
                )
        );
        client.write(writeValues).get();
        
        if (client != null)
            client.disconnect().get();
    }
}

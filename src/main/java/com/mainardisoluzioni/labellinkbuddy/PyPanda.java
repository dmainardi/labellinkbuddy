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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

/**
 *
 * @author adminavvimpa
 */
public class PyPanda {
    public static final String IP_ADDRESS = "192.168.5.1";
    public static final int TCP_PORT = 4840;
    
    private OpcUaClient client;
    
    private final LabelLinkBuddy instance;

    public PyPanda(String nomeEtichettatrice) {
        instance = new LabelLinkBuddy(nomeEtichettatrice);
    }
    
    public void createClientAndWaitForPrint() throws UaException, InterruptedException, ExecutionException {
        List<EndpointDescription> endpoints = DiscoveryClient.getEndpoints("opc.tcp://" + IP_ADDRESS +":" + String.valueOf(TCP_PORT)).get();
        EndpointDescription configPoint = EndpointUtil.updateUrl(endpoints.get(0), IP_ADDRESS, TCP_PORT);

        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        cfg.setEndpoint(configPoint)
                .setApplicationName(LocalizedText.english("eclipse milo opc-ua client"))
                .setApplicationUri("urn:eclipse:milo:examples:client")
                .setRequestTimeout(uint(5000));

        client = OpcUaClient.create(cfg.build());
        
        client.connect().get();
        /*for (int i = 3; i < 7; i++) {
            System.out.println("4, " + i +": " + client.getAddressSpace().getVariableNode(new NodeId(4, i)).getValue().getValue().getValue());
        }*/

        // what to read
        ReadValueId readValueId = new ReadValueId(new NodeId(4, 6), AttributeId.Value.uid(), null, null);

        // monitoring parameters
        int clientHandle = 123456789;
        MonitoringParameters parameters = new MonitoringParameters(uint(clientHandle), 1000.0, null, uint(10), true);

        // creation request
        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

        // The actual consumer
        //Consumer<DataValue> consumerMaina = (t) -> System.out.println("Valore: " + t);
        Consumer<DataValue> consumerMaina = (t) -> {
            Boolean stampare = (Boolean) t.getValue().getValue();
            System.out.println("Valore: " + stampare);
            if (stampare) {
                instance.stampaEtichettaEControllaCodiceABarre();
            }
        };

        // setting the consumer after the subscription creation
        UaSubscription.ItemCreationCallback maina = (monitoredItem, id) -> monitoredItem.setValueConsumer(consumerMaina);

        // creating the subscription
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

        List<UaMonitoredItem> items = subscription.createMonitoredItems(
            TimestampsToReturn.Both,
            Arrays.asList(request),
            maina)
          .get();

        Thread.sleep(120000);
        
        disconnect();
    }
    
    private void disconnect() throws InterruptedException, ExecutionException {
        if (client != null)
            client.disconnect().get();
    }
}

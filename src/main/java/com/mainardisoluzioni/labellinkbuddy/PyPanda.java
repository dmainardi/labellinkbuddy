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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteResponse;
import org.eclipse.milo.opcua.stack.core.types.structured.WriteValue;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;

/**
 *
 * @author adminavvimpa
 */
public class PyPanda {
    public static final String IP_ADDRESS = "192.168.5.1";
    public static final int TCP_PORT = 4840;
    
    private static final int NAMESPACE_INDEX = 4;
    private static final int PROCESS_ENDED_NODE_IDENTIFIER = 6;
    private static final int WRITE_NODE_IDENTIFIER = 7;
    private static final int IDENTIFICATIVO_NODE_IDENTIFIER = 8;
    
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
        ReadValueId readValueId = new ReadValueId(new NodeId(NAMESPACE_INDEX, PROCESS_ENDED_NODE_IDENTIFIER), AttributeId.Value.uid(), null, null);

        // monitoring parameters
        int clientHandle = 123456789;
        MonitoringParameters parameters = new MonitoringParameters(uint(clientHandle), 1000.0, null, uint(10), true);

        // creation request
        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, parameters);

        // The actual consumer
        //Consumer<DataValue> consumerMaina = (t) -> System.out.println("Valore: " + t);
        Consumer<DataValue> consumerStampaEtichettaEControllaCodiceABarre = new Consumer<DataValue>() {
            @Override
            public void accept(DataValue t) {
                Boolean stampare = (Boolean) t.getValue().getValue();
                System.out.println("Processo terminato: " + stampare);
                if (stampare) {
                    try {
                        EsitoControlloCodiceABarre esito;
                        try {
                            String identificativo = String.valueOf(client.getAddressSpace().getVariableNode(new NodeId(NAMESPACE_INDEX, IDENTIFICATIVO_NODE_IDENTIFIER)).readValue().getValue().getValue());
                            esito = instance.stampaEtichettaEControllaCodiceABarre(identificativo);
                        } catch (UaException ex) {
                            esito = EsitoControlloCodiceABarre.ERRORE_IDENTIFICATIVO_VUOTO;
                        }
                        WriteResponse esitoScritturaSuPlc = inviaEsitoAlPlc(esito);
                        System.out.println("Esito scrittura su PLC: " + esitoScritturaSuPlc.getResponseHeader().getServiceResult().isGood());
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(PyPanda.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        // setting the consumer after the subscription creation
        UaSubscription.ItemCreationCallback callbackStampaEtichettaEControllaCodiceABarre = (monitoredItem, id) -> monitoredItem.setValueConsumer(consumerStampaEtichettaEControllaCodiceABarre);

        // creating the subscription
        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();

        List<UaMonitoredItem> items = subscription.createMonitoredItems(
            TimestampsToReturn.Both,
            Arrays.asList(request),
            callbackStampaEtichettaEControllaCodiceABarre)
          .get();

        Thread.sleep(60000);
        
        disconnect();
    }
    
    private WriteResponse inviaEsitoAlPlc(EsitoControlloCodiceABarre esito) throws InterruptedException, ExecutionException {
        List<WriteValue> writeValues = new ArrayList<>();
        writeValues.add(
                new WriteValue(
                        new NodeId(NAMESPACE_INDEX, WRITE_NODE_IDENTIFIER),
                        AttributeId.Value.uid(),
                        null, // indexRange
                        DataValue.valueOnly(new Variant(esito.getValue()))
                )
        );
        return client.write(writeValues).get();
        
        /*CompletableFuture<StatusCode> f = client.writeValue(
                new NodeId(NAMESPACE_INDEX, WRITE_NODE_IDENTIFIER),
                new DataValue(new Variant(esito.getValue()), null, null, null)
        );
        
        return f.get();*/
    }
    
    private void disconnect() throws InterruptedException, ExecutionException {
        if (client != null)
            client.disconnect().get();
    }
}

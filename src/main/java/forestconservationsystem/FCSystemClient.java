/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import generated.grpc.monitoralertservice.MonitorAlertServiceGrpc;
import generated.grpc.monitoralertservice.MonitorAlertServiceGrpc.MonitorAlertServiceStub;
import generated.grpc.monitoralertservice.MonitorAlertServiceGrpc.MonitorAlertServiceBlockingStub;
import generated.grpc.monitoralertservice.SensorReading;
import generated.grpc.monitoralertservice.SensorReading.SensorType;
import generated.grpc.monitoralertservice.AverageData;
import generated.grpc.monitoralertservice.FireAlert;
import generated.grpc.animaltrackerservice.AnimalTrackerServiceGrpc;
import generated.grpc.animaltrackerservice.AnimalTrackerServiceGrpc.AnimalTrackerServiceStub;
import generated.grpc.animaltrackerservice.TrackingRequest;
import generated.grpc.animaltrackerservice.LocationUpdate;
import generated.grpc.rangercoordinatorservice.RangerCoordinatorServiceGrpc;
import generated.grpc.rangercoordinatorservice.RangerCoordinatorServiceGrpc.RangerCoordinatorServiceStub;
import generated.grpc.rangercoordinatorservice.RangerCommand;
import generated.grpc.rangercoordinatorservice.RangerStatus;
import generated.grpc.rangercoordinatorservice.CommandType;
import io.grpc.stub.StreamObserver;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.net.InetAddress;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

/**
 *
 * @author Dong
 */
public class FCSystemClient {
    private static final Logger logger = Logger.getLogger(FCSystemClient.class.getName());
    static JmDNS jmdns;
    
    public static void main(String[] args) throws Exception {
        logger.setLevel(Level.SEVERE);
        Logger.getLogger("io.grpc.netty").setLevel(Level.SEVERE);
        Logger.getLogger("io.grpc.netty.shaded").setLevel(Level.SEVERE);
        Logger.getLogger("javax.jmdns").setLevel(Level.SEVERE);
        
        // TODO change to InetAddress.getLocalHost()
        // jmdns = JmDNS.create(InetAddress.getLocalHost());
        InetAddress localAddr = InetAddress.getByName("192.168.1.18");
        jmdns = JmDNS.create(localAddr);
        
        String serviceType = "_grpc._tcp.local.";
        // Add a service listener
        jmdns.addServiceListener(serviceType, new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                System.out.println("[+] Service added: " + event.getName());
                // This triggers serviceResolved if we explicitly request resolution
                jmdns.requestServiceInfo(event.getType(), event.getName(), 1); // resolve ASAP
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                System.out.println("[-] Service removed: " + event.getName());
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                ServiceInfo serviceInfo = event.getInfo();
                System.out.println("##### Service resolved: " + serviceInfo.getName());
                System.out.println("    Address: " + serviceInfo.getHostAddresses()[0]);
                System.out.println("    Port: " + serviceInfo.getPort());
                
                // get the port number and host name from the ServiceInfo object
                String discoveredHost = serviceInfo.getHostAddresses()[0];
                int discoveredPort = serviceInfo.getPort();
                String serviceName = serviceInfo.getName();
                
                ManagedChannel channel = ManagedChannelBuilder
                        .forAddress(discoveredHost, discoveredPort)
                        .usePlaintext()
                        .build();
                
                try {
                    // now that the service is resolved we can use it
                    // check that it is the specific service we want
                    if (serviceName.equals("ForestConservationSystem")) {
                        useMonitorAlertService(channel);
                        useAnimalTrackerService(channel);
                        useRangerCoordinatorService(channel);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(FCSystemClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(FCSystemClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });

        System.out.println("#######Listening for gRPC services via JmDNS...");
        Thread.sleep(30000);
    }
    
    private static void useMonitorAlertService(ManagedChannel channel) throws InterruptedException, IOException {
        /** Client Streaming
         * streaming sensor reading data to server and get average data
         */
        MonitorAlertServiceStub asyncStub = MonitorAlertServiceGrpc.newStub(channel); // non-blocking stub for client streaming
        StreamObserver<AverageData> responseObserver = new StreamObserver<AverageData>() {
            /** TODO
             * NOTE that in client streaming we expect only one response from the server.So we should see
             * this message only once. We could add some error handling in here to prevent the client from processing
             * more than one reply from the server
             */
            @Override
            public void onNext(AverageData averageData) {
                System.out.println("################### response from server " + averageData.toString());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("################## MonitorAlertService client stream is completed.");
            }
        };
        StreamObserver<SensorReading> requestObserver = asyncStub.streamSensorData(responseObserver);

        // TEMPERATURE
        requestObserver.onNext(SensorReading.newBuilder().setSensorId("TEMP-01").setType(SensorType.TEMPERATURE).setValue(19).build());
        // here the client sleeps for a bit between each request to slow things down so we can see whats happening
        Thread.sleep(500);
        requestObserver.onNext(SensorReading.newBuilder().setSensorId("TEMP-02").setType(SensorType.TEMPERATURE).setValue(20).build());
        Thread.sleep(500);

        // HUMIDITY
        requestObserver.onNext(SensorReading.newBuilder().setSensorId("HUMI-01").setType(SensorType.HUMIDITY).setValue(70).build());
        Thread.sleep(500);
        requestObserver.onNext(SensorReading.newBuilder().setSensorId("HUMI-02").setType(SensorType.HUMIDITY).setValue(72).build());
        Thread.sleep(500);

        // CO2
        requestObserver.onNext(SensorReading.newBuilder().setSensorId("CO2-01").setType(SensorType.CO2).setValue(400).build());
        Thread.sleep(500);
        requestObserver.onNext(SensorReading.newBuilder().setSensorId("CO2-02").setType(SensorType.CO2).setValue(420).build());
        Thread.sleep(500);

        requestObserver.onCompleted();
        // if the client sleeps now then it will see the server response when it wakes
        Thread.sleep(10000);

        
        /** Unary
         * send average data to server and get fire risk level
         */
        MonitorAlertServiceBlockingStub blockingStub = MonitorAlertServiceGrpc.newBlockingStub(channel); // blocking stub for unary
        
        AverageData averageData = AverageData.newBuilder()
                .setAvgTemp(19.5f)
                .setAvgHumi(71)
                .setAvgCo2(410)
                .build();

        FireAlert fireAlert = blockingStub.checkFireRisk(averageData);
        System.out.println("############# Fire risk level: " + fireAlert.getLevel());
    }
    
    private static void useAnimalTrackerService(ManagedChannel channel) throws InterruptedException, IOException {
        /** Server Streaming
         * server pushes location updates of a request animal
         */
        AnimalTrackerServiceStub asyncStub = AnimalTrackerServiceGrpc.newStub(channel); // non-blocking stub for server streaming
        TrackingRequest trackingRequest = TrackingRequest.newBuilder()
                .setAnimalId("TIGER_123")
                .setUpdateInterval(2)
                .build();

        ArrayList<LocationUpdate> locationUpdates = new ArrayList<>();

        StreamObserver<LocationUpdate> responseObserver = new StreamObserver<LocationUpdate> () {
            @Override
            public void onNext(LocationUpdate locationUpdate) {
                System.out.println("################## Client received locationUpdates: " + locationUpdate.toString());
                locationUpdates.add(locationUpdate);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("#################### Error requesting: " + t.getLocalizedMessage());
            }

            @Override
            public void onCompleted() {
                // TODO ???
                System.out.println("################## AnimalTrackerService Client received onCompleted");
            }
        };

        asyncStub.streamAnimalLocations(trackingRequest, responseObserver);
    }
    
    private static void useRangerCoordinatorService(ManagedChannel channel) throws InterruptedException, IOException {
        /** Bi-directional Streaming
         * Real-time command/status exchange
         */
        
        RangerCoordinatorServiceStub asyncStub = RangerCoordinatorServiceGrpc.newStub(channel); // non-blocking stub for bi-directional streaming
        
        StreamObserver<RangerStatus> responseObserver = new StreamObserver<RangerStatus>() {
            @Override
            public void onNext(RangerStatus rangerStatus) {
                System.out.println("############## client received rangerStatus ranger_id: " + rangerStatus.getRangerId() + " status: " + rangerStatus.getStatus() + " findings: " + rangerStatus.getFindingsList());
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("############# receiving rangerStatus completed ");
            }
        };
        
        StreamObserver<RangerCommand> requestObserver = asyncStub.coordinateRangers(responseObserver);
        
        try {
            requestObserver.onNext(RangerCommand.newBuilder().setRangerId("RG-11").setAction(CommandType.SCAN_AREA).build());
            Thread.sleep(500);
            requestObserver.onNext(RangerCommand.newBuilder().setRangerId("RG-12").setAction(CommandType.SCAN_AREA).build());
            Thread.sleep(500);
            requestObserver.onNext(RangerCommand.newBuilder().setRangerId("RG-13").setAction(CommandType.SCAN_AREA).build());
            Thread.sleep(500);
            requestObserver.onNext(RangerCommand.newBuilder().setRangerId("RG-14").setAction(CommandType.SCAN_AREA).build());
            Thread.sleep(500);
            requestObserver.onCompleted();
            // if the client sleeps now then it will see the server response when it wakes
            Thread.sleep(10000);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {			
            e.printStackTrace();
        }

    }
}

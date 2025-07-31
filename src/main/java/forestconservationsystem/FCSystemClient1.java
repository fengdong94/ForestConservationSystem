/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
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
public class FCSystemClient1 {
    static JmDNS jmdns;
    private static MonitorAlertServiceStub monitorAlertServiceStub;
    private static MonitorAlertServiceBlockingStub monitorAlertServiceBlockingStub;
    private static AnimalTrackerServiceStub animalTrackerServiceStub;
    private static CountDownLatch latch = new CountDownLatch(1);
    
    /**
     * create and listen to JmDNS, set up the channel and the stubs.
     * @throws java.lang.Exception
     */
    public FCSystemClient1() throws Exception {
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
                

                // now that the service is resolved we can use it
                // check that it is the specific service we want
                if (serviceName.equals("ForestConservationSystem")) {
                    monitorAlertServiceStub = MonitorAlertServiceGrpc.newStub(channel);
                    monitorAlertServiceBlockingStub = MonitorAlertServiceGrpc.newBlockingStub(channel);
                    animalTrackerServiceStub = AnimalTrackerServiceGrpc.newStub(channel);
                    latch.countDown(); // service is resolved
                }
            }
        });
    }
    
    /**
     * Use the main method to test that the client calls are working. When using
     * the GUI we do not need to run the client as a separate process. The GUI
     * will make an instance of the client and use it.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        FCSystemClient1 fcSystemClient = new FCSystemClient1();
        latch.await(); // wait until service is resolved, otherwise all stubs are null
        fcSystemClient.checkFireRisk(19.5f, 71, 410);
//        fcSystemClient.streamSensorData();
    }
    
    public FireAlert checkFireRisk(float avgTemp, float avgHumi, float avgCo2) {
        AverageData averageData = AverageData.newBuilder()
                .setAvgTemp(avgTemp)
                .setAvgHumi(avgHumi)
                .setAvgCo2(avgCo2)
                .build();

        FireAlert fireAlert = monitorAlertServiceBlockingStub.checkFireRisk(averageData);
        System.out.println("#################### checkFireRisk response: Fire risk level: " + fireAlert.getLevel());
        return fireAlert;
    }
    
    public StreamObserver<SensorReading> streamSensorData(StreamObserver<AverageData> responseObserver) {
        StreamObserver<SensorReading> requestObserver = monitorAlertServiceStub.streamSensorData(responseObserver);
        return requestObserver;
    }
    
    public void streamAnimalLocations(StreamObserver<LocationUpdate> responseObserver, String animalId, int updateInterval) {
        TrackingRequest trackingRequest = TrackingRequest.newBuilder()
                .setAnimalId(animalId)
                .setUpdateInterval(updateInterval)
                .build();
                
        animalTrackerServiceStub.streamAnimalLocations(trackingRequest, responseObserver);
    }
}

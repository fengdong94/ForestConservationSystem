/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import generated.grpc.monitoralertservice.MonitorAlertServiceGrpc;
import generated.grpc.monitoralertservice.MonitorAlertServiceGrpc.MonitorAlertServiceStub;
import generated.grpc.monitoralertservice.MonitorAlertServiceGrpc.MonitorAlertServiceBlockingStub;
import generated.grpc.monitoralertservice.SensorReading;
import generated.grpc.monitoralertservice.AverageData;
import generated.grpc.monitoralertservice.FireAlert;
import generated.grpc.animaltrackerservice.AnimalTrackerServiceGrpc;
import generated.grpc.animaltrackerservice.TrackingRequest;
import generated.grpc.animaltrackerservice.LocationUpdate;
import generated.grpc.rangercoordinatorservice.RangerCoordinatorServiceGrpc;
import generated.grpc.rangercoordinatorservice.RangerCoordinatorServiceGrpc.RangerCoordinatorServiceStub;
import generated.grpc.rangercoordinatorservice.RangerCommand;
import generated.grpc.rangercoordinatorservice.RangerStatus;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
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
    static JmDNS jmdns;
    ManagedChannel channel;
    private static MonitorAlertServiceStub monitorAlertServiceStub;
    private static MonitorAlertServiceBlockingStub monitorAlertServiceBlockingStub;
    private static RangerCoordinatorServiceStub rangerCoordinatorServiceStub;
    private static CountDownLatch latch = new CountDownLatch(1);
    
    /**
     * create and listen to JmDNS, set up the channel and the stubs.
     * @throws java.lang.Exception
     */
    public FCSystemClient() throws Exception {
        jmdns = JmDNS.create(InetAddress.getLocalHost());
        // for macOS
//        InetAddress localAddr = InetAddress.getByName("192.168.1.18");
//        jmdns = JmDNS.create(localAddr);
        
        // Add a service listener
        jmdns.addServiceListener(Utils.SERVICE_TYPE, new ServiceListener() {
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
                
                channel = ManagedChannelBuilder
                        .forAddress(discoveredHost, discoveredPort)
                        .usePlaintext()
                        .build();
                
                // now that the service is resolved we can use it
                // check that it is the specific service we want
                if (serviceName.equals(Utils.SERVICE_NAME)) {
                    monitorAlertServiceStub = MonitorAlertServiceGrpc.newStub(channel);
                    monitorAlertServiceBlockingStub = MonitorAlertServiceGrpc.newBlockingStub(channel);
                    rangerCoordinatorServiceStub = RangerCoordinatorServiceGrpc.newStub(channel);
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
        FCSystemClient fcSystemClient = new FCSystemClient();
        latch.await(); // wait until service is resolved, otherwise all stubs are null
        fcSystemClient.checkFireRisk(19.5f, 71, 410);
//        Now just checkFireRisk is working if you run this client only.
//        For other helper methods to work, we need to create some StreamObserver and pass in corresponding parameters.
    }
    
    /**
     * 
     *
     * All methods below are helper methods to help GUI connect with services.
     *
     * 
     */
    
    // Unary: single request -> single response
    // rpc CheckFireRisk(AverageData) returns (FireAlert);
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
    
    // Client-streaming: Sensors continuously send data
    // rpc StreamSensorData(stream SensorReading) returns (AverageData);
    public StreamObserver<SensorReading> streamSensorData(StreamObserver<AverageData> responseObserver) {
        StreamObserver<SensorReading> requestObserver = monitorAlertServiceStub.streamSensorData(responseObserver);
        return requestObserver;
    }
    
    // In this method, use ClientCall to implement Cancelling of messages
    // Server-streaming: Server pushes location updates
    // rpc StreamAnimalLocations(TrackingRequest) returns (stream LocationUpdate);
    public ClientCall<TrackingRequest, LocationUpdate> streamAnimalLocations() {
        MethodDescriptor<TrackingRequest, LocationUpdate> method = AnimalTrackerServiceGrpc.getStreamAnimalLocationsMethod();
        // Deadlines 20 seconds
        CallOptions callOptions = CallOptions.DEFAULT.withDeadlineAfter(20, TimeUnit.SECONDS);
        ClientCall<TrackingRequest, LocationUpdate> call = channel.newCall(method, callOptions);
        return call;
    }
    
    // Bidirectional streaming: Real-time command/status exchange
    // rpc CoordinateRangers(stream RangerCommand) returns (stream RangerStatus);
    public StreamObserver<RangerCommand> coordinateRangers(StreamObserver<RangerStatus> responseObserver) throws InterruptedException {
        StreamObserver<RangerCommand> requestObserver = rangerCoordinatorServiceStub.coordinateRangers(responseObserver);
        return requestObserver;
    }
}

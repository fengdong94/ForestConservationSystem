/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import java.io.IOException;
import java.util.logging.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.util.logging.Level;

/**
 *
 * @author Dong
 */
public class FCSystemServer {
    private static final Logger logger = Logger.getLogger(FCSystemServer.class.getName());

    public static void main(String[] args) {
        MonitorAlertService monitorAlertService = new MonitorAlertService();
        AnimalTrackerService animalTrackerService = new AnimalTrackerService();
        RangerCoordinatorService rangerCoordinatorService = new RangerCoordinatorService();

        Logger.getLogger("io.grpc.netty").setLevel(Level.SEVERE);
        Logger.getLogger("io.grpc.netty.shaded").setLevel(Level.SEVERE);
        Logger.getLogger("javax.jmdns").setLevel(Level.SEVERE);
        logger.setLevel(Level.SEVERE);
        
        int port = 50051;
        
        try {
            Server server = ServerBuilder.forPort(port)
                    .addService(monitorAlertService)
                    .addService(animalTrackerService)
                    .addService(rangerCoordinatorService)
                    .build()
                    .start();

            logger.info("Server started, listening on " + port);
            System.out.println("#####Server started, listening on" + port);
            
            // register the service to jmDNS 
            ServiceRegistration sr = ServiceRegistration.getInstance();
            sr.registerService( "_grpc._tcp.local.", "ForestConservationSystem", port, "");

            server.awaitTermination();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

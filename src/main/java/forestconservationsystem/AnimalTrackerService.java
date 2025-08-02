/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import generated.grpc.animaltrackerservice.AnimalTrackerServiceGrpc.AnimalTrackerServiceImplBase;
import generated.grpc.animaltrackerservice.TrackingRequest;
import generated.grpc.animaltrackerservice.LocationUpdate;
import io.grpc.stub.StreamObserver;

/**
 *
 * @author Dong
 */
public class AnimalTrackerService extends AnimalTrackerServiceImplBase {
    @Override
    public void streamAnimalLocations(TrackingRequest request, StreamObserver<LocationUpdate> responseObserver) {
        int updateInterval = request.getUpdateInterval();
        Timer timer = new Timer();
        
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // generate longitude, latitude and timestamp for each response
                double[] location = generateRandomLocation();
                LocationUpdate locationUpdate = LocationUpdate.newBuilder()
                        .setLon(location[0])
                        .setLat(location[1])
                        .setTimestamp(generateTimestamp())
                        .build();
                
                try {
                    responseObserver.onNext(locationUpdate);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        };
        
        // stream the locationUpdate data every updateInterval seconds
        timer.scheduleAtFixedRate(task, 0, updateInterval * 1000);
    }
    
    public static double[] generateRandomLocation() {
        // generate a random location relatively nearby a center
        double centerLon = 121.4737;
        double centerLat = 31.2304;
        
        Random rand = new Random();
        double randomLon = centerLon + rand.nextDouble() / 100;
        double randomLat = centerLat + rand.nextDouble() / 100;

        // Round to four decimal places
        return new double[]{Math.round(randomLon * 10000) / 10000.0, Math.round(randomLat * 10000) / 10000.0};
    }
    
    public static String generateTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}

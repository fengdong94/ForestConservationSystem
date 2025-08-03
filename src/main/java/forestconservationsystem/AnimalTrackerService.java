/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import generated.grpc.animaltrackerservice.AnimalTrackerServiceGrpc.AnimalTrackerServiceImplBase;
import generated.grpc.animaltrackerservice.TrackingRequest;
import generated.grpc.animaltrackerservice.LocationUpdate;
import io.grpc.stub.StreamObserver;
import io.grpc.Context;
import io.grpc.Context.CancellationListener;
import com.google.common.util.concurrent.MoreExecutors;

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
                // generate a random location relatively nearby a center
                double[] location = Utils.generateRandomLocation(121.4737, 31.2304);
                LocationUpdate locationUpdate = LocationUpdate.newBuilder()
                        .setLon(location[0])
                        .setLat(location[1])
                        .setTimestamp(generateTimestamp())
                        .build();
                
                try {
                    responseObserver.onNext(locationUpdate);
                } catch (RuntimeException e) {
                    timer.cancel();
                    e.printStackTrace();
                }
            }
        };
        
        // stream the locationUpdate data every updateInterval seconds
        timer.scheduleAtFixedRate(task, 0, updateInterval * 1000);
        
        // when client cancel calls, cancel the timer
        Context.current().addListener(new CancellationListener() {
            @Override
            public void cancelled(Context context) {
                timer.cancel();
            }
        }, MoreExecutors.directExecutor());
    }
    
    public static String generateTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
}

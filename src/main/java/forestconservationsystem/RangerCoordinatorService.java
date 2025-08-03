/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import java.util.*;
import generated.grpc.rangercoordinatorservice.RangerCoordinatorServiceGrpc.RangerCoordinatorServiceImplBase;
import generated.grpc.rangercoordinatorservice.RangerCommand;
import generated.grpc.rangercoordinatorservice.CommandType;
import generated.grpc.rangercoordinatorservice.RangerStatus;
import generated.grpc.rangercoordinatorservice.StatusType;
import io.grpc.stub.StreamObserver;

/**
 *
 * @author Dong
 */
public class RangerCoordinatorService extends RangerCoordinatorServiceImplBase {
    @Override
    public StreamObserver<RangerCommand> coordinateRangers(StreamObserver<RangerStatus> responseObserver) {
        return new StreamObserver<RangerCommand>() {
            @Override
            public void onNext(RangerCommand rangerCommand) {
                System.out.println("################### server received coordinateRangers rangerCommand: " + rangerCommand.toString());

                CommandType action = rangerCommand.getAction();
                // generate status according to action
                StatusType status = StatusType.IDLE;
                if (action.equals(CommandType.MOVE_TO)) {
                    status = StatusType.MOVING;
                }
                if (action.equals(CommandType.SCAN_AREA)) {
                    status = StatusType.SCANNING;
                }
                
                // generate longitude, latitude nearby the destination longitude and latitude
                double[] location = Utils.generateRandomLocation(rangerCommand.getLon(), rangerCommand.getLat());
                
                RangerStatus reply = RangerStatus.newBuilder()
                        .setRangerId(rangerCommand.getRangerId())
                        .setStatus(status)
                        .setLon(location[0])
                        .setLat(location[1])
                        .addAllFindings(generateRandomFindings())
                        .build();

                responseObserver.onNext(reply);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("######################### receiving coordinateRangers completed ");
                // completed too
                responseObserver.onCompleted();
            }
        };
    }
    
    public static Iterable<String> generateRandomFindings() {
        Random rand = new Random();
        // 50% chance to return an empty list
        if (rand.nextBoolean()) {
            return List.of();
        }

        List<String> findings = Arrays.asList("POACHER", "FIRE", "ILLEGAL_LOGGING", "ANIMAL_DISTRESS");
        // Random number of findings (1 to 4)
        List<String> shuffled = new ArrayList<>(findings);
        Collections.shuffle(shuffled);
        int count = rand.nextInt(4) + 1; // 1 to 4 events
        return shuffled.subList(0, count);
    }
}

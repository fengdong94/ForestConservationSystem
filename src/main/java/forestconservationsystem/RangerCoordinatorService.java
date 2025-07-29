/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import generated.grpc.rangercoordinatorservice.RangerCoordinatorServiceGrpc.RangerCoordinatorServiceImplBase;
import generated.grpc.rangercoordinatorservice.RangerCommand;
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
                String rangerId = rangerCommand.getRangerId();
                System.out.println("################### server received coordinateRangers ranger_id: " + rangerId + " action: " + rangerCommand.getAction());

                // TODO different reply, lon lat
                RangerStatus reply = RangerStatus.newBuilder()
                        .setRangerId(rangerId)
                        .setStatus(StatusType.IDLE)
                        .setLon(172.1221)
                        .setLat(23.4567)
//                        .setFindings(0, "POACHER")
//                        .setFindings(1, "FIRE")
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
}

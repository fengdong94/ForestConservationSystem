/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import generated.grpc.monitoralertservice.MonitorAlertServiceGrpc.MonitorAlertServiceImplBase;
import generated.grpc.monitoralertservice.SensorReading;
import generated.grpc.monitoralertservice.SensorReading.SensorType;
import generated.grpc.monitoralertservice.AverageData;
import generated.grpc.monitoralertservice.FireAlert;
import generated.grpc.monitoralertservice.FireAlert.RiskLevel;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;

/**
 *
 * @author Dong
 */
public class MonitorAlertService extends MonitorAlertServiceImplBase {
    @Override
    public StreamObserver<SensorReading> streamSensorData (StreamObserver<AverageData> responseObserver) {
        return new StreamObserver<SensorReading>() {
            // collect each data that arrives from the client into this arrayList
            ArrayList<SensorReading> sensorReadingList = new ArrayList();

            // when a new data arrives, put it into the arrayList
            @Override
            public void onNext(SensorReading sensorReading) {
                sensorReadingList.add(sensorReading);           
            }

            @Override
            public void onError(Throwable t) {
                // TODO Auto-generated method stub
            }
            
            // calculate the average for each SensorType
            @Override
            public void onCompleted() {
                float sumTemp = 0, sumHumi = 0, sumCo2 = 0;
                int sizeTemp = 0, sizeHumi = 0, sizeCo2 = 0;
                
                for(SensorReading sensorReading: sensorReadingList) {
                    SensorType type = sensorReading.getType();
                    float value = sensorReading.getValue();
                    
                    if (type == SensorType.TEMPERATURE) {
                        sumTemp += value;
                        sizeTemp += 1;
                    }
                    if (type == SensorType.HUMIDITY) {
                        sumHumi += value;
                        sizeHumi += 1;
                    }
                    if (type == SensorType.CO2) {
                        sumCo2 += value;
                        sizeCo2 += 1;
                    }
                }
                
                float avgTemp = sumTemp / sizeTemp;
                float avgHumi = sumHumi / sizeHumi;
                float avgCo2 = sumCo2 / sizeCo2;

                AverageData averageData = AverageData.newBuilder()
                        .setAvgTemp(avgTemp)
                        .setAvgHumi(avgHumi)
                        .setAvgCo2(avgCo2)
                        .build();
                
                responseObserver.onNext(averageData);
                responseObserver.onCompleted();
            }
        };
    }
    
    @Override
    public void checkFireRisk(AverageData averageData, StreamObserver<FireAlert> responseObserver) {
        // TODO change the riskLevel based averageData

        FireAlert fireAlert = FireAlert.newBuilder()
                .setLevel(RiskLevel.SAFE)
                .build();

        responseObserver.onNext(fireAlert);
        responseObserver.onCompleted();
    }
}

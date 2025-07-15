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
         
            // calculate the average
            @Override
            public void onCompleted() {
                float sum_temp = 0, sum_humi = 0, sum_co2 = 0;
                int size_temp = 0, size_humi = 0, size_co2 = 0;
                
                for(SensorReading sensorReading: sensorReadingList) {
                    SensorType type = sensorReading.getType();
                    float value = sensorReading.getValue();
                    
                    if (type == SensorType.TEMPERATURE) {
                        sum_temp += value;
                        size_temp += 1;
                    }
                    if (type == SensorType.HUMIDITY) {
                        sum_humi += value;
                        size_humi += 1;
                    }
                    if (type == SensorType.CO2) {
                        sum_co2 += value;
                        size_co2 += 1;
                    }
                }
                
                float avg_temp = sum_temp / size_temp;
                float avg_humi = sum_humi / size_humi;
                float avg_co2 = sum_co2 / size_co2;

                AverageData averageData = AverageData.newBuilder()
                        .setAvgTemp(avg_temp)
                        .setAvgHumi(avg_humi)
                        .setAvgCo2(avg_co2)
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

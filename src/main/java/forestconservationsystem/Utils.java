/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;
import io.grpc.Metadata;
import java.util.Random;

/**
 *
 * @author Dong
 */

// put some universal util variables and methods here, to avoid code duplication and maintain unity
public class Utils {
    public static final String SECRET_KEY = "secret-key-must-be-at-least-32bytes-X24187623";
    public static final Metadata.Key<String> AUTH_KEY = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final String SERVICE_TYPE = "_grpc._tcp.local.";
    public static final String SERVICE_NAME = "ForestConservationSystem";
    
    public static double[] generateRandomLocation(double centerLon, double centerLat) {
        // generate a random location relatively nearby a center
        Random rand = new Random();
        double randomLon = centerLon + rand.nextDouble() / 100;
        double randomLat = centerLat + rand.nextDouble() / 100;
        
        // Round to four decimal places
        return new double[]{Math.round(randomLon * 10000) / 10000.0, Math.round(randomLat * 10000) / 10000.0};
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

import io.grpc.*;
import io.jsonwebtoken.*;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Dong
 * Intercept ClientCall on server, get and validate JWT token
 */
public class JwtServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata, ServerCallHandler<ReqT, RespT> next) {
        String jwt = metadata.get(Utils.AUTH_KEY);
        
        if (jwt == null || !jwt.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Missing JWT token"), new Metadata());
            System.out.println("#################### AnimalTrackerService JWT token not found");
            // if there is no jwt, return empty listener
            return new ServerCall.Listener<ReqT>() {};
        }

        jwt = jwt.substring("Bearer ".length());
        
        try {
            // validate token
            Jwts.parser().setSigningKey(Utils.SECRET_KEY.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(jwt);
            System.out.println("#################### AnimalTrackerService JWT token validation passed");
            return next.startCall(call, metadata);
        } catch (JwtException e) {
            call.close(Status.UNAUTHENTICATED.withDescription("Invalid JWT token"), new Metadata());
            System.out.println("#################### AnimalTrackerService JWT token validation failed");
            // if jwt is invalid, return empty listener
            return new ServerCall.Listener<ReqT>() {};
        }
    }
}
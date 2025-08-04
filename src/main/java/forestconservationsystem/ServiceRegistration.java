/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package forestconservationsystem;

/**
 *
 * @author Dong
 */
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

public class ServiceRegistration {

    private static JmDNS jmdns;
    private static ServiceRegistration theRegister;

    // use Singleton pattern
    private ServiceRegistration() throws UnknownHostException, IOException {
        jmdns = JmDNS.create(InetAddress.getLocalHost());
        // for macOS
//        InetAddress localAddr = InetAddress.getByName("192.168.1.18");
//        jmdns = JmDNS.create(localAddr);
        System.out.println("Register: InetAddress.getLocalHost():" + InetAddress.getLocalHost());
    }

    /**
     * Services call getInstance() to get the singleton instance of the register
     *
     * @return
     * @throws IOException
     */
    public static ServiceRegistration getInstance() throws IOException {
        if (theRegister == null) {
            theRegister = new ServiceRegistration();
        }
        return theRegister;
    }

    /**
     * Services call registerService to register themselves so that clients can
     * discover the service
     *
     * @param type
     * @param name
     * @param port
     * @param text
     * @throws IOException
     */
    public void registerService(String type, String name, int port, String text) throws IOException {
        // Construct a service description for registering with JmDNS
        // Parameters:
        // type - fully qualified service type name, such as _http._tcp.local..
        // name - unqualified service instance name, such as foobar
        // port - the local port on which the service runs
        // text - string describing the service
        // Returns: new service info 
        ServiceInfo serviceInfo = ServiceInfo.create(type, name, port, text);
 
        // register the service
        jmdns.registerService(serviceInfo);
        System.out.println("Registered Service " + serviceInfo.toString());
    }
}

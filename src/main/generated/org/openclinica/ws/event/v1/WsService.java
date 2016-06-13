package org.openclinica.ws.event.v1;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.5.1
 * 2016-06-13T12:21:41.887+02:00
 * Generated source version: 2.5.1
 * 
 */
@WebServiceClient(name = "wsService", 
                  wsdlLocation = "src/main/wsdl/eventWsdl.wsdl",
                  targetNamespace = "http://openclinica.org/ws/event/v1") 
public class WsService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://openclinica.org/ws/event/v1", "wsService");
    public final static QName WsSoap11 = new QName("http://openclinica.org/ws/event/v1", "wsSoap11");
    static {
        URL url = null;
        try {
            url = new URL("src/main/wsdl/eventWsdl.wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(WsService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "src/main/wsdl/eventWsdl.wsdl");
        }
        WSDL_LOCATION = url;
    }

    public WsService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public WsService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WsService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public WsService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public WsService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public WsService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns Ws
     */
    @WebEndpoint(name = "wsSoap11")
    public Ws getWsSoap11() {
        return super.getPort(WsSoap11, Ws.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns Ws
     */
    @WebEndpoint(name = "wsSoap11")
    public Ws getWsSoap11(WebServiceFeature... features) {
        return super.getPort(WsSoap11, Ws.class, features);
    }

}

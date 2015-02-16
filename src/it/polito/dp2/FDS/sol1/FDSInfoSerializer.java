package it.polito.dp2.FDS.sol1;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.*;


import org.w3c.dom.Element;
import org.w3c.dom.Document;


import it.polito.dp2.FDS.Aircraft;
import it.polito.dp2.FDS.FlightInstanceReader;
import it.polito.dp2.FDS.FlightMonitor;
import it.polito.dp2.FDS.FlightMonitorException;
import it.polito.dp2.FDS.FlightMonitorFactory;
import it.polito.dp2.FDS.FlightReader;
import it.polito.dp2.FDS.PassengerReader;


public class FDSInfoSerializer {

	private DateFormat dateFormat;
	private String filename; 
	private FlightMonitor monitor;
	
	
	public FDSInfoSerializer(String xml_filename) throws FlightMonitorException {
		FlightMonitorFactory factory = FlightMonitorFactory.newInstance();
		monitor = factory.newFlightMonitor();
		dateFormat = new SimpleDateFormat("dd/MM/yyyy z");
		filename = xml_filename;
	}
	
	
	
	public Element createAircraftNode(Aircraft air, Document D)
	{
		Element e;
		e = D.createElement("aircraft");
		e.setAttribute("model", air.model);
        e.setAttribute("seatsNumber", Integer.toString(air.seats.size()));
        
        Set<String> seat =  air.seats;	
		for (String s : seat) {
			Element seatele; 
			seatele = D.createElement("seat");
			seatele.setAttribute("number", s);
			e.appendChild(seatele);
		}
    	
    	return e;
	}
	
	
	public Element createFlightNode(FlightReader f, Document D)
	{	
		StringBuffer b = new StringBuffer();
		GregorianCalendar g = new GregorianCalendar();
		g.set(GregorianCalendar.HOUR_OF_DAY, f.getDepartureTime().getHour());
		g.set(GregorianCalendar.MINUTE, f.getDepartureTime().getMinute());	
		b.append(String.format("%1$2tH", g));
		b.append(':');
		b.append(String.format("%1$2tM", g));
	
		Element e;
		e = D.createElement("flight");
		e.setAttribute("number", f.getNumber());
		e.setAttribute("from", f.getDepartureAirport());
        e.setAttribute("to", f.getDestinationAirport());
        e.setAttribute("time", b.toString());
    	
    	return e;
	}
	
	public Element createPassengerNode(PassengerReader PR, Document D)
	{
		Element e;
		e = D.createElement("passenger");
		e.setAttribute("name", PR.getName());
		e.setAttribute("boarded", Boolean.toString(PR.boarded()));
		e.setAttribute("seat", PR.getSeat());
		
		return e;
	}
	
	
	public Element createFlightInstanceNode(FlightInstanceReader f, Document D)
	{
		
		Element e;
		e = D.createElement("flightInstance");
		e.setAttribute("number", f.getFlight().getNumber());
		e.setAttribute("delay", Integer.toString(f.getDelay()));
        e.setAttribute("model", f.getAircraft().model);
        e.setAttribute("status", f.getStatus().toString());
        e.setAttribute("gate", f.getDepartureGate());
		GregorianCalendar d = f.getDate();
		dateFormat.setTimeZone(d.getTimeZone());
		e.setAttribute("date", dateFormat.format(d.getTime()));        
		
        Set<PassengerReader> set = f.getPassengerReaders(null);
		for (PassengerReader ps : set) {
			e.appendChild(createPassengerNode(ps, D));
						
		}
      
		return e;
	}
	
	
	public void savetoXML() throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.newDocument();
				
		
		Element rootEle = document.createElement("flightmonitor");

		
		Set<Aircraft> air =  monitor.getAircrafts();
		
		for (Aircraft aircraft : air) {
			rootEle.appendChild(createAircraftNode(aircraft, document));
						
		}
		
		List<FlightReader> l = monitor.getFlights(null, null, null);
		for (FlightReader f:l) {
			rootEle.appendChild(createFlightNode(f, document));
		}
		
		List<FlightInstanceReader> fl = monitor.getFlightInstances(null, null, null);
		for (FlightInstanceReader f:fl) {
			rootEle.appendChild(createFlightInstanceNode(f, document));
		}
		
		document.appendChild(rootEle);
        
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "dtd/flightInfo.dtd");
           
            tr.transform(new DOMSource(document), 
                                 new StreamResult(new FileOutputStream(filename)));

        } catch (TransformerException te) {
            System.out.println(te.getMessage());
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
	   
		System.out.println("Job Finished");		
	}
	
	public static void main(String[] args) {
		FDSInfoSerializer f;
		
		try {
			f = new FDSInfoSerializer("1234.xml");
			f.savetoXML();
		} catch (FlightMonitorException e) {
			System.err.println("Could not instantiate flight monitor object");
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
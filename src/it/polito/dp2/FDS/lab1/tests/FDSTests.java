package it.polito.dp2.FDS.lab1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.polito.dp2.FDS.Aircraft;
import it.polito.dp2.FDS.FlightInstanceReader;
import it.polito.dp2.FDS.FlightMonitor;
import it.polito.dp2.FDS.FlightMonitorFactory;
import it.polito.dp2.FDS.FlightReader;
import it.polito.dp2.FDS.MalformedArgumentException;
import it.polito.dp2.FDS.PassengerReader;
import it.polito.dp2.FDS.Time;

public class FDSTests {
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	// Comparator classes used for sorting of data objects
	
	// A Comparator for Aircraft objects
    class AircraftComparator implements Comparator<Aircraft> {
        public int compare(Aircraft a0, Aircraft a1) {
        	// comparison is based on the model name, which should be unique
        	return a0.model.compareTo(a1.model);
        }
    }
    
    // A Comparator for FlightReader objects
    class FlightReaderComparator implements Comparator<FlightReader> {
        public int compare(FlightReader f0, FlightReader f1) {
        	// comparison is based on the flight number, which should be unique
        	return f0.getNumber().compareTo(f1.getNumber());
        }
    }
    
    // A Comparator for FlightInstanceReader objects 
    class FlightInstanceReaderComparator implements Comparator<FlightInstanceReader> {
        public int compare(FlightInstanceReader f0, FlightInstanceReader f1) {
        	// comparison is based on flight number and, in case of equality, on flight date
        	if (f0.getFlight().getNumber().equals(f1.getFlight().getNumber()))
        		return f0.getDate().compareTo(f1.getDate());
        	else
        		return f0.getFlight().getNumber().compareTo(f1.getFlight().getNumber());
        }
    }

	
	private static FlightMonitor referenceFlightMonitor;	// reference implementation
	private static FlightMonitor testFlightMonitor;		// implementation under test
	
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	// create the reference implementation (using the pseudo-random generator)
        System.setProperty("it.polito.dp2.FDS.FlightMonitorFactory", "it.polito.dp2.FDS.Random.FlightMonitorFactoryImpl");
        referenceFlightMonitor = FlightMonitorFactory.newInstance().newFlightMonitor();

        // create the implementation under test (using the implementation in sol1)
        System.setProperty("it.polito.dp2.FDS.FlightMonitorFactory", "it.polito.dp2.FDS.sol1.FlightMonitorFactory");
        testFlightMonitor = FlightMonitorFactory.newInstance().newFlightMonitor();;
    }
    
    @Before
    public void setUp() throws Exception {
    	// if reference implementation or implementation under test are null, the test fails
        assertNotNull(referenceFlightMonitor);
        assertNotNull(testFlightMonitor);
    }

    @Test
    public final void testGetAircrafts() {
    	// call getAircrafts on the two implementations
        Set<Aircraft> rs = referenceFlightMonitor.getAircrafts();
        Set<Aircraft> ts = testFlightMonitor.getAircrafts();
        
        // if one of the two calls returned null while the other didn't return null, the test fails 
        if ((rs == null) && (ts != null) || (rs != null) && (ts == null)) {
            fail();
            return;
        }

        // if both calls returned null, there are no aircrafts, and the test passes
        if ((rs == null) && (ts == null)) {
            assertTrue("There are no aircrafts!", true);
            return;
        }
        
        // check that the number of aircrafts matches
        assertEquals("Wrong Number of Aircrafts", rs.size(), ts.size());
        
        // create treesets of aircrafts, using the aircraft comparator for sorting, one for reference and one for impl. under test 
        TreeSet<Aircraft> rts = new TreeSet<Aircraft>(new AircraftComparator());
        TreeSet<Aircraft> tts = new TreeSet<Aircraft>(new AircraftComparator());
    
        rts.addAll(rs);
        tts.addAll(ts);
        
        // check that all aircrafts match one by one
        Iterator<Aircraft> ri = rts.iterator();
        Iterator<Aircraft> ti = tts.iterator();

        while (ri.hasNext() && ti.hasNext()) {
        	compareAircraft(ri.next(),ti.next());
        }
    }

    // method for comparing two non-null Aircraft objects
	private void compareAircraft(Aircraft ra, Aircraft ta) {
		assertNotNull(ra);
        assertNotNull(ta);

        // check the model matches
        compareString(ra.model, ta.model, "Aircraft Model");
		
        // get the sets of seats
		Set<String> rs = ra.seats;
        Set<String> ts = ta.seats;
        
        // if one of the two sets of seats is null while the other is not null, the test fails
        if ((rs == null) && (ts != null) || (rs != null) && (ts == null)) {
            fail();
            return;
		
        }
	
        // if both sets of seats are null, there is nothing to match and the comparison succeeds
        if ((rs == null) && (ts == null)) {
            assertTrue("There are no seats!", true);
            return;
        }
        
        // check the sizes of the two sets of seats matche
        assertEquals("Wrong Number of Seats", rs.size(), ts.size());
        
        // create treesets of seats (strings), one for reference and one for impl. under test 
        TreeSet<String> rts = new TreeSet<String>();
        TreeSet<String> tts = new TreeSet<String>();
    
        rts.addAll(rs);
        tts.addAll(ts);
        
        Iterator<String> ri = rts.iterator();
        Iterator<String> ti = tts.iterator();

        // check that all seats match one by one
        while (ri.hasNext() && ti.hasNext()) {
        	compareString(ri.next(),ti.next(),"Seat");
        }	
	}

	// method for comparing two non-null strings
	private void compareString(String rs, String ts, String meaning) {
		assertNotNull("NULL "+meaning, ts);
        assertEquals("Wrong "+meaning, rs, ts);		
	}
	
    @Test
    public final void testGetFlights() {
    	try {
    		// call getFlights with null parameters on the two implementations
			List<FlightReader> rl = referenceFlightMonitor.getFlights(null, null, null);
			List<FlightReader> tl = testFlightMonitor.getFlights(null, null, null);
			
			// if one of the two calls returned null while the other didn't return null, the test fails
	        if ((rl == null) && (tl != null) || (rl != null) && (tl == null)) {
	            fail();
	            return;
	        }

	        // if both calls returned null, there are no flights, and the test passes
	        if ((rl == null) && (tl == null)) {
	            assertTrue("There are no flights!", true);
	            return;
	        }
	        
	        // check the sizes of the returned lists are the same
	        assertEquals("Wrong Number of Flights", rl.size(), tl.size());
	        
	        // create treesets of FlightReaders, using the FlightReaderComparator for sorting, one for reference and one for impl. under test
	        TreeSet<FlightReader> rts = new TreeSet<FlightReader>(new FlightReaderComparator());
	        TreeSet<FlightReader> tts = new TreeSet<FlightReader>(new FlightReaderComparator());
	    
	        rts.addAll(rl);
	        tts.addAll(tl);
	        
	        // check that all FlightReaders match one by one
	        Iterator<FlightReader> ri = rts.iterator();
	        Iterator<FlightReader> ti = tts.iterator();

	        while (ri.hasNext() && ti.hasNext()) {
	        	compareFlightReader(ri.next(),ti.next());
	        }
		} catch (MalformedArgumentException e) {
			// if a MalformedArgumentException is raised the test fails (the reference implementation will not raise it)
			fail();
		}
    }

    // method for comparing two non-null FlightReader objects
	private void compareFlightReader(FlightReader rfr, FlightReader tfr) {
		// check the FlightReaders are not null
		assertNotNull(rfr);
        assertNotNull(tfr);

        // check the FlightReaders return the same data
        compareString(rfr.getNumber(), tfr.getNumber(), "Flight Number");
        compareString(rfr.getDepartureAirport(), tfr.getDepartureAirport(), "Departure Airport");
        compareString(rfr.getDestinationAirport(), tfr.getDestinationAirport(), "Destination Airport");
        compareTime(rfr.getDepartureTime(),tfr.getDepartureTime());	
	}

	// method for comparing non-null Time objects
	private void compareTime(Time t1, Time t2) {
		assertNotNull(t1);
        assertNotNull(t2);
        
        assertEquals("Wrong Hour", t1.getHour(), t2.getHour());
        assertEquals("Wrong Minute", t1.getMinute(), t2.getMinute());
	}
	
    @Test
    public final void testGetFlightInstances() {
    	try {
    		// call getFlightInstances with null parameters on the two implementations
			List<FlightInstanceReader> rl = referenceFlightMonitor.getFlightInstances(null, null, null);
			List<FlightInstanceReader> tl = testFlightMonitor.getFlightInstances(null, null, null);
			
			// if one of the two calls returned null while the other didn't return null, the test fails
	        if ((rl == null) && (tl != null) || (rl != null) && (tl == null)) {
	            fail();
	            return;
	        }

	        // if both calls returned null, there are no flight instances, and the test passes
	        if ((rl == null) && (tl == null)) {
	            assertTrue("There are no flight instances!", true);
	            return;
	        }
	        
	        // check the number of flight instances matches
	        assertEquals("Wrong Number of Flight Instances", rl.size(), tl.size());
	        
	        // create treesets of FlightInstanceReaders, using the FlightInstanceReaderComparator for sorting, one for reference and one for impl. under test
	        TreeSet<FlightInstanceReader> rts = new TreeSet<FlightInstanceReader>(new FlightInstanceReaderComparator());
	        TreeSet<FlightInstanceReader> tts = new TreeSet<FlightInstanceReader>(new FlightInstanceReaderComparator());
	    
	        rts.addAll(rl);
	        tts.addAll(tl);
	        
	        // check all FlightInstanceReaders match one by one
	        Iterator<FlightInstanceReader> ri = rts.iterator();
	        Iterator<FlightInstanceReader> ti = tts.iterator();

	        while (ri.hasNext() && ti.hasNext()) {
	        	compareFlightInstanceReader(ri.next(),ti.next());
	        }
		} catch (MalformedArgumentException e) {
			// if a MalformedArgumentException is raised the test fails (the reference implementation will not raise it)
			fail();
		}
    }
    
    // method for building a string that identifies a FlightInstanceReader
    private String flightInstanceId(FlightInstanceReader f) {
    	GregorianCalendar g = f.getDate();
    	dateFormat.setTimeZone(g.getTimeZone());
    	return "Flight Instance of flight "+f.getFlight().getNumber()+" of "+ dateFormat.format(g.getTime());
    }

    // method for comparing two non-null FlightInstanceReader objects
	private void compareFlightInstanceReader(FlightInstanceReader rfir, FlightInstanceReader tfir) {
		// check the FlightReaders are not null
		assertNotNull(rfir);
        assertNotNull(tfir);
        
        // get sets of passengers for the two FlightInstanceReader objects
        Set<PassengerReader> rpass = rfir.getPassengerReaders(null);
        Set<PassengerReader> tpass = tfir.getPassengerReaders(null);

        // get flight readers for the two FlightInstanceReader objects and compare them
        compareFlightReader(rfir.getFlight(), tfir.getFlight());
        
        // check the flight readers do not return null dates (the reference one will not)
		assertNotNull(rfir.getDate());
        assertNotNull(tfir.getDate());
        
        // make identifying string for reference flight instance reader
        String rfId = flightInstanceId(rfir);
        
        // get reference and implementation dates and turn them into GregorianCalendar objects
        GregorianCalendar rgc = rfir.getDate();
        GregorianCalendar tgc = tfir.getDate();
                
        // check the two dates have the same day month and year
        if (rpass!= null && rpass.size()>0) {	// test cases 1 or 2
        	assertEquals("Wrong day in "+rfId, rgc.get(GregorianCalendar.DAY_OF_MONTH), tgc.get(GregorianCalendar.DAY_OF_MONTH));
        	assertEquals("Wrong month in "+rfId, rgc.get(GregorianCalendar.MONTH)+1, tgc.get(GregorianCalendar.MONTH)+1);
        	assertEquals("Wrong year in "+rfId, rgc.get(GregorianCalendar.YEAR), tgc.get(GregorianCalendar.YEAR));
        }
        
        // check the flight instances return the same aircraft, status, delay and departure gate
        compareAircraft(rfir.getAircraft(), tfir.getAircraft());
        assertEquals("Wrong status in "+rfId, rfir.getStatus(), tfir.getStatus());
        assertEquals("Wrong delay in "+rfId, rfir.getDelay(), tfir.getDelay());
        assertEquals("Wrong Departure Gate in "+rfId, rfir.getDepartureGate(), tfir.getDepartureGate());
        
        if (rpass!= null && rpass.size()>0) { // test cases 1 or 2        	
        	// compare numbers of passengers
        	assertNotNull(rpass);
            assertNotNull(tpass);
            assertEquals("Wrong Number of Passengers in"+rfId, rpass.size(), tpass.size());
        }       
	}
}

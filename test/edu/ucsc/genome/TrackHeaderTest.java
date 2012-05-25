package edu.ucsc.genome;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TrackHeaderTest {
	
	public static final String TEST_HEADER = "track type=wiggle_0 name='Test Wig File' description=\"ChIP of Protein\" viewLimits=-5:5 autoScale=off visibility=full";
	public static final String INVALID_HEADER = "track type=wiggle_0 name='Test Wig File' description=";
	public static final String INVALID_HEADER2 = "track type=wiggle_0 name='Test Wig File' mykey=value";
	public static final TrackHeader.Type TYPE = TrackHeader.Type.WIGGLE;
	public static final String NAME = "Test Wig File";
	public static final String DESCRIPTION = "ChIP of Protein";
	public static final Double[] VIEW_LIMITS = {-5.0, 5.0};
	public static final Boolean AUTO_SCALE = false;
	public static final String VISIBILITY = "full";
	
	private TrackHeader test;
	
	@Before
	public void setUp() throws Exception {
		test = new TrackHeader(TYPE);
		test.setName(NAME);
		test.setDescription(DESCRIPTION);
		test.setViewLimits(VIEW_LIMITS[0], VIEW_LIMITS[1]);
		test.setAutoScale(AUTO_SCALE);
		test.setVisibility(VISIBILITY);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParse() throws TrackHeaderException {
		TrackHeader header = TrackHeader.parse(TEST_HEADER);
	
		assertEquals("Type not parsed correctly", TYPE, header.getType());
		assertEquals("Name not parsed correctly", NAME, header.getName());
		assertEquals("Description not parsed correctly", DESCRIPTION, header.getDescription());
		assertArrayEquals("View limits not parsed correctly", VIEW_LIMITS, header.getViewLimits());
		assertEquals("Auto scale not parsed correctly", AUTO_SCALE, header.isAutoScale());
		assertEquals("Visibility not parsed correctly", VISIBILITY, header.getVisibility());
	}
	
	@Test(expected = TrackHeaderException.class)
	public void testParseInvalid() throws TrackHeaderException {
		TrackHeader.parse(INVALID_HEADER);
	}
	
	@Test(expected = TrackHeaderException.class)
	public void testParseInvalidKey() throws TrackHeaderException {
		TrackHeader.parse(INVALID_HEADER2);
	}

	@Test
	public void testToString() {
		String expected = "track type=wiggle_0 name='Test Wig File' description='ChIP of Protein' autoScale=off visibility=full viewLimits=-5.0:5.0";
		assertEquals("toString not correct", expected, test.toString());
	}

}

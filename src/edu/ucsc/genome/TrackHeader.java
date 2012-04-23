package edu.ucsc.genome;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author timpalpant
 * Represents a track line for the UCSC Genome Browser
 * 
 * Optional parameters: See http://genome.ucsc.edu/goldenPath/help/customTrack.html#TRACK
 * and http://genome.ucsc.edu/goldenPath/help/wiggle.html
 * 
 * name              trackLabel           # default is "User Track"
 * description       centerlabel          # default is "User Supplied Track"
 * visibility        full|dense|hide      # default is hide (will also take numeric values 2|1|0)
 * color             RRR,GGG,BBB          # default is 255,255,255
 * altColor          RRR,GGG,BBB          # default is 128,128,128
 * priority          N                    # default is 100
 * autoScale         on|off               # default is on
 * alwaysZero        on|off               # default is off
 * gridDefault       on|off               # default is off
 * maxHeightPixels   max:default:min      # default is 128:128:11
 * graphType         bar|points           # default is bar
 * viewLimits        lower:upper          # default is range found in data
 * yLineMark         real-value           # default is 0.0
 * yLineOnOff        on|off               # default is off
 * windowingFunction maximum|mean|minimum # default is maximum
 * smoothingWindow   off|[2-16]           # default is off
 */
public class TrackHeader {
	protected String type;
	protected String name;
	protected String description;
	protected String visibility;
	protected Short[] color;
	protected Short[] altColor;
	protected Short priority;
	protected Boolean autoScale;
	protected Boolean alwaysZero;
	protected Boolean gridDefault;
	protected Integer[] maxHeightPixels;
	protected String graphType;
	protected Double[] viewLimits;
	protected Double yLineMark;
	protected Boolean yLineOnOff;
	protected String windowingFunction;
	protected Byte smoothingWindow;
	protected Boolean itemRgb;
	protected String colorByStrand;
	protected Boolean useScore;
	protected String group;
	protected String db;
	protected String url;
	protected String htmlUrl;
	
	/**
	 * Matches key-value attribute pairs in a track line, with optional quotation marks around the value
	 * (quotation marks are mandatory for values with whitespace)
	 */
	private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("\\b[\\w]*=(\".*\"|\'.*\'|\\S*)");
	
	public TrackHeader(String type) {
		this.type = type;
	}
	
	public TrackHeader() {
		this(null);
	}
	
	public static TrackHeader parse(String line) throws TrackHeaderException {
		TrackHeader header = new TrackHeader();
		
		Matcher m = ATTRIBUTE_PATTERN.matcher(line);
		while (m.find()) {
			String token = m.group();
			int delim = token.indexOf('=');
			if (delim == -1) { 
				throw new TrackHeaderException("Invalid token: '" + token + "' in UCSC track header"); 
			}
			String key = token.substring(0, delim);
			String value = token.substring(delim+1);
			if (key.length() == 0 || value.length() == 0) { 
				throw new TrackHeaderException("Invalid token: '" + token + "' in UCSC track header"); 
			}
			char firstChar = value.charAt(0);
			if (firstChar == '\"' || firstChar == '\'') { 
				value = value.substring(1); 
			}
			char lastChar = value.charAt(value.length()-1);
			if (lastChar == '\"' || lastChar == '\'') { 
				value = value.substring(0, value.length()-1); 
			}
			
			// Attempt to parse and set the relevant parameter
			try {
				switch(key) {
				case "type":
					header.setType(value);
					break;
				case "name":
					header.setName(value);
					break;
				case "description":
					header.setDescription(value);
					break;
				case "visibility":
					header.setVisibility(value);
					break;
				case "color":
					String[] rgb = value.split(":");
					header.setColor(Short.parseShort(rgb[0]), Short.parseShort(rgb[1]), Short.parseShort(rgb[2]));
					break;
				case "altColor":
					String[] altrgb = value.split(":");
					header.setColor(Short.parseShort(altrgb[0]), Short.parseShort(altrgb[1]), Short.parseShort(altrgb[2]));
					break;
				case "priority":
					header.setPriority(Short.parseShort(value));
					break;
				case "autoScale":
					header.setAutoScale(parseBoolean(value));
					break;
				case "alwaysZero":
					header.setAlwaysZero(parseBoolean(value));
					break;
				case "gridDefault":
					header.setGridDefault(parseBoolean(value));
					break;
				case "maxHeightPixels":
					String[] mdm = value.split(":");
					header.setMaxHeightPixels(Integer.parseInt(mdm[0]), Integer.parseInt(mdm[1]), Integer.parseInt(mdm[2]));
					break;
				case "graphType":
					header.setGraphType(value);
					break;
				case "viewLimits":
					String[] limits = value.split(":");
					header.setViewLimits(Double.parseDouble(limits[0]), Double.parseDouble(limits[1]));
					break;
				case "yLineMark":
					header.setyLineMark(Double.parseDouble(value));
					break;
				case "yLineOnOff":
					header.setyLineOnOff(parseBoolean(value));
					break;
				case "windowingFunction":
					header.setWindowingFunction(value);
					break;
				case "smoothingWindow":
					header.setSmoothingWindow(Byte.parseByte(value));
					break;
				case "itemRgb":
					header.setItemRgb(parseBoolean(value));
					break;
				case "colorByStrand":
					header.setColorByStrand(value);
					break;
				case "useScore":
					header.setUseScore(parseBoolean(value));
					break;
				case "group":
					header.setGroup(value);
					break;
				case "db":
					header.setDb(value);
					break;
				case "url":
					header.setUrl(value);
					break;
				case "htmlUrl":
					header.setHtmlUrl(value);
					break;
				default:
					throw new TrackHeaderException("Unknown track attribute: " + key);
				}
			} catch (Exception e) {
				throw new TrackHeaderException("Invalid or unknown attribute: " + token);
			}
		}
		
		return header;
	}
	
	public String toString() {
		StringBuilder s = new StringBuilder("track");
		
		if (type != null) { s.append(" type=").append(type); }
		if (name != null) { s.append(" name='").append(name).append("'"); }
		if (description != null) { s.append(" description='").append(description).append("'"); }
		if (autoScale != null) {
			String autoScaleStr = autoScale ? "on" : "off";
			s.append(" autoScale=").append(autoScaleStr); 
		}
		if (visibility != null) { s.append(" visibility=").append(visibility); }
		if (viewLimits != null) {
			s.append(" viewLimits=").append(viewLimits[0]).append(":").append(viewLimits[1]); 
		}
		if (color != null) { s.append(" color=").append(color); }
		if (altColor != null) { s.append(" altColor=").append(altColor); }
		if (priority != null) { s.append(" priority=").append(priority); }
		if (alwaysZero != null) { s.append(" color=").append(color); }
		
		// TODO: Include all properties
        
    return s.toString();
	}
	
	private static boolean parseBoolean(String value) {
		if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
			return true;
		}
		
		return false;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the visibility
	 */
	public String getVisibility() {
		return visibility;
	}

	/**
	 * @param visibility the visibility to set
	 */
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	/**
	 * @return the color
	 */
	public Short[] getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(short red, short green, short blue) {
		color = new Short[3];
		color[0] = red;
		color[1] = green;
		color[2] = blue;
	}

	/**
	 * @return the altColor
	 */
	public Short[] getAltColor() {
		return altColor;
	}

	/**
	 * @param altColor the altColor to set
	 */
	public void setAltColor(short red, short green, short blue) {
		altColor = new Short[3];
		altColor[0] = red;
		altColor[1] = green;
		altColor[2] = blue;
	}

	/**
	 * @return the priority
	 */
	public Short getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(short priority) {
		this.priority = priority;
	}

	/**
	 * @return the autoScale
	 */
	public boolean isAutoScale() {
		return autoScale;
	}

	/**
	 * @param autoScale the autoScale to set
	 */
	public void setAutoScale(boolean autoScale) {
		this.autoScale = autoScale;
	}

	/**
	 * @return the alwaysZero
	 */
	public boolean isAlwaysZero() {
		return alwaysZero;
	}

	/**
	 * @param alwaysZero the alwaysZero to set
	 */
	public void setAlwaysZero(boolean alwaysZero) {
		this.alwaysZero = alwaysZero;
	}

	/**
	 * @return the gridDefault
	 */
	public Boolean getGridDefault() {
		return gridDefault;
	}

	/**
	 * @param gridDefault the gridDefault to set
	 */
	public void setGridDefault(boolean gridDefault) {
		this.gridDefault = gridDefault;
	}

	/**
	 * @return the maxHeightPixels
	 */
	public Integer[] getMaxHeightPixels() {
		return maxHeightPixels;
	}

	/**
	 * @param maxHeightPixels the maxHeightPixels to set
	 */
	public void setMaxHeightPixels(int maxHeight, int defaultHeight, int minHeight) {
		maxHeightPixels = new Integer[3];
		maxHeightPixels[0] = maxHeight;
		maxHeightPixels[1] = defaultHeight;
		maxHeightPixels[2] = minHeight;
	}

	/**
	 * @return the graphType
	 */
	public String getGraphType() {
		return graphType;
	}

	/**
	 * @param graphType the graphType to set
	 */
	public void setGraphType(String graphType) {
		this.graphType = graphType;
	}

	/**
	 * @return the viewLimits
	 */
	public Double[] getViewLimits() {
		return viewLimits;
	}

	/**
	 * @param viewLimits the viewLimits to set
	 */
	public void setViewLimits(double min, double max) {
		viewLimits = new Double[2];
		viewLimits[0] = min;
		viewLimits[1] = max;
	}

	/**
	 * @return the yLineMark
	 */
	public Double isyLineMark() {
		return yLineMark;
	}

	/**
	 * @param yLineMark the yLineMark to set
	 */
	public void setyLineMark(double yLineMark) {
		this.yLineMark = yLineMark;
	}

	/**
	 * @return the yLineOnOff
	 */
	public boolean isyLineOnOff() {
		return yLineOnOff;
	}

	/**
	 * @param yLineOnOff the yLineOnOff to set
	 */
	public void setyLineOnOff(boolean yLineOnOff) {
		this.yLineOnOff = yLineOnOff;
	}

	/**
	 * @return the windowingFunction
	 */
	public String getWindowingFunction() {
		return windowingFunction;
	}

	/**
	 * @param windowingFunction the windowingFunction to set
	 */
	public void setWindowingFunction(String windowingFunction) {
		this.windowingFunction = windowingFunction;
	}

	/**
	 * @return the smoothingWindow
	 */
	public Byte getSmoothingWindow() {
		return smoothingWindow;
	}

	/**
	 * @param smoothingWindow the smoothingWindow to set
	 */
	public void setSmoothingWindow(byte smoothingWindow) {
		this.smoothingWindow = smoothingWindow;
	}

	/**
	 * @return the itemRgb
	 */
	public Boolean getItemRgb() {
		return itemRgb;
	}

	/**
	 * @param itemRgb the itemRgb to set
	 */
	public void setItemRgb(boolean itemRgb) {
		this.itemRgb = itemRgb;
	}

	/**
	 * @return the colorByStrand
	 */
	public String getColorByStrand() {
		return colorByStrand;
	}

	/**
	 * @param colorByStrand the colorByStrand to set
	 */
	public void setColorByStrand(String colorByStrand) {
		this.colorByStrand = colorByStrand;
	}

	/**
	 * @return the useScore
	 */
	public Boolean getUseScore() {
		return useScore;
	}

	/**
	 * @param useScore the useScore to set
	 */
	public void setUseScore(boolean useScore) {
		this.useScore = useScore;
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @return the db
	 */
	public String getDb() {
		return db;
	}

	/**
	 * @param db the db to set
	 */
	public void setDb(String db) {
		this.db = db;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the htmlUrl
	 */
	public String getHtmlUrl() {
		return htmlUrl;
	}

	/**
	 * @param htmlUrl the htmlUrl to set
	 */
	public void setHtmlUrl(String htmlUrl) {
		this.htmlUrl = htmlUrl;
	}
}

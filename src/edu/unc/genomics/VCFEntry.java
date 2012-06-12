package edu.unc.genomics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.unc.genomics.io.IntervalFileFormatException;

/**
 * An entry in a VCF v4.1 file
 * @author timpalpant
 *
 */
public class VCFEntry extends Interval {

	private static final long serialVersionUID = 6831019853585975440L;
	
	private String ref;
	private String[] alt;
	private double qual;
	private String filter;
	private Map<String,String> info = new LinkedHashMap<>();
	private String[] format;
	private List<String[]> genotypes = new ArrayList<>();

	public VCFEntry(String chr, int pos, String id) {
		super(chr, pos, pos, id);
	}
	
	public static VCFEntry parse(final String line) {
		if (line.startsWith("#")) {
			return null;
		}
		
		String[] entry = line.split("\t");
		if (entry.length < 8) {
			throw new IntervalFileFormatException("Invalid VCF entry has < 8 columns");
		}
		
		String chr = entry[0];
		int pos = Integer.parseInt(entry[1]);
		String id = entry[2];
		VCFEntry vcf = new VCFEntry(chr, pos, id);
		vcf.setRef(entry[3]);
		vcf.setAlt(entry[4].split(","));
		vcf.setQual(Double.parseDouble(entry[5]));
		vcf.setFilter(entry[6]);
		for (String token : entry[7].split(";")) {
			String[] keypair = token.split("=");
			if (keypair.length == 1) {
				vcf.addInfo(keypair[0], null);
			} else if (keypair.length == 2) {
				vcf.addInfo(keypair[0], keypair[1]);
			} else {
				throw new IntervalFileFormatException("Invalid key-value pair ("+token+") in info string ("+entry[7]+") in VCF file!");
			}
		}
		
		// If the VCF file has genotype information
		if (entry.length > 8) {
			vcf.setFormat(entry[8].split(":"));
			for (int i = 9; i < entry.length; i++) {
				vcf.addGenotype(entry[i].split(":"));
			}
		}
		
		return vcf;
	}
	
	@Override
	public String toOutput() {
		return toVCF();
	}
	
	public String toVCF() {
		StringBuilder sb = new StringBuilder();
		sb.append(getChr()).append('\t').append(getStart()).append('\t').append(getId());
		sb.append('\t').append(ref).append('\t').append(StringUtils.join(alt, ','));
		sb.append('\t').append(qual).append('\t').append(filter).append('\t').append(getInfoString());
		if (format != null) {
			sb.append('\t').append(getFormatString());
		}
		if (genotypes.size() > 0) {
			for (int i = 0; i < genotypes.size(); i++) {
				sb.append('\t').append(StringUtils.join(genotypes.get(i), ':')); 
			}
		}
		
		return sb.toString();
	}

	/**
	 * @return the ref
	 */
	public final String getRef() {
		return ref;
	}

	/**
	 * @param ref the ref to set
	 */
	public final void setRef(String ref) {
		this.ref = ref;
	}

	/**
	 * @return the alt
	 */
	public final String[] getAlt() {
		return alt;
	}

	/**
	 * @param alt the alt to set
	 */
	public final void setAlt(String[] alt) {
		this.alt = alt;
	}

	/**
	 * @return the qual
	 */
	public final double getQual() {
		return qual;
	}

	/**
	 * @param qual the qual to set
	 */
	public final void setQual(double qual) {
		this.qual = qual;
	}

	/**
	 * @return the filter
	 */
	public final String getFilter() {
		return filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public final void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * @return the info
	 */
	public final Map<String,String> getInfo() {
		return info;
	}
	
	/**
	 * @return the info
	 */
	public final String getInfoString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String key : info.keySet()) {
			sb.append(key);
			if (info.get(key) != null) {
				sb.append('=').append(info.get(key));
			}
			
			if (++i < info.size()) {
				sb.append(';');
			}
		}
		
		return sb.toString();
	}

	/**
	 * @param info the info to set
	 */
	public final void addInfo(String key, String value) {
		info.put(key, value);
	}

	/**
	 * @return the format
	 */
	public final String[] getFormat() {
		return format;
	}
	
	/**
	 * @return the format
	 */
	public final String getFormatString() {
		return StringUtils.join(format, ":");
	}

	/**
	 * @param format the format to set
	 */
	public final void setFormat(String[] format) {
		this.format = format;
	}
	
	/**
	 * @return the genotype columns of this VCF entry
	 */
	public final List<String[]> getGenotypes() {
		return genotypes;
	}
	
	/**
	 * @param genotype a new genotype entry
	 */
	public final void addGenotype(String[] genotype) {
		genotypes.add(genotype);
	}

}

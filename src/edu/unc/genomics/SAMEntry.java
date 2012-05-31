package edu.unc.genomics;

import java.util.List;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.Cigar;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFileSource;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecord.SAMTagAndValue;
import net.sf.samtools.SAMValidationError;

/**
 * Wrapper for SAM-JDK's SAMRecord class that conforms to the specifications
 * of all other java-genomics-io classes and is a subclass of Interval
 * 
 * @author timpalpant
 *
 */
public class SAMEntry extends Interval implements Sequence {

	private static final long serialVersionUID = -439658908814430105L;
	
	private final SAMRecord r;
	
	/**
	 * @param chr
	 * @param start
	 * @param stop
	 */
	public SAMEntry(SAMRecord r) {
		super(r.getReferenceName(), r.getAlignmentStart(), r.getAlignmentEnd());
		this.r = r;
        
		// Make the SAMEntry conform to standards
    int extend = (r.getInferredInsertSize() == 0) ? r.getReadLength() : Math.abs(r.getInferredInsertSize());
		if (r.getReadNegativeStrandFlag()) {
			start = r.getAlignmentStart() + r.getReadLength() - 1;
			stop = start - extend + 1;
		} else {
			stop = start + extend - 1;
		}
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadName()
	 */
	public String getReadName() {
		return r.getReadName();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadNameLength()
	 */
	public int getReadNameLength() {
		return r.getReadNameLength();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setReadName(java.lang.String)
	 */
	public void setReadName(String value) {
		r.setReadName(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadString()
	 */
	public String getReadString() {
		return r.getReadString();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setReadString(java.lang.String)
	 */
	public void setReadString(String value) {
		r.setReadString(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadBases()
	 */
	public byte[] getReadBases() {
		return r.getReadBases();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setReadBases(byte[])
	 */
	public void setReadBases(byte[] value) {
		r.setReadBases(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadLength()
	 */
	public int getReadLength() {
		return r.getReadLength();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getBaseQualityString()
	 */
	public String getBaseQualityString() {
		return r.getBaseQualityString();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setBaseQualityString(java.lang.String)
	 */
	public void setBaseQualityString(String value) {
		r.setBaseQualityString(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getBaseQualities()
	 */
	public byte[] getBaseQualities() {
		return r.getBaseQualities();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setBaseQualities(byte[])
	 */
	public void setBaseQualities(byte[] value) {
		r.setBaseQualities(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getOriginalBaseQualities()
	 */
	public byte[] getOriginalBaseQualities() {
		return r.getOriginalBaseQualities();
	}

	/**
	 * @param oq
	 * @see net.sf.samtools.SAMRecord#setOriginalBaseQualities(byte[])
	 */
	public void setOriginalBaseQualities(byte[] oq) {
		r.setOriginalBaseQualities(oq);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReferenceName()
	 */
	public String getReferenceName() {
		return r.getReferenceName();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setReferenceName(java.lang.String)
	 */
	public void setReferenceName(String value) {
		r.setReferenceName(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReferenceIndex()
	 */
	public Integer getReferenceIndex() {
		return r.getReferenceIndex();
	}

	/**
	 * @param referenceIndex
	 * @see net.sf.samtools.SAMRecord#setReferenceIndex(int)
	 */
	public void setReferenceIndex(int referenceIndex) {
		r.setReferenceIndex(referenceIndex);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getMateReferenceName()
	 */
	public String getMateReferenceName() {
		return r.getMateReferenceName();
	}

	/**
	 * @param mateReferenceName
	 * @see net.sf.samtools.SAMRecord#setMateReferenceName(java.lang.String)
	 */
	public void setMateReferenceName(String mateReferenceName) {
		r.setMateReferenceName(mateReferenceName);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getMateReferenceIndex()
	 */
	public Integer getMateReferenceIndex() {
		return r.getMateReferenceIndex();
	}

	/**
	 * @param referenceIndex
	 * @see net.sf.samtools.SAMRecord#setMateReferenceIndex(int)
	 */
	public void setMateReferenceIndex(int referenceIndex) {
		r.setMateReferenceIndex(referenceIndex);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getAlignmentStart()
	 */
	public int getAlignmentStart() {
		return r.getAlignmentStart();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setAlignmentStart(int)
	 */
	public void setAlignmentStart(int value) {
		r.setAlignmentStart(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getAlignmentEnd()
	 */
	public int getAlignmentEnd() {
		return r.getAlignmentEnd();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getUnclippedStart()
	 */
	public int getUnclippedStart() {
		return r.getUnclippedStart();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getUnclippedEnd()
	 */
	public int getUnclippedEnd() {
		return r.getUnclippedEnd();
	}

	/**
	 * @param offset
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReferencePositionAtReadPosition(int)
	 */
	public int getReferencePositionAtReadPosition(int offset) {
		return r.getReferencePositionAtReadPosition(offset);
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setAlignmentEnd(int)
	 */
	public void setAlignmentEnd(int value) {
		r.setAlignmentEnd(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getMateAlignmentStart()
	 */
	public int getMateAlignmentStart() {
		return r.getMateAlignmentStart();
	}

	/**
	 * @param mateAlignmentStart
	 * @see net.sf.samtools.SAMRecord#setMateAlignmentStart(int)
	 */
	public void setMateAlignmentStart(int mateAlignmentStart) {
		r.setMateAlignmentStart(mateAlignmentStart);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getInferredInsertSize()
	 */
	public int getInferredInsertSize() {
		return r.getInferredInsertSize();
	}

	/**
	 * @param inferredInsertSize
	 * @see net.sf.samtools.SAMRecord#setInferredInsertSize(int)
	 */
	public void setInferredInsertSize(int inferredInsertSize) {
		r.setInferredInsertSize(inferredInsertSize);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getMappingQuality()
	 */
	public int getMappingQuality() {
		return r.getMappingQuality();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setMappingQuality(int)
	 */
	public void setMappingQuality(int value) {
		r.setMappingQuality(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getCigarString()
	 */
	public String getCigarString() {
		return r.getCigarString();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setCigarString(java.lang.String)
	 */
	public void setCigarString(String value) {
		r.setCigarString(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getCigar()
	 */
	public Cigar getCigar() {
		return r.getCigar();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getCigarLength()
	 */
	public int getCigarLength() {
		return r.getCigarLength();
	}

	/**
	 * @param cigar
	 * @see net.sf.samtools.SAMRecord#setCigar(net.sf.samtools.Cigar)
	 */
	public void setCigar(Cigar cigar) {
		r.setCigar(cigar);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadGroup()
	 */
	public SAMReadGroupRecord getReadGroup() {
		return r.getReadGroup();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getFlags()
	 */
	public int getFlags() {
		return r.getFlags();
	}

	/**
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setFlags(int)
	 */
	public void setFlags(int value) {
		r.setFlags(value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadPairedFlag()
	 */
	public boolean getReadPairedFlag() {
		return r.getReadPairedFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getProperPairFlag()
	 */
	public boolean getProperPairFlag() {
		return r.getProperPairFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadUnmappedFlag()
	 */
	public boolean getReadUnmappedFlag() {
		return r.getReadUnmappedFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getMateUnmappedFlag()
	 */
	public boolean getMateUnmappedFlag() {
		return r.getMateUnmappedFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadNegativeStrandFlag()
	 */
	public boolean getReadNegativeStrandFlag() {
		return r.getReadNegativeStrandFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getMateNegativeStrandFlag()
	 */
	public boolean getMateNegativeStrandFlag() {
		return r.getMateNegativeStrandFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getFirstOfPairFlag()
	 */
	public boolean getFirstOfPairFlag() {
		return r.getFirstOfPairFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getSecondOfPairFlag()
	 */
	public boolean getSecondOfPairFlag() {
		return r.getSecondOfPairFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getNotPrimaryAlignmentFlag()
	 */
	public boolean getNotPrimaryAlignmentFlag() {
		return r.getNotPrimaryAlignmentFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getReadFailsVendorQualityCheckFlag()
	 */
	public boolean getReadFailsVendorQualityCheckFlag() {
		return r.getReadFailsVendorQualityCheckFlag();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getDuplicateReadFlag()
	 */
	public boolean getDuplicateReadFlag() {
		return r.getDuplicateReadFlag();
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setReadPairedFlag(boolean)
	 */
	public void setReadPairedFlag(boolean flag) {
		r.setReadPairedFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setProperPairFlag(boolean)
	 */
	public void setProperPairFlag(boolean flag) {
		r.setProperPairFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setReadUnmappedFlag(boolean)
	 */
	public void setReadUnmappedFlag(boolean flag) {
		r.setReadUnmappedFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setMateUnmappedFlag(boolean)
	 */
	public void setMateUnmappedFlag(boolean flag) {
		r.setMateUnmappedFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setReadNegativeStrandFlag(boolean)
	 */
	public void setReadNegativeStrandFlag(boolean flag) {
		r.setReadNegativeStrandFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setMateNegativeStrandFlag(boolean)
	 */
	public void setMateNegativeStrandFlag(boolean flag) {
		r.setMateNegativeStrandFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setFirstOfPairFlag(boolean)
	 */
	public void setFirstOfPairFlag(boolean flag) {
		r.setFirstOfPairFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setSecondOfPairFlag(boolean)
	 */
	public void setSecondOfPairFlag(boolean flag) {
		r.setSecondOfPairFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setNotPrimaryAlignmentFlag(boolean)
	 */
	public void setNotPrimaryAlignmentFlag(boolean flag) {
		r.setNotPrimaryAlignmentFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setReadFailsVendorQualityCheckFlag(boolean)
	 */
	public void setReadFailsVendorQualityCheckFlag(boolean flag) {
		r.setReadFailsVendorQualityCheckFlag(flag);
	}

	/**
	 * @param flag
	 * @see net.sf.samtools.SAMRecord#setDuplicateReadFlag(boolean)
	 */
	public void setDuplicateReadFlag(boolean flag) {
		r.setDuplicateReadFlag(flag);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getValidationStringency()
	 */
	public ValidationStringency getValidationStringency() {
		return r.getValidationStringency();
	}

	/**
	 * @param validationStringency
	 * @see net.sf.samtools.SAMRecord#setValidationStringency(net.sf.samtools.SAMFileReader.ValidationStringency)
	 */
	public void setValidationStringency(ValidationStringency validationStringency) {
		r.setValidationStringency(validationStringency);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String tag) {
		return r.getAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getIntegerAttribute(java.lang.String)
	 */
	public Integer getIntegerAttribute(String tag) {
		return r.getIntegerAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getShortAttribute(java.lang.String)
	 */
	public Short getShortAttribute(String tag) {
		return r.getShortAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getByteAttribute(java.lang.String)
	 */
	public Byte getByteAttribute(String tag) {
		return r.getByteAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getStringAttribute(java.lang.String)
	 */
	public String getStringAttribute(String tag) {
		return r.getStringAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getCharacterAttribute(java.lang.String)
	 */
	public Character getCharacterAttribute(String tag) {
		return r.getCharacterAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getFloatAttribute(java.lang.String)
	 */
	public Float getFloatAttribute(String tag) {
		return r.getFloatAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getByteArrayAttribute(java.lang.String)
	 */
	public byte[] getByteArrayAttribute(String tag) {
		return r.getByteArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getUnsignedByteArrayAttribute(java.lang.String)
	 */
	public byte[] getUnsignedByteArrayAttribute(String tag) {
		return r.getUnsignedByteArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getSignedByteArrayAttribute(java.lang.String)
	 */
	public byte[] getSignedByteArrayAttribute(String tag) {
		return r.getSignedByteArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getUnsignedShortArrayAttribute(java.lang.String)
	 */
	public short[] getUnsignedShortArrayAttribute(String tag) {
		return r.getUnsignedShortArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getSignedShortArrayAttribute(java.lang.String)
	 */
	public short[] getSignedShortArrayAttribute(String tag) {
		return r.getSignedShortArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getUnsignedIntArrayAttribute(java.lang.String)
	 */
	public int[] getUnsignedIntArrayAttribute(String tag) {
		return r.getUnsignedIntArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getSignedIntArrayAttribute(java.lang.String)
	 */
	public int[] getSignedIntArrayAttribute(String tag) {
		return r.getSignedIntArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#getFloatArrayAttribute(java.lang.String)
	 */
	public float[] getFloatArrayAttribute(String tag) {
		return r.getFloatArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @return
	 * @see net.sf.samtools.SAMRecord#isUnsignedArrayAttribute(java.lang.String)
	 */
	public boolean isUnsignedArrayAttribute(String tag) {
		return r.isUnsignedArrayAttribute(tag);
	}

	/**
	 * @param tag
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String tag, Object value) {
		r.setAttribute(tag, value);
	}

	/**
	 * @param tag
	 * @param value
	 * @see net.sf.samtools.SAMRecord#setUnsignedArrayAttribute(java.lang.String, java.lang.Object)
	 */
	public void setUnsignedArrayAttribute(String tag, Object value) {
		r.setUnsignedArrayAttribute(tag, value);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getAttributes()
	 */
	public List<SAMTagAndValue> getAttributes() {
		return r.getAttributes();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getHeader()
	 */
	public SAMFileHeader getHeader() {
		return r.getHeader();
	}

	/**
	 * @param header
	 * @see net.sf.samtools.SAMRecord#setHeader(net.sf.samtools.SAMFileHeader)
	 */
	public void setHeader(SAMFileHeader header) {
		r.setHeader(header);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getVariableBinaryRepresentation()
	 */
	public byte[] getVariableBinaryRepresentation() {
		return r.getVariableBinaryRepresentation();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getAttributesBinarySize()
	 */
	public int getAttributesBinarySize() {
		return r.getAttributesBinarySize();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getAlignmentBlocks()
	 */
	public List<AlignmentBlock> getAlignmentBlocks() {
		return r.getAlignmentBlocks();
	}

	/**
	 * @param recordNumber
	 * @return
	 * @see net.sf.samtools.SAMRecord#validateCigar(long)
	 */
	public List<SAMValidationError> validateCigar(long recordNumber) {
		return r.validateCigar(recordNumber);
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#hashCode()
	 */
	public int hashCode() {
		return r.hashCode();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#isValid()
	 */
	public boolean isValid() {
		return r.isValid().size() == 0;
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getFileSource()
	 */
	public SAMFileSource getFileSource() {
		return r.getFileSource();
	}

	/**
	 * @return
	 * @see net.sf.samtools.SAMRecord#getSAMString()
	 */
	public String getSAMString() {
		return r.getSAMString();
	}
	
	@Override
	public String toOutput() {
		return getSAMString();
	}

	/**
	 * @return the SAMRecord that this entry wraps
	 */
	public SAMRecord getSAMRecord() {
		return r;
	}

	@Override
	public String getSequence() {
		return getReadString();
	}

	@Override
	public String getQualities() {
		return getBaseQualityString();
	}

}

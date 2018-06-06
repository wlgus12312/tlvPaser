package tlvparser;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ubivelox.gaia.GaiaException;
import com.ubivelox.gaia.util.GaiaUtils;

import exception.UbiveloxException;
import lombok.Data;

@Data
public class TLVParser {

	public enum ValueType {
		PRIMITIVE, CONSTRUCTED;
	}

	private static final Logger logger = LoggerFactory.getLogger(TlvParserTest.class);

	static class TLVResultNBytePosition {
		String tlvResult;
		int byteArrayPosition;

		public TLVResultNBytePosition(final String tlvResult, final int byteArrayPosition) {
			this.tlvResult = tlvResult;
			this.byteArrayPosition = byteArrayPosition;
		}
	}
	
	
	
	
	public static String parse(final String hexStringOrg) throws UbiveloxException, GaiaException {
		
		byte[] byteArrayOrg = GaiaUtils.convertHexaStringToByteArray(hexStringOrg);
		
		return parse1(hexStringOrg,byteArrayOrg, 0, 0);
	}
	

	private static String parse1(final String hexStringOrg, final byte[] byteArrayOrg, final int dept, final int byteArrayPosition2) throws UbiveloxException, GaiaException {
		//GaiaUtils.checkHexaString(hexStringOrg);
		//String hexString = hexStringOrg;
		
		String result = "";
		TLVResultNBytePosition parseOne;
		int tlvIndex = byteArrayPosition2;
		byte[] byteArray = byteArrayOrg;
		int byteArrayPosition = 0;
		int hexStringIndex = 0;
		
		do {
			{
				String outPut = "";
				int deptint = dept;
				String tapDept = "";
		
				ValueType valueType = ValueType.PRIMITIVE;
				byteArrayPosition = tlvIndex; //+ byteArrayPosition2;
				int tSize = 0;
				int lSize = 0;
				int vSize = 0;
				String tagString = "";
				String lengthString = "";
				String valueString = "";
				// primitive와 constructed 구분
				if (((byteArray[byteArrayPosition]) & 0b0010_0000) == 0b0010_0000) {
					valueType = ValueType.CONSTRUCTED;
				} else {
					valueType = ValueType.PRIMITIVE;
				}
				
				System.out.println("wd = "+tlvIndex);
				tSize = getTagSize(byteArray, byteArrayPosition);
				byteArrayPosition += tSize / 2 + tlvIndex;
				if (byteArray.length == byteArrayPosition) {
					throw new UbiveloxException("Length Range is not exist");
				} else {
					lSize = getLengthSize(byteArray, byteArrayPosition);
					System.out.println(byteArrayPosition);
					byteArrayPosition = ((tSize + lSize) / 2) - 1 + tlvIndex;
					
					
					for (int i = 0; i < deptint; i++) {
						tapDept += "\t";
					}
					tagString = hexStringOrg.substring(tlvIndex*2, (tlvIndex*2+tSize));
					lengthString = hexStringOrg.substring((tlvIndex*2+tSize), (tlvIndex*2 +tSize+lSize));
					
					outPut += tapDept + tagString + "\t" + lengthString;
					System.out.println(outPut);
				}
				if (byteArray[byteArrayPosition] == 0) {
					parseOne = new TLVResultNBytePosition(outPut, byteArrayPosition);
					tlvIndex += (tSize + lSize + vSize) / 2;
					
				} else {
					if ((tSize + lSize+ tlvIndex) *2 > hexStringOrg.length()) {
						throw new UbiveloxException("Value Range is not exist");
					}
					vSize = byteArray[byteArrayPosition] * 2;
					if ((tSize + lSize + vSize + tlvIndex*2) >  hexStringOrg.length()) {
						throw new UbiveloxException("Value Range is not enough");
					}

					if (valueType == ValueType.PRIMITIVE) {
						
						valueString = hexStringOrg.substring(tlvIndex * 2 + tSize + lSize, tlvIndex * 2 + tSize + lSize + vSize );
						outPut +=  "\t" + valueString;
						System.out.println(outPut);
						
					} else {
						deptint++;
						outPut += "\n" + parse1(hexStringOrg, byteArray, deptint, tlvIndex + (tSize + lSize) / 2);
						System.out.println(outPut);
					}
					parseOne = new TLVResultNBytePosition(outPut, byteArrayPosition + (vSize / 2 + tlvIndex));
					tlvIndex += (tSize + lSize + vSize) / 2;
				}
			}
			result += (hexStringIndex == 0 ? "" : "\n") + parseOne.tlvResult;
			hexStringIndex = parseOne.byteArrayPosition * 2 + 2;
			
		} while (tlvIndex < (hexStringIndex)/2);
		
		return result;
	}
	

	
	
	static int getTagSize(final byte[] byteArray, int byteArrPos) throws UbiveloxException, GaiaException {
		// checkNLO(byteArray, byteArray == null ? 0 : byteArray.length, "Tag Byte
		// Array");
		GaiaUtils.checkNullOrEmpty(byteArray);

		int tSize = 2;
		System.out.println("byteArrPos = "+byteArrPos);
		if (((byteArray[byteArrPos]) & 0b0001_1111) == 0b0001_1111) {
			tSize += 2;
			for (int i = byteArrPos + 1; (byteArray[i] & 0b1000_0000) == 0b1000_0000; i++) {
				tSize += 2;
				if (tSize >= 6) {
					throw new UbiveloxException("Tag Range Overflow");
				}
			}
		}
		byteArrPos = tSize / 2;
		return tSize;
	}

	static int getLengthSize(final byte[] byteArray, final int byteArrPos) throws UbiveloxException, GaiaException {
		// checkNLO(byteArr, byteArr == null ? 0 : byteArr.length, "Length Byte Array");
		GaiaUtils.checkNullOrEmpty(byteArray);

		int lSize = 2;
		if ((byteArray[byteArrPos] & 0xff) > 0x81) {
			throw new UbiveloxException("Length Range Overflow");
		} else if ((byteArray[byteArrPos] & 0xff) == 0x81) {
			logger.info("0x81 들어감");
			lSize += 2;

			if (byteArrPos + 1 >= byteArray.length) {
				throw new UbiveloxException("Length is not enough");
			}
			
		}

		return lSize;
	}

	
	
	
	
}

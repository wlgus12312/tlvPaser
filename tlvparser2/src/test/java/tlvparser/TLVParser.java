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
		
		return parse1(byteArrayOrg, 0, 0);
	}
	

	private static String parse1(final byte[] byteArrayOrg, final int dept, final int byteArrayPosition2) throws UbiveloxException, GaiaException {
		//GaiaUtils.checkHexaString(hexStringOrg);
		//String hexString = hexStringOrg;
		
		String result = "";
		TLVResultNBytePosition parseOne;
		int tlvIndex = 0;
		byte[] byteArray = byteArrayOrg;
		int byteArrayPosition=0;
		int ndept = 0;

		do {
			String outPut = "";
			int deptint = dept;
			String tapDept = "";
			{
		
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
				tSize = getTagSize(byteArray, byteArrayPosition);
				//byteArrayPosition += tSize / 2;
				for (int i = byteArrayPosition; i < byteArrayPosition+tSize; i++) {
					tagString += GaiaUtils.convertByteToHexaString(byteArray[i]);
				}
				byteArrayPosition += tSize;
				System.out.println("태그 이후의 바이트어레이포지션 = " + byteArrayPosition);
				if (byteArray.length == byteArrayPosition) {
					//throw new UbiveloxException("Length Range is not exist");
				} else {
					lSize = getLengthSize(byteArray, byteArrayPosition);
					//byteArrayPosition = ((tSize + lSize) / 2) - 1 + tlvIndex;
					for (int i = 0; i < deptint; i++) {
						tapDept += "\t";
					}
					if(lSize == 0) {
						//lengthString = "00";
					}else {
						
						for (int i = byteArrayPosition; i < byteArrayPosition + tSize + lSize -1; i++) {
							lengthString += GaiaUtils.convertByteToHexaString(byteArray[i]);
							System.out.println("랭스 = " + i);
						}
						//outPut += tapDept + hexString.substring(0, tSize) + "\t" + hexString.substring(tSize, tSize + lSize);
					}
					byteArrayPosition += ((tSize + lSize)) - 1;
					System.out.println("렝스 이후의 바이트어레이포지션 = " + byteArrayPosition);
					outPut += tapDept + tagString + "\t" + lengthString;
					System.out.println(outPut);
				}
				
				//if (byteArray[byteArrayPosition] == 0) {
				if (lSize == 0) {
					parseOne = new TLVResultNBytePosition(outPut, byteArrayPosition);
					//tlvIndex += (tSize + lSize + vSize) / 2;
					tlvIndex += (tSize + lSize + vSize);
					
				} else {
					//hexString.length() > byteArray.length
					if ((tSize + lSize) == byteArray.length+1) {
						throw new UbiveloxException("Value Range is not exist");
					}
					//vSize = byteArray[byteArrayPosition] * 2;
					vSize = byteArray[byteArrayPosition-1];
					System.out.println("vSIze = " + vSize);
					//hexString.length() > byteArray.length
					if ((tSize + lSize + vSize) > byteArray.length+1) {
						throw new UbiveloxException("Value Range is not enough");
					}

					if (valueType == ValueType.PRIMITIVE) {
						for (int i = (byteArrayPosition); i < (byteArrayPosition+(tSize + lSize + vSize))-2; i++) {
							valueString += GaiaUtils.convertByteToHexaString(byteArray[i]);
							System.out.println(valueString + " : " + i);
						}
						//outPut +=  "\t" + hexString.substring((tSize + lSize), (tSize + lSize + vSize));
						outPut +=  "\t" + valueString;
					} else {
						deptint++;
						//valueValue = byteToHexaString(byteBuffer.get(byteArray, (tSize + lSize), (tSize + lSize + vSize)));
						//outPut += "\n" + parse1(hexString.substring((tSize + lSize), (tSize + lSize + vSize)), deptint);
						outPut += "\n" + parse1(byteArray, deptint, byteArrayPosition);
					}
					//parseOne = new TLVResultNBytePosition(outPut, byteArrayPosition + (vSize / 2));
					parseOne = new TLVResultNBytePosition(outPut, (byteArrayPosition + (vSize)));
					//tlvIndex += (tSize + lSize + vSize) / 2;
					tlvIndex += (tSize + lSize + vSize);
					System.out.println("tlvIndex = "+ tlvIndex);
				}
			}
			
			result += (ndept == 0 ? "" : "\n") + parseOne.tlvResult;
			System.out.println(result);
			//hexStringIndex = parseOne.byteArrayPosition * 2 + 2;
			System.out.println("파스원 바이트포지션"+parseOne.byteArrayPosition);
			byteArrayPosition = parseOne.byteArrayPosition;
			//hexString = hexStringOrg.substring(hexStringIndex);
		//} while (!hexString.isEmpty());
			ndept++;
		} while (tlvIndex < byteArray.length-1);
		
		return result;
	}
	

	
	
	static int getTagSize(final byte[] byteArray, int byteArrPos) throws UbiveloxException, GaiaException {
		// checkNLO(byteArray, byteArray == null ? 0 : byteArray.length, "Tag Byte
		// Array");
		GaiaUtils.checkNullOrEmpty(byteArray);

		//int tSize = 2;
		int tSize = 1;

		if (((byteArray[byteArrPos]) & 0b0001_1111) == 0b0001_1111) {
			tSize += 1;
			for (int i = byteArrPos + 1; (byteArray[i] & 0b1000_0000) == 0b1000_0000; i++) {
				tSize += 1;
				if (tSize >= 6) {
					throw new UbiveloxException("Tag Range Overflow");
				}
			}
		}
		//byteArrPos = tSize / 2;
		byteArrPos = tSize;
		return tSize;
	}

	static int getLengthSize(final byte[] byteArray, final int byteArrPos) throws UbiveloxException, GaiaException {
		// checkNLO(byteArr, byteArr == null ? 0 : byteArr.length, "Length Byte Array");
		GaiaUtils.checkNullOrEmpty(byteArray);

		int lSize = 1;

		if ((byteArray[byteArrPos] & 0xff) > 0x81) {
			throw new UbiveloxException("Length Range Overflow");
		} else if ((byteArray[byteArrPos] & 0xff) == 0x81) {
			logger.info("0x81 들어감");
			lSize += 1;

			if (byteArrPos + 1 >= byteArray.length) {
				throw new UbiveloxException("Length is not enough");
			}
		}

		return lSize;
	}

	
	
	
	
}

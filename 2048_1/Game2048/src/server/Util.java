/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package server;

import java.io.File;
import java.io.FilenameFilter;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author Dongle
 */
public class Util {
    
    public static final String MD5 = "MD5";
    public static final String SHA1 = "SHA1";
    
    private static final Random random = new Random();
    
    public static FilenameFilter createFilenameFilter(String ext)
    {
        return new myFilenameFilter(ext);
    }
    
    public static int getRandomInt(int bound)
    {
        return random.nextInt(bound);
    }
    
    public static String getTime()
    {
        Date date = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	return formatter.format(date);      
    }
    
    public static String encodeByMD5(String str) {
		return EncoderHandler.encodeByMD5(str);
    }
    
    public static String encodeBySHA1(String str) {
		return EncoderHandler.encodeBySHA1(str);
    }
    
    private static class EncoderHandler {

	private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
         
	/**
	 * encode string
	 *
	 * @param algorithm
	 * @param str
	 * @return String
	 */
	public static String encode(String algorithm, String str) {
		if (str == null) {
			return null;
		}
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.update(str.getBytes());
			return getFormattedText(messageDigest.digest());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * encode By MD5
	 *
	 * @param str
	 * @return String
	 */
	public static String encodeByMD5(String str) {
		return encode(MD5, str);
	}
        
        /**
	 * encode By SHA1
	 *
	 * @param str
	 * @return String
	 */
	public static String encodeBySHA1(String str) {
		return encode(SHA1, str);
	}

	/**
	 * Takes the raw bytes from the digest and formats them correct.
	 *
	 * @param bytes
	 *            the raw bytes from the digest.
	 * @return the formatted bytes.
	 */
	private static String getFormattedText(byte[] bytes) {
		int len = bytes.length;
		StringBuilder buf = new StringBuilder(len * 2);
		// ������ת����ʮ�����Ƶ��ַ�����ʽ
		for (int j = 0; j < len; j++) { 			buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
			buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
		}
		return buf.toString();
	}

    }
    

    /**
     * �̳�FileFilter�ӿڵ��ļ�������
     * @author nileader
     * @see http://www.nileader.cn
     */
    private static class myFilenameFilter  implements FilenameFilter{
            public String  dat;     //�������չ��
            public String getDat() {
                    return dat;
            }
            public void setDat(String dat) {
                    this.dat = dat;
            }
            /**
             * ���췽��
             */
            public myFilenameFilter(String dat){
                    this.setDat(dat);
            }
            /**
             * ���˵ķ���
             * @param file ����ѯ���ļ�����
             * @return �Ƿ����ָ���ļ�
             */
            public boolean accept(File dir, String fileName) {

                    //�Ի�ȡ���ļ�ȫ�����в��
                    String[] arrName = fileName.split("\\.");
                    if(arrName[1].equalsIgnoreCase( this.getDat() ) )
                    {
                            return true;
                    }
                    return false;
            }
    }
}



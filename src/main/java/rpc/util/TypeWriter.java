package rpc.util;

public class TypeWriter {
    /** 
     * 整型转换为4位字节数组 
     * @param intValue 
     * @return 
     */  
    public static byte[] int2Byte(int intValue) {  
        byte[] b = new byte[4];  
        for (int i = 0; i < 4; i++) {  
            b[i] = (byte) (intValue >> 8 * (3 - i) & 0xFF);  
        }  
        return b;  
    }  
    /** 
     * 4位字节数组转换为整型 
     * @param b 
     * @return 
     */  
    public static int byte2Int(byte[] b) {  
        int intValue = 0;  
        for (int i = 0; i < b.length; i++) {  
            intValue += (b[i] & 0x000000FF) << (8 * (3 - i));  
        }  
        return intValue;  
    }  


}

package rpc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ReadDataUtil {

	
	/**
	 * 
	 * @param input 输入流
	 * @param finish 结束符标志
	 * @param timeOutReadTime 超时再读次数
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String readDataForExcuteCmd(InputStream input, String finish, int timeOutReadTime) throws UnsupportedEncodingException{
		StringBuffer sb = new StringBuffer();
		byte[] t = new byte[64];
		int size = -1;
		int tp = 0;
		while(true){
			if(timeOutReadTime < 0){
				return "false";
			}
			try {
				size = input.read();
				byte b = (byte) size;
	            if(-1 != size) {
	            	if(Character.isValidCodePoint(b)){
	        			if(tp>0){
	        				sb.append(new String(t, 0, tp,"GBK"));
	        				tp=0;
	        			}
	        			sb.append((char)b);
	        		}else{
	        			if(tp == 64){
	        		   		sb.append(new String(t,0,tp,"GBK"));
	    		    		tp = 0;
	        			}
	        			t[tp++] = b;
	        		}
	            }
			} catch (IOException e) {//超时1s
		    	if(tp > 0){
		    		sb.append(new String(t,0,tp,"GBK"));
		    		tp = 0;
		    	}
                if(sb.toString().trim().endsWith(finish)) {
                	return sb.toString();		
                }
                timeOutReadTime --;
			}
		}
	}
	
	public static byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = -1;
        while((len=inStream.read(buffer))!=-1){
            outStream.write(buffer,0,len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }


}

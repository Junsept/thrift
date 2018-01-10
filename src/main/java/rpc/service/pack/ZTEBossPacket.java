package rpc.service.pack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rpc.service.exception.PacketReadLenException;
import rpc.util.TypeWriter;

public class ZTEBossPacket {
	static Logger log = LoggerFactory.getLogger(ZTEBossPacket.class);
	private static byte[] start = new byte[]{(byte)0xAA, 0x55,  (byte)0xAA, 0x55}; //消息开始标志
	private static byte[] type = new byte[]{0x01};
	private static byte[] msglen = new byte[4];
	private static byte[] version = new byte[]{0x00, 0x00, 0x00, 0x01}; //版本号
	private static byte[] hostmac = new byte[]{0x30, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};//网元标识
	private static byte[] sessionId = new byte[]{0x00, 0x00, 0x00, 0x01}; //事务Id
	private static byte[] result = new byte[]{0x00, 0x00, 0x00, 0x00}; //执行结果
	private static byte[] houxpacket = new byte[]{0x00}; //是否有后继包 0 – 不存在后序包 1 – 存在后序包
	private static byte[] nowpacket = new byte[]{0x00, 0x00}; //当前包序列号
	private static byte[] param = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };//保留字段
	
	private static StringBuffer sb = new StringBuffer();
	
	public static boolean getStatus(){
		return TypeWriter.byte2Int(result) == 0;
	}
	
	public static void write(OutputStream output, String cmd) throws IOException{
		String nessStr = cmd.substring(0, cmd.indexOf("|"));//与上层约定“网元标志字符串|具体的下发指令”
		StringBuffer sb = new StringBuffer();
		sb.append(nessStr);
		for(int i = nessStr.length(); i < 8; i ++){
			sb.append(" ");
		}
		cmd = cmd.substring(cmd.indexOf("|") + 1);
		byte[] nessBytes = sb.toString().getBytes();
		init(nessBytes);
		int msglenInt = cmd.getBytes().length + 39;
		msglen = TypeWriter.int2Byte(msglenInt);
		output.write(start);
		output.write(type);
		output.write(msglen);
		output.write(version);
		output.write(hostmac);
		output.write(sessionId);
		output.write(result);
		output.write(houxpacket);
		output.write(nowpacket);
		output.write(param);
		output.write(cmd.getBytes());
	}
	static void read(InputStream input,byte[] arr) throws IOException{
		for(int i=0;i<arr.length;i++){
			arr[i] = (byte)input.read();
		}
	}
	static void readStart(InputStream input) throws IOException{
		int index = 0;
		while(index < start.length){
			byte b = (byte) input.read();
			if(b == start[index]){
				index ++;				
			}else{
				index =0;
			}
		}	
	}
	public static String read(InputStream input,PacketReadLenException exe) throws IOException, PacketReadLenException{
		String temp;
		readStart(input);
		read(input, type);
		read(input, msglen);
		read(input, version);
		read(input, hostmac);
		read(input, sessionId);
		read(input, result);
		read(input, houxpacket);
		read(input, nowpacket);
		read(input, param);
		int msglenInt = TypeWriter.byte2Int(msglen);
		if(msglenInt<39){
			throw (exe==null)?new PacketReadLenException():exe;
		}
		byte[] msgByte = new byte[msglenInt - 39];
		read(input, msgByte);
		sb.append(new String(msgByte, "GBK"));
		while(true){
			if((int)houxpacket[0] == 0){//无后续包
				temp = sb.toString();
				sb = new StringBuffer();
				break;
			}
			read(input,exe);
		}
		return temp;
	}
	
	public static void init(byte[] nessBytes){
		start = new byte[]{(byte)0xAA, 0x55,  (byte)0xAA, 0x55}; //消息开始标志
		type = new byte[]{0x01};
		msglen = new byte[4];
		version = new byte[]{0x00, 0x00, 0x00, 0x01}; //版本号
		hostmac = nessBytes;//网元标识
		sessionId = new byte[]{0x00, 0x00, 0x00, 0x01}; //事务Id
		result = new byte[]{0x00, 0x00, 0x00, 0x00}; //执行结果
		houxpacket = new byte[]{0x00}; //是否有后继包 0 – 不存在后序包 1 – 存在后序包
		nowpacket = new byte[]{0x00, 0x00}; //当前包序列号
		param = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, };//保留字段		
	}
	
	public static void main(String[] args) {
		System.out.println(getStatus());
	}
}

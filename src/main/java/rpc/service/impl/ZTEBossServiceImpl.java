package rpc.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rpc.core.model.EndPoint;
import rpc.core.model.Result;
import rpc.service.pack.*;
import rpc.service.Iface.*;
import rpc.service.exception.PacketReadLenException;
import rpc.util.Pool;
import rpc.util.ReadDataUtil;

public class ZTEBossServiceImpl implements Iface{
	private Socket socket = null;
	private InputStream input = null;       
	private OutputStream output = null;
	private int timeout = 60 * 1000;
	Result result = new Result();
	Logger logger = LoggerFactory.getLogger(ZTEBossServiceImpl.class);
	Pool<Socket> pool = new Pool<Socket>(1000, 10*60*1000, new Pool.Clear<Socket>() {
		@Override
		public void clear(Socket e) {
			try {
				if(!e.isClosed())
					e.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	});
	public List<Result> connectAndExcute(EndPoint endPoint, List<String> cmd) throws TException {
		List<Result> resultList = new ArrayList<Result>();
		try{
			String sessionId = connect(endPoint).getMessage().toString();	
			for(String oneCmd : cmd){
				Result resultTemp = new Result();
				excuteCMD(oneCmd, sessionId);
				resultTemp.setStatus(result.isStatus());
				resultTemp.setMessage(result.getMessage());
				resultList.add(resultTemp);
			}
			close(sessionId);
		}catch(Exception e){
			result.setStatus(false);
			result.setMessage(e.getMessage());
			logger.error(e.getMessage());
			resultList.add(result);
		}
		return resultList;
	}

	public Result connect(EndPoint endPoint) throws TException {
		if(check(endPoint).isStatus()){
			try {
				logger.info("准备连接：" + endPoint.hostname + ":" + endPoint.port);
				socket = new Socket(endPoint.hostname, endPoint.port);
				socket.setSoTimeout(timeout);
				output = socket.getOutputStream();
				input = socket.getInputStream();
				
				result.setStatus(true);
				String sesssionId = pool.put(socket);
				result.setMessage(sesssionId);
				logger.info("连接成功!");
			} catch (Exception e) {
				result.setStatus(false);
				result.setMessage("连接失败!" + e.getMessage());
				logger.error(e.getMessage());
				e.printStackTrace();
			} 
		}
		return result;
	}
	private Result excuteCMD(String cmd, String sessionId,PacketReadLenException exe){
		try {
			if(pool.get(sessionId) != null){
				output = pool.get(sessionId).getOutputStream();
				input = pool.get(sessionId).getInputStream();
				
				ZTEBossPacket.write(output, cmd);
				String message = ZTEBossPacket.read(input,exe);
				if(ZTEBossPacket.getStatus()){
					result.setStatus(true);
					result.setMessage(message);
					logger.info("执行命令成功!");
				}else{
					result.setStatus(false);
					result.setMessage(message);
					logger.info("执行命令失败!");
				}

			}else{
				result.setStatus(false);
				result.setMessage("连接已关闭!请重启");
				logger.error("连接已关闭!请重启");
			}
		}catch (PacketReadLenException e) {
			if(e.isRetry()){
				System.out.println("失败重试!");
				return excuteCMD(cmd,sessionId,e);
			}
			result.setStatus(false);
			result.setMessage(e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			result.setStatus(false);
			result.setMessage(e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	public Result excuteCMD(String cmd, String sessionId) throws TException {
		return excuteCMD(cmd,sessionId,null);
	}

	public Result close(String sessionId) throws TException {
		try{
			socket = pool.get(sessionId);
			input = socket.getInputStream();
			output = socket.getOutputStream();
			pool.remove(sessionId);
			disInputStream();
			disOutputStream();
			disConnect();
		}catch(Exception e){
			result.setStatus(false);
			result.setMessage(e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/*一些辅助方法*/
	public boolean isConnected() {
		return socket!=null && socket.isConnected() && !socket.isClosed() && input!=null && output!=null;
	}
	
	public boolean isClosed() {
		return socket==null || socket.isClosed() || !socket.isConnected() || input==null || output==null;
	}
	
	public void disInputStream(){
		if(input!=null)
		try {
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disOutputStream(){
		if(output!=null)
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disConnect(){
		if(socket!=null && socket.isConnected()){
			try {
				logger.info("准备关闭连接!");
				socket.close();
				result.setStatus(true);
				result.setMessage("连接关闭!");
				logger.info("连接关闭!");
			} catch (IOException e) {
				e.printStackTrace();
				result.setStatus(false);
				result.setMessage("连接关闭失败!" + e.getMessage());
				logger.error(e.getMessage());
			}	
		}
	}
	
	private Result check(EndPoint endPoint) {
		if(endPoint.getHostname() == null || endPoint.getHostname().equals("")){
			result.setStatus(false);
			result.setMessage("IP地址为空!");
			logger.error("IP地址为空!");
			return result;
		}else if(String.valueOf(endPoint.getPort()) == null || String.valueOf(endPoint.getPort()).equals("") || endPoint.getPort() == 0){
			result.setStatus(false);
			result.setMessage("端口为空!");
			logger.error("端口为空!");
			return result;
		}else if(endPoint.getUsername() == null || endPoint.getUsername().equals("")){
			result.setStatus(false);
			result.setMessage("用户名为空!");
			logger.error("用户名为空!");
			return result;
		}else if(endPoint.getPassword() == null || endPoint.getPassword().equals("")){
			result.setStatus(false);
			result.setMessage("密码为空!");
			logger.error("密码为空!");
			return result;
		}else{
			result.setStatus(true);
			return result;
		}
	}
}

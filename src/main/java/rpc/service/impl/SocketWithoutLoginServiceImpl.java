package rpc.service.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rpc.core.model.EndPoint;
import rpc.core.model.Result;
import rpc.service.*;
import rpc.service.Iface.*;
import rpc.util.Pool;
import rpc.util.ReadDataUtil;
public class SocketWithoutLoginServiceImpl implements Iface{
	private class SocketObject{
		private Socket socket ;
		private InputStream input;
		private OutputStream output;
		private int timeout = 1000;
		private String finish ;
	}
	Result result = new Result();
	Logger logger = LoggerFactory.getLogger(SocketWithoutLoginServiceImpl.class);
	Pool<SocketObject> pool = new Pool<SocketObject>(1000, 10*60*1000, new Pool.Clear<SocketObject>() {
		@Override
		public void clear(SocketObject e) {
			try {
				if(!e.socket.isClosed())
					e.socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	});
	
	
	public List<Result> connectAndExcute(EndPoint endPoint, List<String> cmd) throws TException {
		List<Result> resultList = new ArrayList<Result>();
		String sessionId = connect(endPoint).getMessage().toString();	
		if(isConnected(pool.get(sessionId))){
			for(String oneCmd : cmd){
				Result resultTemp = new Result();
				excuteCMD(oneCmd, sessionId);
				resultTemp.setStatus(result.isStatus());
				resultTemp.setMessage(result.getMessage());
				resultList.add(resultTemp);
			}
			close(sessionId);
		}else{
			resultList.add(result);
		}
		return resultList;
	}

	public Result connect(EndPoint endPoint) throws TException {
		SocketObject obj = new SocketObject();
		obj.finish = endPoint.getFinish();//标识符保存
		if(check(endPoint).isStatus()){
			try {
				logger.info("准备连接：" + endPoint.hostname + ":" + endPoint.port);
				obj.socket = new Socket(endPoint.hostname, endPoint.port);
				obj.socket.setSoTimeout(obj.timeout);
				obj.output = obj.socket.getOutputStream();
				obj.input = obj.socket.getInputStream();
	        	
				if(isConnected(obj)){
					result.setStatus(true);
					result.setMessage("连接成功!");
					String sesssionId = pool.put(obj);
					result.setMessage(sesssionId);
					logger.info("连接成功!");
				}else{
					result.setStatus(false);
					result.setMessage("连接失败!");
					logger.error("连接失败!");
				}
			} catch (Exception e) {;
				result.setStatus(false);
				result.setMessage("连接失败!" + e.getMessage());
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

		return result;
	}

	public Result excuteCMD(String cmd, String sessionId) throws TException {
		try {
			if(pool.get(sessionId) != null){
				SocketObject obj = pool.get(sessionId);
				obj.output = obj.socket.getOutputStream();
				obj.input = obj.socket.getInputStream();
				logger.info("准备执行命令：" + cmd);
				PrintStream pStream = new PrintStream(obj.output); 
				pStream.println(cmd + "\n");
				pStream.flush();	
				
				String message = ReadDataUtil.readDataForExcuteCmd(obj.input, obj.finish, 10);
	            
				if(("false").equals(message)){
					result.setStatus(false);
					result.setMessage("命令执行失败!返回结果中为空或无法匹配finish标志");
					logger.info("命令执行失败!返回结果中为空或无法匹配finish标志");
					return result;
				}
				
				result.setStatus(true);
				result.setMessage(message);
				logger.info("命令执行完毕!");
			}else{
				result.setStatus(false);
				result.setMessage("连接已关闭!请重启");
				logger.error("连接已关闭!请重启");
			}
		} catch (IOException e) {
			result.setStatus(false);
			result.setMessage(e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	public Result close(String sessionId) throws TException {
		try{
			SocketObject obj = pool.get(sessionId);
			pool.remove(sessionId);
			disInputStream(obj);
			disOutputStream(obj);
			disConnect(obj);
		}catch(Exception e){
			result.setStatus(false);
			result.setMessage(e.getMessage());
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/*一些辅助方法*/
	public boolean isConnected(SocketObject obj) {
		return obj.socket!=null && obj.socket.isConnected() && !obj.socket.isClosed() && obj.input!=null && obj.output!=null;
	}
	
	public boolean isClosed(SocketObject obj) {
		return obj.socket==null || obj.socket.isClosed() || !obj.socket.isConnected() || obj.input==null || obj.output==null;
	}
	
	public void disInputStream(SocketObject obj){
		if(obj.input!=null)
		try {
			obj.input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disOutputStream(SocketObject obj){
		if(obj.output!=null)
		try {
			obj.output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disConnect(SocketObject obj){
		if(obj.socket!=null && obj.socket.isConnected()){
			try {
				logger.info("准备关闭连接!");
				obj.socket.close();
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
		}else if(endPoint.getFinish() == null || endPoint.getFinish().equals("")){
			result.setStatus(false);
			result.setMessage("finish标识为空!");
			logger.error("finish标识为空!");
			return result;
		}else{
			result.setStatus(true);
			return result;
		}
	}
		



}

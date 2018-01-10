package rpc.service.exception;

public class PacketReadLenException extends Exception{
	
	private int retryTime = 1;
	
	public boolean isRetry(){
		return (retryTime --)>0;
	}
	
}

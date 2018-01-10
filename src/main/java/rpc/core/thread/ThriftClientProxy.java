package rpc.core.thread;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rpc.core.thread.thriftpool.ConnectionManager;


public class ThriftClientProxy {
	private Logger logger = LoggerFactory.getLogger(ThriftClientProxy.class);
    private ConnectionManager connectionManager;  
    
    public ConnectionManager getConnectionManager() {  
        return connectionManager;  
    }  
    public void setConnectionManager(ConnectionManager connectionManager) {  
        this.connectionManager = connectionManager;  
    }
    
	public Object getClient(Class clazz) {
        Object result = null;  
        TTransport transport = null;
        try {  
            transport = connectionManager.getSocket();  
            TProtocol protocol = new TBinaryProtocol(transport);  
            //多接口服务
            TMultiplexedProtocol tMultiplexedProtocol = new TMultiplexedProtocol(protocol,clazz.getName());
            Class client = Class.forName(clazz.getName()+ "$Client");           
            Constructor con = client.getConstructor(TProtocol.class);  
            result = con.newInstance(tMultiplexedProtocol);  
        } catch(Exception e){
        	if(transport != null){
        		transport.close();
        	}
        	logger.error(e.getMessage());
        }
        return result;  
    }  
}

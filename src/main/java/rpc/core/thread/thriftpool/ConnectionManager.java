package rpc.core.thread.thriftpool;

import org.apache.thrift.transport.TSocket;  
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.stereotype.Service;  
  
/** 
 * 连接池管理 
 */  
@Service  
public class ConnectionManager {  
	private Logger logger = LoggerFactory.getLogger(getClass());
    /** 保存local对象 */  
    ThreadLocal<TSocket> socketThreadSafe = new ThreadLocal<TSocket>();  
  
    /** 连接提供池 */  
    private ConnectionProvider connectionProvider;  
  
    public TSocket getSocket() {  
        TSocket socket = null;  
        try {  
            socket = connectionProvider.getConnection();  
            socketThreadSafe.set(socket);  
            return socketThreadSafe.get();  
        } catch (Exception e) { 
        	if(socket != null){
        		socket.close();
        	}
            logger.error("error ConnectionManager.invoke()", e);  
        } finally {  
            connectionProvider.returnCon(socket);  
            socketThreadSafe.remove();  
        }  
        return socket;  
    }

	public ThreadLocal<TSocket> getSocketThreadSafe() {
		return socketThreadSafe;
	}

	public void setSocketThreadSafe(ThreadLocal<TSocket> socketThreadSafe) {
		this.socketThreadSafe = socketThreadSafe;
	}

	public ConnectionProvider getConnectionProvider() {
		return connectionProvider;
	}

	public void setConnectionProvider(ConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
	}  
  
}  
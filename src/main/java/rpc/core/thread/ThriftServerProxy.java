package rpc.core.thread;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import rpc.core.model.Service;
import rpc.service.Iface.*;

import java.lang.reflect.Constructor;
import java.util.List;

public class ThriftServerProxy {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static class HolderClass {
        private final static ThriftServerProxy instance = new ThriftServerProxy();
    }

    public static ThriftServerProxy getInstance() {
        return HolderClass.instance;
    }

    private ThriftServerProxy() {
    }

    private static int port;

    private static List<Service> rpcServices;


    private Thread thriftThread;

    public void setPort(int port) {
        ThriftServerProxy.port = port;
    }

    public static int getPort() {
        return port;
    }

    public static void setRpcServices(List<Service> rpcServices) {
        ThriftServerProxy.rpcServices = rpcServices;
    }

    public static List<Service> getRpcServices() {
        return rpcServices;
    }

    public void start() {
        thriftThread = new Thread(new Runnable() {
            public void run() {
                if(rpcServices == null || rpcServices.isEmpty()){
                    logger.warn("No RPC service init.");
                    return;
                }
                TServerSocket serverTransport = null;
                TServer server = null;
                TMultiplexedProcessor tMultiplexedProcessor = new TMultiplexedProcessor();
                try {
                    serverTransport = new TServerSocket(getPort());
                    TBinaryProtocol.Factory protFactory = new TBinaryProtocol.Factory(true, true);
                    TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
                    args.protocolFactory(protFactory);
                    args.processor(tMultiplexedProcessor);
                    server = new TThreadPoolServer(args);
                    //注册多个TMultiplexedProcessor
                    for (Service service : rpcServices) {
                        // 实现类处理类class
                        Class processorClass = null;
                        processorClass = Class.forName(service.getServiceInterface() + "$Processor");
                        // 接口
//                        Class iface = Class.forName(service.getServiceInterface());
                        Class iface =  Iface.class;
                        // 接口构造方法类
                        Constructor con = processorClass.getConstructor(iface);
                        TProcessor processor = (TProcessor) con.newInstance(service.getServiceImplObject());
                        tMultiplexedProcessor.registerProcessor(service.getServiceInterface(), processor);
                        logger.info("RPC service {} init.", service.getServiceInterface());
                       
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                Assert.notNull(serverTransport, "RPC start server error……");
                logger.info("Starting RPC server on port " + getPort() + " ...");
                server.serve();

            }
        });

        thriftThread.setDaemon(true);
        thriftThread.start();
    }

    public void stop() {
        thriftThread.interrupt();
        try {
            thriftThread.join();
            logger.info("Thrift service paused.");
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

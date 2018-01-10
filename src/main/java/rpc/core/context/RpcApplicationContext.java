package rpc.core.context;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import rpc.core.thread.ThriftServerProxy;

public class RpcApplicationContext implements ApplicationContextAware {


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    // ---------------------- init + destroy ----------------------
    public void init() throws Exception {
        ThriftServerProxy.getInstance().start();
        System.out.println("端口：" + ThriftServerProxy.getPort());
    }

    public void destroy(){
        ThriftServerProxy.getInstance().stop();
    }
}

package datamakethrift;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import rpc.core.model.EndPoint;
import rpc.core.model.Result;
import rpc.core.thread.ThriftClientProxy;
import rpc.service.SocketService;
import rpc.service.SocketWithoutLoginService;
import rpc.service.TelnetService;
import rpc.service.TelnetWithoutLoginService;
import rpc.service.ZTEBossService;
import rpc.service.Iface.*;


public class RPCClientTest {
	static Result result = null;
	public static void main(String[] args) throws TException {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationcontext-rpc-client.xml");
		ThriftClientProxy proxy = (ThriftClientProxy) context.getBean("thriftClientProxy");
				
		
		//华为DC0
		EndPoint endPoint = new EndPoint();
		endPoint.setHostname("132.129.5.76");
		endPoint.setPort(9818);
		endPoint.setUsername("NOC_lanjie");
		endPoint.setPassword("shnoc123");
		endPoint.setFinish(";");
		Iface client =  (Iface) proxy.getClient(SocketWithoutLoginService.class);
		String message = client.connect(endPoint).getMessage();
		System.out.println(message);
		System.out.println(client.excuteCMD("LOGIN-NIS:North::10000::USRNAME=\"NOC_lanjie\",USRPWD=\"shnoc123\";", message));
		System.out.println(client.excuteCMD("LST-VERS:ServiceDM::4966::COMPONENT=\"ALL\";", message));
		System.out.println(client.excuteCMD("LST-VERS:TID::CTAG::COMPONENT=\"ALL\";", message));
		System.out.println(client.excuteCMD("LOGOUT-NIS:North::10002::;", message));
		
		
/*		EndPoint endPoint = new EndPoint();
		endPoint.setHostname("132.129.5.9");
		endPoint.setPort(5321);
		endPoint.setUsername("bosstest");
		endPoint.setPassword("bosstest");
		Iface client =  (Iface) proxy.getClient(ZTEBossService.class);
		String sessionId = client.connect(endPoint).getMessage();
		System.out.println(sessionId);

		System.out.println(client.excuteCMD("0|90000:1=\"bosstest\",2=\"bosstest\";", sessionId));

		System.out.println(client.excuteCMD("0|90001", sessionId));
		
		System.out.println(client.close(sessionId));*/
	}
}

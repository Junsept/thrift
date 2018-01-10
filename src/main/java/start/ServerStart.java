package start;

import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ServerStart extends Thread {
	public static void main(String[] args) throws Exception {
		Runnable r = new Runnable() {
			ApplicationContext context = new ClassPathXmlApplicationContext("spring-rpc.xml");												
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
					}
				}
			}

		};

		Thread t = new Thread(r);
		//t.setDaemon(true);
		t.start();
		Thread.sleep(3000);
	}
}


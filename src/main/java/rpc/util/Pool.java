package rpc.util;

import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Pool<P>{
	/**
	 * 池最大容量：默认 Integer.MAX_VALUE;
	 */
	private int maxsize = Integer.MAX_VALUE;
	/**
	 * 最大不活跃时间（超过该时间会被清除）
	 * 单位：毫秒    默认：30分钟
	 */
	private long maxtime = 30*60*1000; 
	/**
	 * 定时清除周期
	 * 单位：毫秒    默认：1分钟
	 */
	long seedtime = 60000L;
	/**
	 * 元素集
	 */
	private ConcurrentHashMap<String, Element<P>> elements;
	
	private Clear<P> clear;
	private Thread clearThread;
	
	public Pool(){
		this.elements = new ConcurrentHashMap<String, Element<P>>(maxsize);
	}
	
	/**
	 * 
	 * @param maxsize 最大容量
	 * @param maxtime 最大不活跃时间
	 * @param clear 清除处理函数
	 */
	public Pool(final int maxsize,final long maxtime,final Clear<P> clear){
		this.maxsize = maxsize;
		this.maxtime = maxtime;
		this.clear = clear;
		this.elements = new ConcurrentHashMap<String, Element<P>>(maxsize);
		startClear();
	}
	/**
	 * 当前已存数量
	 * @return
	 */
	public int size(){
		return this.elements.size();
	}
	public P remove(String key){
		Element<P> ele = this.elements.remove(key);
		if(ele == null){
			return null;
		}
		return ele.get();
	}
	public String put(P p){
		if(this.elements.size() >= this.maxsize){
			System.err.println("超过最大容量");
			return null;
		}
		Element<P> ele = new Element<P>(p);
		String key = getRandomKey();
		while(elements.containsKey(key)){
			key = getRandomKey();
		}
		elements.put(key, ele);
		return key;
	}
	
	public boolean put(String sessionId, P p){
		if(this.elements.contains(sessionId)){
			return false;
		}
		Element<P> ele = new Element<P>(p);
		this.elements.put(sessionId, ele);
		return false;
	}
	public P get(String key){
		Element<P> ele = this.elements.get(key);
		if(ele == null){
			return null;
		}
		ele.updateTime();
		return ele.get();
	}
	
	private void startClear(){
		if(this.clear == null){
			return;
		}
		clearThread = new Thread(new Runnable() {
			public void run() {
				while(true){
					try {
						Thread.sleep(seedtime);
						System.out.println("当前size:"+size());
						Enumeration<String> itor = elements.keys();
						while(itor.hasMoreElements()){
							String key = itor.nextElement();
							if(elements.containsKey(key) && elements.get(key).isClearable(maxtime)){
								System.out.println("清除超时元素："+key);
								Element<P> ele = elements.remove(key);
								clear.clear(ele.get());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		clearThread.setDaemon(true);
		System.out.println("开启定时清除线程");
		clearThread.start();
	}
	
	private String getRandomKey(){
		long current = System.currentTimeMillis();
		String uuid = UUID.randomUUID().toString();
		return new String(current+"").hashCode()+"_"+uuid.hashCode();
	}
	
	public abstract static class Clear<E>{
		public abstract void clear(E e);
	}
	
	private static class Element<E> {
		E e;
		long lasttime;
		public Element(E e){
			this.e = e;
		}
		public E get(){
			return e;
		}
		public void updateTime(){
			this.lasttime = System.currentTimeMillis();
		}
		public long getTime(){
			return lasttime;
		}
		public boolean isClearable(long maxtime){
			return (System.currentTimeMillis()-lasttime) >= maxtime;
		}
	}
	

	public static void main(String[] args) throws InterruptedException {
		Pool<Integer> pool = new Pool<Integer>(5, 20000,new Pool.Clear<Integer>() {

			@Override
			public void clear(Integer e) {
				System.out.println("clear : "+e);
			}
		});
		String s1 = pool.put(5);
		String s2 = pool.put(10);
		String s3 = pool.put(15);
		String s4 = pool.put(20);
		String s5 = pool.put(25);
		System.out.println(pool.get(s1));
		System.out.println(pool.get(s2));
		System.out.println(pool.get(s3));
		System.out.println(pool.get(s4));
		System.out.println(pool.get(s5));
		System.out.println(pool.remove(s1));
		while(true){
			Thread.sleep(5000);
			pool.get(s5);
		}
	}
}

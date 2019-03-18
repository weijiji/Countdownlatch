package com.wjj.util.thread.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>ThreadPool是带计数器的阻塞型FixedThreadPool线程池。</p>
 * <p>当线程池运行后，计数器({@link CountDownLatch})会自动阻塞线程池的主线程，每当一个任务完成(无论成功失败)，计数器自动减一。直到所有任务完成，计数器({@link CountDownLatch})才取消阻塞。</p>
 * 
 * <p>当所有任务完成后可通过以下函数取得状态信息:</p>
 * <ul>
 * <li>{@link #getTheadPoolErrorList()} &nbsp;返回线程池的异常信息</li>
 * <li>{@link #getReturnSuccessList()} &nbsp; 返回已执行成功的结果集</li>
 * <li>{@link #getReturnErrorList()} &nbsp;返回已执行失败的结果集</li>
 * </ul>
 *  @author Keith.Wei
 * */
public class ThreadPool {
		//错误结果集，线程间共享数据，用于收集每个线程在执行过程中发生的所有错误信息
		private List<Object> returnErrorList;
		//成功结果集，线程间共享数据，用于收集每个线程在执行过程成功后返回的信息
		private List<Object> returnSuccessList;
		//计数器，默认1，用于线程间通讯及堵塞主线程，每当完成一个任务,则减一,当所有线程完成,则不堵塞主线程
		private CountDownLatch countDownLatch = new CountDownLatch(1);
		//外部传入的任务队列
		private List<AbstractRunnable> taskList;
		//线程池错误结果集
		private Map<AbstractRunnable,Exception> theadPoolErrorList;
		
		private ExecutorService pool;
		/**
		 * <p>ThreadPool是带计数器的阻塞型FixedThreadPool线程池。</p>
		 * <p>当线程池运行后，计数器({@link CountDownLatch})会自动阻塞线程池的主线程，每当一个任务完成(无论成功失败)，计数器自动减一。直到所有任务完成，计数器({@link CountDownLatch})才取消阻塞。</p>
		 * 
		 * <p>当所有任务完成后可通过以下函数取得状态信息:</p>
		 * <ul>
		 * <li>{@link #getTheadPoolErrorList()} &nbsp;返回线程池的异常信息</li>
		 * <li>{@link #getReturnSuccessList()} &nbsp; 返回已执行成功的结果集</li>
		 * <li>{@link #getReturnErrorList()} &nbsp;返回已执行失败的结果集</li>
		 * </ul>
		 * @param nThreads 线程池最大核心线程数
		 * */
		public ThreadPool(int nThreads) throws Exception{
			pool = Executors.newFixedThreadPool(nThreads);
		}
		
		protected void initialize(){
			countDownLatch = new CountDownLatch(taskList.size());
			returnErrorList = Collections.synchronizedList(new ArrayList<Object>());	//使用安全线程List
			returnSuccessList = Collections.synchronizedList(new ArrayList<Object>());	//使用安全线程List
			theadPoolErrorList = new HashMap<AbstractRunnable,Exception>();
		}
		
		
		protected void execute() throws InterruptedException{
			for(AbstractRunnable task : taskList){
				task.setCountDownLatch(countDownLatch);			//插入计数器
				task.setReturnSuccessList(returnSuccessList);	//插入成功结果收集器
				task.setReturnErrorList(returnErrorList);		//插入错误结果收集器
			}
			
			for(AbstractRunnable task : taskList){
				try {
					pool.execute(task);	//添加任务到任务队列
				} catch (Exception e) {	//捕捉线程异常，出现问题后，优先保证线程的池稳定性，保护已有线程顺利进行
					e.printStackTrace();
					theadPoolErrorList.put(task,e);	//收集错误信息
					countDownLatch.countDown(); //计数器-1，将那些因为线程池异常而没有进行的任务从计数器中剔除
				}
			}
			
			try {
				countDownLatch.await();	//阻塞主线程，直到所有线程任务完成
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw e;
			}
			
			pool.shutdown();	//关闭线程池,施放线程
		}
		
		public void execute(List<AbstractRunnable> taskList) throws Exception{
			if(taskList==null || taskList.size() ==0)  throw new Exception("Task list can not be empty");
			this.taskList = taskList;
			this.initialize();
			this.execute();
		}
		
		/**
		 * 返回任务错误信息
		 * */
		public List<Object> getReturnErrorList() {
			return returnErrorList;
		}
		/**
		 * 返回任务成功结果
		 * */
		public List<Object> getReturnSuccessList() {
			return returnSuccessList;
		}

		/**
		 * 返回线程池错误信息
		 * */
		public Map<AbstractRunnable, Exception> getTheadPoolErrorList() {
			return theadPoolErrorList;
		}

}

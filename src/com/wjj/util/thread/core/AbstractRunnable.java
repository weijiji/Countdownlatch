package com.wjj.util.thread.core;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractRunnable implements Runnable{
	
	CountDownLatch countDownLatch;
	List<Object> returnErrorList;
	List<Object> returnSuccessList;
	
	@Override
	public void run() {
		try {
			setMessage(execute());
		}finally {
			if(countDownLatch !=null && countDownLatch.getCount()>0){
				countDownLatch.countDown();
			}
		}
	}
	/**
	 * 鐢ㄤ簬鑷畾涔塕unnable浠诲姟瀹屾垚鍚庤繑鍥炵殑鐘舵�佷俊鎭�
	 * */
	public abstract ReturnMsg execute();
	
	private synchronized void setMessage(ReturnMsg msg){
		
		if(msg.getSuccessMsg()!=null){
			returnSuccessList.add(msg.getSuccessMsg());
		}
		if(msg.getErrorMsg()!=null){
			returnErrorList.add(msg.getErrorMsg());
		}
	}

	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}

	public void setCountDownLatch(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}

	public List<Object> getReturnErrorList() {
		return returnErrorList;
	}

	public void setReturnErrorList(List<Object> returnErrorList) {
		this.returnErrorList = returnErrorList;
	}

	public List<Object> getReturnSuccessList() {
		return returnSuccessList;
	}
	public void setReturnSuccessList(List<Object> returnSuccessList) {
		this.returnSuccessList = returnSuccessList;
	}

	public static class ReturnMsg{
		private Object successMsg;
		private Object errorMsg;
		public Object getSuccessMsg() {
			return successMsg;
		}
		public void setSuccessMsg(Object successMsg) {
			this.successMsg = successMsg;
		}
		public Object getErrorMsg() {
			return errorMsg;
		}
		public void setErrorMsg(Object errorMsg) {
			this.errorMsg = errorMsg;
		}
	}
}

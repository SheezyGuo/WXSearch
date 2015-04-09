package team.xinyuan519.wxsearch.utils;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AccountInfoExectutor {
	private final String dbIP = "localhost";
	private final int dbPort = 27017;

	private final int poolSize = 100 / 2; // 25,50 or 100 is recommended
	private AccountInfo[] accountInfos;
	private int length;
	private Semaphore semaphore;

	public AccountInfoExectutor(AccountInfo[] accountInfos, Semaphore semaphore) {
		this.accountInfos = accountInfos;
		this.length = accountInfos.length;
		this.semaphore = semaphore;
	}

	private class PoolFiller implements Runnable {
		private AccountInfo accountInfo;
		private ExecutorService pool;
		private String dbIP;
		private int dbPort;

		public PoolFiller(AccountInfo accountInfo, ExecutorService pool,
				String dbIP, int dbPort) {
			this.accountInfo = accountInfo;
			this.pool = pool;
			this.dbIP = dbIP;
			this.dbPort = dbPort;
		}

		@Override
		public void run() {
			ProfileInfo profileInfo = new ProfileInfo(
					accountInfo.getIdentity(), accountInfo.getOpenid());
			profileInfo.init();
			Queue<String> linkList = profileInfo.getLinkList();
			while (!linkList.isEmpty()) {
				String link = linkList.poll();
				SimpleInfoCrawler crawler = new SimpleInfoCrawler(link,
						this.dbIP, this.dbPort, profileInfo);
				pool.execute(crawler);
			}
		}
	}

	private void MutilThreadPoolFill(ExecutorService pool) {
		PoolFiller[] fillers = new PoolFiller[this.length];
		for (int i = 0; i < this.length; i++) {
			fillers[i] = new PoolFiller(this.accountInfos[i], pool, this.dbIP,
					this.dbPort);
		}
		Thread[] threads = new Thread[this.length];
		for (int i = 0; i < this.length; i++) {
			threads[i] = new Thread(fillers[i]);
			threads[i].start();
		}
		for (int i = 0; i < this.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		pool.shutdown();
		while (!pool.isTerminated()) {
			try {
				pool.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void execute() {
		try {
			this.semaphore.acquire();
			ExecutorService pool = Executors.newFixedThreadPool(poolSize);
			MutilThreadPoolFill(pool);
			this.semaphore.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

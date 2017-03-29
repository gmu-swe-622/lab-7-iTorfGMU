package edu.gmu.swe622.lab6;

import java.util.HashMap;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class LockServerWrapper {

	private CuratorFramework curator;

	public LockServerWrapper() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		curator = CuratorFrameworkFactory.newClient("localhost:9000,localhost:9001,localhost:9002,localhost:9003,localhost:9004", retryPolicy);
		curator.start();
	}

	/**
	 * See: http://curator.apache.org/curator-recipes/shared-reentrant-lock.html
	 */
	public void getLockOnPerson(String name) {

	}

	public void unlockPerson(String name) {

	}
	

}

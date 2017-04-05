package edu.gmu.swe622.lab7;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.nodes.GroupMember;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class MembershipMain {

	private static CuratorFramework curator;
	
	private static long waitTime = 2000;
	
	private static String pathHeading = "/memberships/";

	public static void main(String[] args) {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		curator = CuratorFrameworkFactory.newClient("localhost:9000,localhost:9001,localhost:9002,localhost:9003,localhost:9004", retryPolicy);
		curator.start();

		Scanner s = new Scanner(System.in);
		System.out.println("Enter id for this member: ");
		String id = s.nextLine();
		System.out.println("Options: 1 (quit) 2 (join group) 3 (leave group) 4 (list group members)");
		scannerLoop: while (s.hasNextLine()) {

			int opt = 0;
			try {
				opt = Integer.valueOf(s.nextLine());
			} catch (Throwable t) {
				System.out.println("Invalid input.");
			}
			switch (opt) {
			case 1:
				break scannerLoop;
			case 2:
				System.out.println("What group would you like to join?");
				joinGroup(s.nextLine(), id);
				break;
			case 3:
				System.out.println("What group would you like to leave?");
				leaveGroup(s.nextLine());
				break;
			case 4:
				System.out.println("What group would you like to view?");
				listMembers(s.nextLine());
				break;
			}
			System.out.println("Options: 1 (quit) 2 (join group) 3 (leave group) 4 (list group members)");
		}
		s.close();
	}

	static HashMap<String, GroupMember> memberships = new HashMap<String, GroupMember>();
	private static void joinGroup(String groupName, String id) {
		if (!memberships.containsKey(pathHeading+groupName)) {
			GroupMember member = new GroupMember(curator, pathHeading+groupName, id);
			member.start();
			memberships.put(pathHeading+groupName, member);
		} else {
			System.out.println("Group member already exists o.O");
		}
	}

	private static void leaveGroup(String groupName) {
		if (memberships.containsKey(groupName)) {
			GroupMember member = memberships.get(pathHeading+groupName);
			member.close();
			memberships.remove(pathHeading+groupName);
		}
	}

	private static void listMembers(String groupName) {
		if (memberships.containsKey(pathHeading+groupName)) {
			Map<String, byte[]> groupMembers = memberships.get(pathHeading+groupName).getCurrentMembers();
			System.out.println("Members: ");
			for (String key : groupMembers.keySet()) {
				System.out.println("\t"+key);
			}
		} else {
			System.out.println("Group does not exist o.O");
		}
	}
	
	private static void getLockForPath(String lockPath) throws Exception {
		InterProcessMutex lock = new InterProcessMutex(curator, lockPath);
		if ( lock.acquire(waitTime, TimeUnit.MILLISECONDS) ) 
		{
		    try 
		    {
		        // do some work inside of the critical section here
		    }
		    finally
		    {
		        lock.release();
		    }
		}
	}
	
	private static void setElectedLeader(String path) {
		LeaderSelectorListener listener = new LeaderSelectorListenerAdapter()
		{
		    public void takeLeadership(CuratorFramework client) throws Exception
		    {
		        // this callback will get called when you are the leader
		        // do whatever leader work you need to and only exit
		        // this method when you want to relinquish leadership
		    }
		};

		LeaderSelector selector = new LeaderSelector(curator, path, listener);
		selector.autoRequeue();  // not required, but this is behavior that you will probably expect
		selector.start();
	}
}







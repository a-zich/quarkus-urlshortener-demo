package eu.zich.demo.quarkusurlshortener.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.scheduler.Scheduled;
import lombok.ToString;
import lombok.extern.jbosslog.JBossLog;

/**
 * This service will provide statistics of how many times a URL (identified by
 * id) was called. Every time the URL is called you should invoke
 * {@link #increaseCallCount(String)}. To reduce memory usage not every single
 * call is saved including the time. Instead buckets for every
 * {@link #MINUTES_PER_BUCKET} minutes are generated and increased. The
 * assumption is that users will not care about the detailed per second
 * statistics. <br>
 * Methods in this class are synchronized.
 */
@ApplicationScoped
@JBossLog
public class StatisticService {

	static final int MINUTES_PER_BUCKET = 1; // make sure this is a divisor of 24*60

	private static final int BUCKETS_PER_DAY = 24 * 60 / MINUTES_PER_BUCKET;

	@ToString
	class CallStat {
		long firstBucketTimestamp = currentBucketTimestamp(); // this marks the first counter's timestamp
		int[] counters = new int[BUCKETS_PER_DAY]; // counter[0] is the latest time, counter[0] is MINUTES_PER_BUCKET
													// earlier and so on.
	}

	private Map<String, CallStat> stats = new HashMap<>();

	/**
	 * This defines a cleanup task. It will delete all statistics where the
	 * {@link CallStat.firstBucketTimestamp} is more than 24h ago. This means the
	 * URL wasn't called in the last 24h and statistic can be removed.
	 */
	@Scheduled(every = "1m")
	synchronized void cleanupData() {
		log.info("cleaning statistic data");
		long timestamp = currentBucketTimestamp(); // this is the value it should have
		long oldestTimestamp = timestamp - BUCKETS_PER_DAY;

		// we will iterate over all keys in the statistics hasmap
		// to look for too old data
		for (Iterator<Entry<String, CallStat>> it = stats.entrySet().iterator(); it.hasNext();) {
			Entry<String, CallStat> entry = it.next();
			CallStat stat = entry.getValue();
			if (stat.firstBucketTimestamp < oldestTimestamp) {
				// the statistics first value is older than 24h, so just delete this entry
				it.remove();
				log.infov("removed statistic for {0}", entry.getKey());
			}
		}
	}

	public synchronized int getStats(String id) {

		if (stats.containsKey(id)) {
			CallStat stat = stats.get(id);
			cleanStats(stat);

			int counter = 0;
			for (int i : stat.counters) {
				counter += i;
			}
			return counter;
		} else {
			return 0; // we don't bother if the forward doesn't exist
		}

	}

	public synchronized void increaseCallCount(String id) {
		CallStat stat = stats.get(id);
		if (stat == null) {
			// create statistic object
			stat = new CallStat();
			stats.put(id, stat);
		}
		cleanStats(stat);
		stat.counters[0]++; // counter[0] contains the latest bucket

	}

	public synchronized void deleteStats(String id) {
		log.infov("delete statistic for {0}", id);
		stats.remove(id);
	}

	/**
	 * This method will move away old buckets so that data older than 24h is not
	 * considered anymore
	 * 
	 * @param v The CallStat to work on
	 */
	private static void cleanStats(CallStat v) {
		long timestamp = currentBucketTimestamp(); // this is the value it should have

		long moveBucketsBy = timestamp - v.firstBucketTimestamp;

		log.debugv("before cleaning bucket: {0}", v);

		if (moveBucketsBy >= BUCKETS_PER_DAY) {
			// this means we can just clean out all data
			// because this URL wasn't call in the last day

			v.firstBucketTimestamp = timestamp;
			Arrays.fill(v.counters, 0);
		} else if (moveBucketsBy > 0) {
			// when we are it is safe to cast to an int
			System.arraycopy(v.counters, 0, v.counters, (int) moveBucketsBy, BUCKETS_PER_DAY - (int) moveBucketsBy);
			Arrays.fill(v.counters, 0, (int) moveBucketsBy, 0);
			v.firstBucketTimestamp = timestamp;

		} // no need to do anything if moveBucketsBy == 0
		log.debugv("after cleaning bucket: {0}", v);
	}

	/**
	 * @return bucket number since the epoch
	 */
	private static long currentBucketTimestamp() {
		return System.currentTimeMillis() / 1000 / 60 / MINUTES_PER_BUCKET;
	}
}

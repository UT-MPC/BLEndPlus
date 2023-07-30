import java.util.ArrayList;
import java.util.Iterator;

public class BLEndSchedule extends BLESchedule{

    private double T; // epoch length in milliseconds
    private double L; // listen interval length in milliseconds
    private double M; // missing rate of beacons
	private double WP_Scan; // warm up interval of Scan
	private double WP_Adv; // warm up interval of Adv
	private double WP; // total warm up interval
    private double advertisingInterval; // interval between beacons, in milliseconds
	private boolean addExtraEpoch; // does extra epoch added for fixing WP issue

    private double threeB; // the beaconLength to use for schedule generation; we use beaconLength for computing collisions
    
    private BLEndSchedule(){}

    public BLEndSchedule(int nodeID, BLEDiscSimulatorOptions options, double simulationTime, double[] startOffsets){
		super(nodeID, options, simulationTime);

		this.threeB = 3*beaconLength;
		this.T = options.getT();
		this.M = options.getM();
		this.WP_Scan = options.getWP_Scan();
		this.WP_Adv = options.getWP_Adv();
		this.WP = this.WP_Scan+this.WP_Adv;
		this.addExtraEpoch = options.getAddExtra();
		
		// the listening time is the time specified in the properties
		this.L = options.getL();
		// however, if the option to "correct" for the 10ms random added advDelay is through listening,
		// then we need to add the max additional maximum possible delay to L
		if(options.correctAdvDelay() == BLEDiscSimulatorOptions.ADV_DELAY_CORRECT_LISTEN){
			L = L + options.getMaxAdditionalAdvDelay();
		}

		// FIX: The advertising interval needs to be b smaller  to ensure that a COMPLETE beacon is heard within L
		// however, in the code, we use this as the gap (radio off time) between two successive advertisement events, so it's not
		// really the advertisement *interval* exactly (it's b smaller than that)

		//HC: CHANGING!
		this.advertisingInterval = options.getL() - beaconLength;
		//CJ: CHANGING!
		//this.advertisingInterval = (options.getL() - threeB);
		//this.advertisingInterval = (options.getL() - 2*beaconLength);

			// however, if the option to "correct" for the 10ms random added advDelay is through advertising,
		// then we need to also subtract the additional maximum possible delay
		if(options.correctAdvDelay() == BLEDiscSimulatorOptions.ADV_DELAY_CORRECT_ADVERTISE){
			advertisingInterval = advertisingInterval - options.getMaxAdditionalAdvDelay();
		}
		
		// if this advertising interval is now LESS than a beacon, we're in trouble. Cry and quit.
		//CJ: CHANGING!
		if(advertisingInterval < threeB){
			//if(advertisingInterval < beaconLength){
			System.err.println("Whoops! Invalid setting! The advertisement interval has to be longer than a beacon. This is probably the fault of the correction for BLE's added random advertising delay");
			System.exit(0);
		}
		setStartOffset(startOffsets, options, T+260); //TODO: Changes with +260
    }

    // for BLEnd, we just need to make sure that the nodes' epoch start times are not aligned
    private void setStartOffset(double[] startOffsets, BLEDiscSimulatorOptions options, double range){
		if(options.controlStartOffset()){
			// then I need to know what start offsets have already been selected and select something different
			// CJ: CHANGING!!
			double requiredDifference = options.getB();
			/*if(options.modelChannels()){
			requiredDifference = 1;
			}*/
			// this will actually not be possible if there are more nodes than possible start times. How many
			// possible start times are there? floor(T/requiredDifference). So let's check that first.
			/*if(startOffsets.length + 1 > Math.floor(T/requiredDifference)){
			System.err.println("Unsatisfiable configuration!");
			System.exit(1);
			}*/
			// the above doesn't work because we allow nodes to choose randomly...
			
			boolean found = false;
			int tries = 0;
			// since we can't really compute whether we will succeed or not, but we can try really hard. If we try 100000 times without finding
			// a satisfying schedule, we quit.
			while(!found && tries < 100000){
			startOffset = (Math.random() * range);
			found = true;
			for(int i = 0; i<startOffsets.length; i++){
				if((Math.abs(startOffsets[i]-startOffset) < requiredDifference)||
				// for BLEnd, we also need to avoid the weird case that node A's before-listen beacon is aligned with node B's
				// after-listen beacon

				// CJ: changing to threeB
				// TODO: this fix only works for 1ms beacons: GP says just add checking the end to be within requiredDifference
				//((startOffset + L) == (startOffsets[i] - options.getB()))||
				((startOffset + L) == (startOffsets[i] - threeB))||
				// have to check both directions
				//((startOffsets[i] + L) == (startOffset - options.getB()))){
				((startOffsets[i] + L) == (startOffset - threeB))){
				// oops; I selected a startOffset too close to someone else's
				found = false;
				startOffset = -1;
				// try again. Sure, this is terrible, but at least its still pseudo-random...
				}
			}
			tries++;
			}
			// if we failed to find a satisfactory startOffset, then it will still have the value -1
			if(startOffset == -1){
			System.err.println("Unsatisfiable configuration! Too many nodes to find a satisfying start offset.");
			System.exit(1);
			}
		}
		else{
			// just select random number between 0 and  for the schedule's offset
			startOffset = (Math.random() * range);
		}
    }

    // a schedule has to be three epochs long to represent
    // listening on the three channels in turn
    void createSchedule(){
		if(schedule == null){
			schedule = new ArrayList<BLEScheduleEvent>();
			double epochStartTime = startOffset;
			//int scanChannel = BLEScheduleEvent.ADVERTISEMENT_CHANNEL_ONE;
			// randomly choose one of the three scan channels to start on
			int scanChannel = (int) (Math.random() * 3);
			while(epochStartTime < simulationTime){
				// random epochs for compensating the warmup interval
				double extraEpoch = 0;
				if(addExtraEpoch){
					// random epochs for compensating the warmup interval
					extraEpoch = Math.random() * T;
				}
				createOneEpoch(scanChannel, epochStartTime, extraEpoch);
				epochStartTime += (T+extraEpoch);
				scanChannel = BLEScheduleEvent.getNextScanChannel(scanChannel);
			}
		}
    }

    // this creates the schedule for one "epoch" which involves listening on only one channel
    private void createOneEpoch(int channel, double startTime, double extraEpoch){
		// listen for L milliseconds, starting at the provided startTime

		// non-function wp interval
		if(WP_Scan>0) {
			BLEListenStartEvent startListenInWP = new BLEListenStartEvent(nodeID, startTime, channel);
			BLEListenEndEvent endListenInWP = new BLEListenEndEvent(nodeID, startTime + WP_Scan);
			startListenInWP.setIsInWPScan(true);
			endListenInWP.setIsInWPScan(true);
			schedule.add(startListenInWP);
			schedule.add(endListenInWP);
		}

		// It lietens nothing when it is under warmup interval
		double listenAvailableTime = startTime + WP_Scan;
		BLEListenStartEvent startListen =
			new BLEListenStartEvent(nodeID, listenAvailableTime, channel);
		schedule.add(startListen);
		double endListenTime = startTime + L;
		if(addExtraEpoch){
			endListenTime += WP_Scan; //extend scan interval for an extra WP_Scan length
		}
		BLEListenEndEvent endListen = new BLEListenEndEvent(nodeID, endListenTime);
		schedule.add(endListen);

		// non-function wp interval
		if(WP_Adv>0) {
			BLEAdvertiseStartEvent startAdvInWP = new BLEAdvertiseStartEvent(nodeID, endListenTime);
			BLEAdvertiseEndEvent endAdvInWP = new BLEAdvertiseEndEvent(nodeID, endListenTime + WP_Adv);
			startAdvInWP.setIsInWPAdv(true);
			endAdvInWP.setIsInWPAdv(true);
			schedule.add(startAdvInWP);
			schedule.add(endAdvInWP);
		}

		// create the advertisement events and add them to the schedule
		double time = endListenTime + WP_Adv; //advertising WP interval is before adv schedule
		double epoch = T + extraEpoch;
		// the missing rate of advertising beacons
		double missingRate = M;
		// This is for the half epoch of uni- and bi-directional blend.
		while(time < (startTime + epoch - threeB)){
			// we need to compute the start time for the beacon and the end time for the beacon
			// we need to use the start time of the beacon to compute the start time of the NEXT beacon
			// the start time for THIS beacon is just time
			// the end time for THIS beacon should be time + beaconLength

			// We randomly choose a number to indicate if the advertising is missing.
			boolean isMissing = Math.random()<missingRate;

			BLEAdvertiseStartEvent startAdvertising = new BLEAdvertiseStartEvent(nodeID, time);
			if(isMissing){
				startAdvertising.setIsPkLoss(true);
			}
			// right? the time between two start beacons should be the same as a listen (which is the advertising interval + beacon length)
			schedule.add(startAdvertising);

			time = time+beaconLength;
			BLEAdvertiseEndEvent endAdvertising = new BLEAdvertiseEndEvent(nodeID, time);
			if(isMissing){
				endAdvertising.setIsPkLoss(true);
			}
			schedule.add(endAdvertising);
			// the start time for the NEXT beacon should be time + advertisingInterval + the BLE random delay

			//HC: CHANGING!
			time = time + advertisingInterval - beaconLength;
			//CJ: CHANGING!
			//time = time + advertisingInterval + (threeB-beaconLength);

			// select a random number between 0 and the max possible added random delay
			double randomDelay = Math.random() * options.getMaxAdditionalAdvDelay();
			time = time + randomDelay;
		}
		// Remove the last beacon right before the next listen to reduce the time spend on advertising warm-up interval
    }

    public void onDiscovery(BLEAdvertiseEndEvent base, BLEDiscSimulator simulation){
	// we only activate beacons if we're using the BLEnd half epoch model (which is the usual case)
	if(options.modelBLEndHalfEpoch()){
	    // if bidirectional discovery is enabled, then we also need the discoverer to activate
	    // a beacon in the schedule to "hit" the listening interval of the node it discovered
	    // we don't want to do this for the extra beacons, though
	    if(options.isBidirectionalDiscoveryEnabled() && !base.isExtra()){
		int discoveredNodeID = base.getNodeID();
		double discoveryTime = base.getTime();
		// first, we need to figure out when the discovered node will be listening.
		// TODO: This is cheating. I should really be using sequence numbers in the beacons and then
		// doing the math
		BLESchedule discoveredNodesSchedule = simulation.getScheduleForNodeID(discoveredNodeID);
		BLEListenStartEvent blse = discoveredNodesSchedule.getNextListenStartEvent(discoveryTime);
		if(blse != null){
		    double nextListenTime = blse.getTime();
		    // then we need to find the next beacon that comes after that listen start time, both the
		    // event for starting that beacon and the one for ending it. We need to activate both.
		    BLEScheduleEvent toActivateStart = getNextAdvertiseStartEvent(nextListenTime);
		    if(toActivateStart != null){
			toActivateStart.setActive(true);
			//System.out.println(base.getTime() + ": node " + nodeID + ": adding beacon for " + discoveredNodeID + " at " + toActivateStart.getTime());
			BLEScheduleEvent toActivateEnd = getNextAdvertiseEndEvent(toActivateStart.getTime());
			if(toActivateEnd != null){
			    toActivateEnd.setActive(true);
			}
		    }
		}
	    }
	}
    }	
}

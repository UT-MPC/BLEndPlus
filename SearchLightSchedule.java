import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SearchLightSchedule extends BLESchedule{

    private int t; // number of slots in a Searchlight period
    private double slotLength; // length of a slot in milliseconds
    //private int[] probeSlotSchedule; // the permutation of probe slots to use
	private double M; // missing rate of beacons
	private double WP_Scan; // warm up interval of Scan
	private double WP_Adv; // warm up interval of Adv
	private double WP; // total warm up interval
	private boolean addExtraEpoch; // does extra epoch added for fixing WP issue

    private double threeB; // the beaconLength to use for schedule generation; we use beaconLength for computing collisions

    
    private SearchLightSchedule(){}

    public SearchLightSchedule(int nodeID, BLEDiscSimulatorOptions options, double simulationTime, double[] startOffsets){
	super(nodeID, options, simulationTime);
	this.t = (int)options.getT();
	this.slotLength = options.getSlotLength();
	this.M = options.getM();
	this.WP_Scan = options.getWP_Scan();
	this.WP_Adv = options.getWP_Adv();
	this.WP = this.WP_Scan+this.WP_Adv;
	this.addExtraEpoch = options.getAddExtra();
	
	setStartOffset(startOffsets, options, (t * slotLength));
    }

    // ugh. For searchlight, this is complicated. We need to make sure that, for every pair of nodes, their slot start times are separated
    // by at least three ms, regardless of whether we're modeling the separate channels. This is because every node starts an active slot
    // by sending for three ms
    private void setStartOffset(double[] startOffsets, BLEDiscSimulatorOptions options, double range){
	if(options.controlStartOffset()){
	    // then I need to know what start offsets have already been selected and select something different
	    // TODO: for now this only works for the 1ms beacons!
	    double requiredDifference = options.getB() + 1; // the +1 is to allow for one ms of the beacon to overlap the listen
	    boolean found = false;
	    int tries = 0;
	    // since we can't really compute whether we will succeed or not, but we can try really hard. If we try 100000 times without finding
	    // a satisfying schedule, we quit.
	    while(!found && tries < 100000){
		startOffset = (int) (Math.random() * range);
		double slotOffset = startOffset % slotLength;
		found = true;
		for(int i = 0; i<startOffsets.length; i++){
		    double otherSlotOffset = startOffsets[i] % slotLength;
		    if((Math.abs(otherSlotOffset-slotOffset) < requiredDifference)||
		       // ugh... then there's the corner case where one's is something like 54 and the other is 1...
		       (Math.abs(otherSlotOffset-slotOffset) > (slotLength - requiredDifference))){
			// oops; I selected a startOffset too synchronized with someone else's
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
	    startOffset = (int) (Math.random() * range);
	}
    }

    private int[] createRandomProbeSlotSchedule(int numProbeSlots){
	int[] probeSlotSchedule = new int[numProbeSlots];
	for(int i = 0; i<probeSlotSchedule.length; i++){
	    probeSlotSchedule[i] = i+1;
	}
	Random random = new Random();
	for (int i = probeSlotSchedule.length; i > 1; i--) {
	    int randomInt = random.nextInt(i);
	    int temp = probeSlotSchedule[i-1];
	    probeSlotSchedule[i-1] = probeSlotSchedule[randomInt];
	    probeSlotSchedule[randomInt] = temp;
	}
	return probeSlotSchedule;
    }

    void createSchedule(){
	if(schedule == null){
	    schedule = new ArrayList<BLEScheduleEvent>();

	    // initialize the (random) permutation that is the slot schedule
	    int numProbeSlots = (int)Math.floor((double)t/2); // the number of probe slots. From the paper floor(t/2)
	    int[] probeSlotSchedule = createRandomProbeSlotSchedule(numProbeSlots);

	    // get the first scan channel. This has to be incremented before each scan; there's
	    // a method in BLEScheduleEvent that will increment correctly (to cycle through the available
	    // advertisement channels
	    int scanChannel = BLEScheduleEvent.ADVERTISEMENT_CHANNEL_ONE;
	    
	    // to be used as a counter into the probeSlotSchedule. It gets incremented once we schedule
	    // the "anchor slot" and the "probe slot" for this period;
	    // it gets reset to 0 once it falls off the end of the list
	    int probeSlotCounter = 0;

	    double periodStartTime = startOffset;
	    while(periodStartTime < simulationTime){

		// first, the anchor slot is always the first slot. We need to advertise at the beginning, listen
		// then advertise at the end
		createActiveSlot(periodStartTime, scanChannel);

		// increment the scan channel so that we cycle through all three
		scanChannel = BLEScheduleEvent.getNextScanChannel(scanChannel);
		
		// the probe slot is slightly more difficult because which slot it is moves around. But that's
		// why we have that fancy probeSlotSchedule array!
		double actualSlotLength = slotLength;
		if(addExtraEpoch){
			actualSlotLength += 2*WP_Adv+WP_Scan;
		}
		double probeSlotStartTime = periodStartTime + (probeSlotSchedule[probeSlotCounter] * actualSlotLength);
		createActiveSlot(probeSlotStartTime, scanChannel);

		periodStartTime = periodStartTime + ( t * actualSlotLength ); //TODO: need to consider extra epoch

		// increment the probeslot counter so that we probe in a different slot next time
		probeSlotCounter++;
		if(probeSlotCounter == probeSlotSchedule.length){
		    probeSlotCounter = 0;
		}
		// increment the scan channel so that we cycle through all three
		scanChannel = BLEScheduleEvent.getNextScanChannel(scanChannel);
	    }
	}
    }

    private void createActiveSlot(double slotStartTime, int channel){
	double missingRate = M;
	boolean isMissing = Math.random() < missingRate;

	// non-function wp interval
	double wpEndTime;
	if(addExtraEpoch){
		wpEndTime = WP_Adv;
	}else {
		wpEndTime = Math.min(beaconLength,WP_Adv);
	}
	if(WP_Adv>0) {
		BLEAdvertiseStartEvent startAdvInWP = new BLEAdvertiseStartEvent(nodeID, slotStartTime);
		BLEAdvertiseEndEvent endAdvInWP = new BLEAdvertiseEndEvent(nodeID, wpEndTime);
		startAdvInWP.setIsInWPAdv(true);
		endAdvInWP.setIsInWPAdv(true);
		schedule.add(startAdvInWP);
		schedule.add(endAdvInWP);
	}
	double time = wpEndTime;

	// if there is advertising wp in orig Searchlight without slot extension, the beacon would not be scheduled
	if(addExtraEpoch || WP_Adv==0){
		double beaconStartTime = slotStartTime;
		double beaconEndTime = slotStartTime + beaconLength;
		beaconStartTime += WP_Adv;
		beaconEndTime += WP_Adv;

		BLEAdvertiseStartEvent startAdvertising = new BLEAdvertiseStartEvent(nodeID, beaconStartTime);
		BLEAdvertiseEndEvent endAdvertising =
				new BLEAdvertiseEndEvent(nodeID, beaconEndTime);
		if (isMissing) {
			startAdvertising.setIsPkLoss(true);
			endAdvertising.setIsPkLoss(true);
		}
		schedule.add(startAdvertising);
		schedule.add(endAdvertising);
		time += beaconLength;
	}

	time = slotStartTime + beaconLength;
	if(addExtraEpoch){ time += WP_Adv;}

	// non-function wp interval
	if(WP_Scan>0) {
		BLEListenStartEvent startListenInWP = new BLEListenStartEvent(nodeID, time, channel);
		BLEListenEndEvent endListenInWP = new BLEListenEndEvent(nodeID, time + WP_Scan);
		startListenInWP.setIsInWPScan(true);
		endListenInWP.setIsInWPScan(true);
		schedule.add(startListenInWP);
		schedule.add(endListenInWP);
	}

	double listenAvailableTime = time + WP_Scan;
	BLEListenStartEvent startListen =
			new BLEListenStartEvent(nodeID, listenAvailableTime, channel);
	schedule.add(startListen);
	// we need to end listening before the end of the active slot to allow time for the second beacon
	// end listining at periodStartTime + slotLength - beaconLength
	double endListenTime = time + slotLength - 2*beaconLength;
	if(addExtraEpoch){
		endListenTime += WP_Scan;
	}
	BLEListenEndEvent endListen = new BLEListenEndEvent(nodeID, endListenTime);
	schedule.add(endListen);

	// finally, advertise one more time
	isMissing = Math.random()<missingRate;

	// adv wp before adv
	if(WP_Adv>0) {
		BLEAdvertiseStartEvent startAdvInWP = new BLEAdvertiseStartEvent(nodeID, endListenTime);
		BLEAdvertiseEndEvent endAdvInWP = new BLEAdvertiseEndEvent(nodeID, endListenTime + WP_Adv);
		startAdvInWP.setIsInWPAdv(true);
		endAdvInWP.setIsInWPAdv(true);
		schedule.add(startAdvInWP);
		schedule.add(endAdvInWP);
	}

	// if there is advertising wp in orig Searchlight, the beacon would not be scheduled
	if(addExtraEpoch || WP_Adv==0) {
		double beaconStartAgainTime = endListenTime;
		double beaconEndAgainTime = endListenTime + beaconLength;
		beaconStartAgainTime += WP_Adv;
		beaconEndAgainTime += WP_Adv;

		BLEAdvertiseStartEvent startAdvertisingAgain = new BLEAdvertiseStartEvent(nodeID, beaconStartAgainTime);
		BLEAdvertiseEndEvent endAdvertisingAgain =
				new BLEAdvertiseEndEvent(nodeID, beaconEndAgainTime);
		if (isMissing) {
			startAdvertisingAgain.setIsPkLoss(true);
			endAdvertisingAgain.setIsPkLoss(true);
		}
		schedule.add(startAdvertisingAgain);
		schedule.add(endAdvertisingAgain);
	}
    }

    public  void onDiscovery(BLEAdvertiseEndEvent base, BLEDiscSimulator simulation){
	// the searchLight implementation doesn't really need to do anything in this callback
	
    }    
}

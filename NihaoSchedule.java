import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class NihaoSchedule extends BLESchedule{

    private int n; // width and height of the balanced Nihao slot matrix
	private int m; // width of the balanced Nihao slot matrix
    private double slotLength; // slot length (in milliseconds)
    private double threeB; // the beaconLength to use for schedule generation; we use beaconLength for computing collisions
	private double M; // missing rate of beacons
	private double WP_Scan; // warm up interval of Scan
	private double WP_Adv; // warm up interval of Adv
	private double WP; // total warm up interval
    private boolean addExtraEpoch; // does extra epoch added for fixing WP issue
    
    private NihaoSchedule(){}

    // so what does Nihao do? every n^2 slots, it beacons, then listens for n slots.
    // Then every n slots, it beacons again
    public NihaoSchedule(int nodeID, BLEDiscSimulatorOptions options, double simulationTime, double[] startOffsets){
	super(nodeID, options, simulationTime);

	this.n = options.getN();
	this.m = 1; //HC: We set m to 1 and the slot length is exactly the advertising interval of advertising mode (In Android, there are Low_power, Balaanced, Low_Latency)
	this.slotLength = options.getSlotLength();
	this.M = options.getM();
	this.WP_Scan = options.getWP_Scan();
	this.WP_Adv = options.getWP_Adv();
	this.WP = this.WP_Scan+this.WP_Adv;
	this.addExtraEpoch = options.getAddExtra();

	// TODO: if we're being cool, we'd adjust Nihao to account for the random slop in beaconing
	// for now, though, we'll treat Nihao like we treat Searchlight, assuming that we turn
	// advertising on and off each time
	
	// select random number between 0 and T for the schedule's offset
	setStartOffset(startOffsets, options, (m * n * slotLength));

    }


    
    // for nihao, we need to make sure that each node's (n * slot start time) is not aligned with another. So we need to make
    // sure that they're not multiples of n * the slot length (plus or minus the beacon)
    private void setStartOffset(double[] startOffsets, BLEDiscSimulatorOptions options, double range){
	if(options.controlStartOffset()){
	    // then I need to know what start offsets have already been selected and select something different
	    int requiredDifference = 3;
	    if(options.modelChannels()){
		requiredDifference = 1;
	    }
	    boolean found = false;
	    int tries = 0;
	    // since we can't really compute whether we will succeed or not, but we can try really hard. If we try 100000 times without finding
	    // a satisfying schedule, we quit.
	    while(!found && tries < 100000){
		startOffset = (int) (Math.random() * range);
		double slotOffset = startOffset % (slotLength * n);
		found = true;
		for(int i = 0; i<startOffsets.length; i++){
		    double otherSlotOffset = startOffsets[i] % (slotLength * n);
		    if(Math.abs(otherSlotOffset-slotOffset) < requiredDifference){
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

    // a schedule has to be three epochs long to represent
    // listening on the three channels in turn
    void createSchedule(){
	if(schedule == null){
	    schedule = new ArrayList<BLEScheduleEvent>();
	    double matrixStartTime = startOffset;
	    int scanChannel = BLEScheduleEvent.ADVERTISEMENT_CHANNEL_ONE;

		double extraTime = 0;
		if(addExtraEpoch){
			//int randomSlotSize= Math.random()>0.5 ? 1:0;
			int randomSlotSize= (int) Math.floor(Math.random()*(m*n)); //Changes
			// random epochs for compensating the warmup interval
			extraTime = randomSlotSize*(slotLength) + WP; //WP is for extending the listening interval & extending the adv
		}

	    while(matrixStartTime < simulationTime){
			createOneMatrix(scanChannel, matrixStartTime, extraTime);
			matrixStartTime += (m * n * slotLength);
			if(addExtraEpoch) {
				matrixStartTime += extraTime; //changes
			}
			scanChannel = BLEScheduleEvent.getNextScanChannel(scanChannel);
	    }
	}
    }

    // this creates the schedule for one "matrix" which involves listening on only one channel
    private void createOneMatrix(int channel, double startTime, double extraTime){
		double missingRate = M;

		// there is non-function adv wp interval before beacon
		double wpEndTime;
		if(addExtraEpoch){
			wpEndTime = WP_Adv;
		}else {
			wpEndTime = startTime + Math.min(beaconLength,WP_Adv);
		}
		if(WP_Adv>0) {
			BLEAdvertiseStartEvent startAdvInWP = new BLEAdvertiseStartEvent(nodeID, startTime);
			BLEAdvertiseEndEvent endAdvInWP = new BLEAdvertiseEndEvent(nodeID, wpEndTime);
			startAdvInWP.setIsInWPAdv(true);
			endAdvInWP.setIsInWPAdv(true);
			schedule.add(startAdvInWP);
			schedule.add(endAdvInWP);
		}

		// then, we beacon
		double time = wpEndTime;
		boolean isMissing = Math.random()<missingRate;
		// if there is advertising wp in orig Searchlight without slot extension, the beacon would not be scheduled
		if(addExtraEpoch || WP_Adv==0) {
			BLEAdvertiseStartEvent startFirstAdvertising = new BLEAdvertiseStartEvent(nodeID, time);
			if (isMissing) {
				startFirstAdvertising.setIsPkLoss(true);
			}
			schedule.add(startFirstAdvertising);
			time += beaconLength;
			BLEAdvertiseEndEvent endFirstAdvertising = new BLEAdvertiseEndEvent(nodeID, time);
			if (isMissing) {
				endFirstAdvertising.setIsPkLoss(true);
			}
			schedule.add(endFirstAdvertising);
		}

		// non-function scan wp interval
		if(WP_Scan>0) {
			BLEListenStartEvent startListenInWP = new BLEListenStartEvent(nodeID, time, channel);
			BLEListenEndEvent endListenInWP = new BLEListenEndEvent(nodeID, time + WP_Scan);
			startListenInWP.setIsInWPScan(true);
			endListenInWP.setIsInWPScan(true);
			schedule.add(startListenInWP);
			schedule.add(endListenInWP);
		}

		// now listen for the remaining n * slotLength milliseconds, starting at the provided startTime
		double listenAvailableTime = time + WP_Scan;
		BLEListenStartEvent startListen =
			new BLEListenStartEvent(nodeID, listenAvailableTime, channel); // include WP interval
		schedule.add(startListen);
		double listenEndTime = time + slotLength - beaconLength;
		if(addExtraEpoch){
			listenEndTime += WP_Scan;
		}
		BLEListenEndEvent endListen = new BLEListenEndEvent(nodeID, listenEndTime);
		schedule.add(endListen);
		time = listenEndTime;

		// non-function Adv wp interval
		double wpEndAgainTime;
		if(addExtraEpoch){
			wpEndAgainTime = time + WP_Adv;
		}else {
			wpEndAgainTime = time + Math.min(beaconLength,WP_Adv);
		}

		if(WP_Adv>0) {
			BLEAdvertiseStartEvent startAdvInWP = new BLEAdvertiseStartEvent(nodeID, time);
			BLEAdvertiseEndEvent endAdvInWP = new BLEAdvertiseEndEvent(nodeID, wpEndAgainTime);
			startAdvInWP.setIsInWPAdv(true);
			endAdvInWP.setIsInWPAdv(true);
			schedule.add(startAdvInWP);
			schedule.add(endAdvInWP);
		}
		time = wpEndAgainTime;

		//beacon again
		double matrixEndTime = startTime + (m * n * slotLength);
		if(addExtraEpoch){
			matrixEndTime += extraTime;
		}
		// create the advertisement events and add them to the schedule
		while(time < matrixEndTime){
			isMissing = Math.random()<missingRate;

			// if there is advertising wp in orig Searchlight without slot extension, the beacon would not be scheduled
			if(addExtraEpoch || WP_Adv==0) {
				BLEAdvertiseStartEvent startAdvertising = new BLEAdvertiseStartEvent(nodeID, time);
				if (isMissing) {
					startAdvertising.setIsPkLoss(true);
				}
				schedule.add(startAdvertising);
				BLEAdvertiseEndEvent endAdvertising = new BLEAdvertiseEndEvent(nodeID, time + beaconLength);
				if (isMissing) {
					endAdvertising.setIsPkLoss(true);
				}
				schedule.add(endAdvertising);
			}

			// the start time for the NEXT beacon should be time + advertisingInterval + the BLE random delay
			time = time + (m * slotLength);
			// In original Nihao, there is no impact of eandom slack
			if (addExtraEpoch) {
				// select a random number between 0 and the max possible added random delay
				double randomDelay = Math.random() * options.getMaxAdditionalAdvDelay();
				time = time + randomDelay;
			}
		}
    }

    public void onDiscovery(BLEAdvertiseEndEvent base, BLEDiscSimulator simulation){
	// Niaho doesn't do anything in response to a disdovery event
    }

}

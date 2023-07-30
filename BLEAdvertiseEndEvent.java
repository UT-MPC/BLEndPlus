

public class BLEAdvertiseEndEvent extends BLEScheduleEvent{

    protected BLEAdvertiseEndEvent(){}
    
    public BLEAdvertiseEndEvent(int nodeID, double time){
	super(nodeID, time, false);
    }

    public String toString(){
	return (nodeID + " : " + time + " : END ADVERTISE + (" + isActivated + ")");
    }

    public void process(BLEDiscSimulator simulator){
	// because some BLEAdvertiseEndEvents are inactivated, we have to check to make sure we only
	// process the active ones

	if(isActivated){
	    simulator.process(this);
	}
    }

    public boolean isBeacon(){
	return true;
    }

}

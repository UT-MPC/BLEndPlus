
public class BLEAdvertiseStartEvent extends BLEScheduleEvent{
    
    protected BLEAdvertiseStartEvent(){}
    
    public BLEAdvertiseStartEvent(int nodeID, double time){
	super(nodeID, time, true);
    }	    

    public String toString(){
	return (nodeID + " : " + time + " : START ADVERTISE + (" + isActivated + ")");
    }

    @Override
    public void process(BLEDiscSimulator simulator){
	// because some BLEAdvertiseStartEvents are inactivated, we have to check to make sure we only
	// process the active ones
	if(isActivated){
	    simulator.process(this);
	}
    }

    public boolean isBeacon(){
	return true;
    }

}

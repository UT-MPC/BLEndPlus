public class BLEAdvertiseOneChannelEndEvent extends BLEAdvertiseEndEvent{

    private int channel; // just in case; probably not necessary
    
    private BLEAdvertiseOneChannelEndEvent(){}
    
    public BLEAdvertiseOneChannelEndEvent(int nodeID, double time, int channel){
	super(nodeID, time);
	this.channel = channel;
    }

    public String toString(){
	return (nodeID + " : " + time + " : END ADVERTISE ONE CHANNEL + (" + isActivated + ")");
    }

    public boolean isBeacon(){
	return true;
    }
    
}

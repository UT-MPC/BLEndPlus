
// this class is used when we're modeling BLE's three channels...
// we capture the advertsising start event and convert it into three of these, each on a different channel,
// always in order

public class BLEAdvertiseOneChannelStartEvent extends BLEAdvertiseStartEvent {

    private int channel; // the advertising channel on which to advertise

    private BLEAdvertiseOneChannelStartEvent(){}

    public BLEAdvertiseOneChannelStartEvent(int nodeID, double time, int channel){
	super(nodeID, time);
	this.channel = channel;
    }

    public int getChannel(){
	return channel;
    }

    @Override
    public void process(BLEDiscSimulator simulator){
	simulator.process(this);
    }

    public boolean isBeacon(){
	return true;
    }
}

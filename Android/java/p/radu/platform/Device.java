package p.radu.platform;

public class Device {
    private double distanceFront;
    private double distanceBack;
    private String state;
    private String blocked;

    public Device() {

    }

    public Device(double distanceFront, double distanceBack, String state, String blocked) {
        this.distanceFront = distanceFront;
        this.distanceBack = distanceBack;
        this.state = state;
        this.blocked = blocked;
    }

    public double getDistanceFront() {
        return distanceFront;
    }

    public double getDistanceBack() {
        return distanceBack;
    }

    public String getState() {
        return state;
    }

    public String getBlocked() {
        return blocked;
    }

}

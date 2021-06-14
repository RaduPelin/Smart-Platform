
package p.radu.platform;

import org.parceler.Parcel;

@Parcel
public class Step {
    public String direction;
    public double distance;
    public String distanceUnits;
    public int numberOfLapses;
    public double delay;
    public String delayUnits;

    public Step() {

    }

    public Step(String direction, double distance, String distanceUnits, int numberOfLapses, double delay, String delayUnits) {
        this.direction = direction;
        this.distance = distance;
        this.distanceUnits = distanceUnits;
        this.numberOfLapses = numberOfLapses;
        this.delay = delay;
        this.delayUnits = delayUnits;
    }

    public String getDirection() {
        return direction;
    }

    public double getDistance() {
        return distance;
    }

    public String getDistanceUnits() {
        return distanceUnits;
    }

    public int getNumberOfLapses() {
        return numberOfLapses;
    }

    public double getDelay() {
        return delay;
    }


    public String getDelayUnits() {
        return delayUnits;
    }


    @Override
    public String toString() {
        return "Direction: " + direction +  " Distance: " + distance + " " + distanceUnits + " Number of Lapses: " + numberOfLapses + " Delay: " + delay + " " + delayUnits;
    }

}

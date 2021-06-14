package p.radu.platform;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class Program {
    public List<Step> steps;
    public boolean comeBack;
    public boolean on;
    public String name;

    public Program() {

    }

    public Program(List<Step> steps, boolean comeBack, boolean on, String name) {
        this.steps = steps;
        this.comeBack = comeBack;
        this.on = on;
        this.name = name;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public boolean isComeBack() {
        return comeBack;
    }

    public void setComeBack(boolean comeBack) {
        this.comeBack = comeBack;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

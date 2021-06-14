package p.radu.platform;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String fullName;
    private String email;

    private String phone;
    private String deviceID;
    private boolean standBy;
    private boolean valid;


    public User() {

    }

    public User(String fullName, String email, String phone, String deviceID, boolean standBy, boolean valid) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.deviceID = deviceID;
        this.standBy = standBy;
        this.valid = valid;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public boolean isStandBy() {
        return standBy;
    }

    public boolean isValid() {
        return valid;
    }

}


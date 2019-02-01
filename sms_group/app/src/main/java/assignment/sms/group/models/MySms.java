package assignment.sms.group.models;

public class MySms {

    private String address;
    private String body;
    private long timestamp;

    public MySms(String address, String body, long timestamp) {
        this.address = address;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

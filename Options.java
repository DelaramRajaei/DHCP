import java.io.DataInputStream;
import java.io.IOException;

public class Options {
    private int dhcp_message_type = 0;
    private int client_identifier = 0;
    private int server_identifier = 0;
    private int requested_ip = 0;
    private int lease_time = 0;
    private int renew_time = 0;
    private int rebind_time = 0;

    public Options(DataInputStream option) throws IOException {
        int i = 0;
        boolean flag = false;
        while (!flag) {
            option.reset();
            option.skipBytes(232 + i);
            int code = option.readInt();
            switch (code) {
                case 255:// End of option
                    flag = true;
                    break;
                case 53:// DHCP message type
                    i += 4;
                    option.reset();
                    option.skipBytes(232 + i);
                    dhcp_message_type = option.readInt();
                    break;
                case 54:// Server identifier
                    i += 4;
                    option.reset();
                    option.skipBytes(232 + i);
                    server_identifier = option.readInt();
                    break;
                case 61:// Client identifier
                    i += 4;
                    option.reset();
                    option.skipBytes(232 + i);
                    client_identifier = option.readInt();
                    break;
                case 50:// Requested IP address
                    i += 4;
                    option.reset();
                    option.skipBytes(232 + i);
                    requested_ip = option.readInt();
                    break;
                case 51:// Lease time
                    i += 4;
                    option.reset();
                    option.skipBytes(232 + i);
                    lease_time = option.readInt();
                    break;
                case 58:// Renew time
                    i += 4;
                    option.reset();
                    option.skipBytes(232 + i);
                    renew_time = option.readInt();
                    break;
                case 59:// Rebind time
                    i += 4;
                    option.reset();
                    option.skipBytes(232 + i);
                    rebind_time = option.readInt();
                    break;
            }
            i += 4;
        }
    }
    public void setRequested_ip(int ip) {
        requested_ip = ip;
    }
    public int getDhcp_message_type() {
        return dhcp_message_type;
    }

    public int getClient_identifier() {
        return client_identifier;
    }

    public int getServer_identifier() {
        return server_identifier;
    }

    public int getRequested_ip() {
        return requested_ip;
    }

    public int getLease_time() {
        return lease_time;
    }

    public int getRenew_time() {
        return renew_time;
    }

    public int getRebind_time() {
        return rebind_time;
    }
}

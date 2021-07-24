import java.io.*;
import java.net.*;
import java.util.Random;

public class DHCPClient {
    private final static int BUF_SIZE = 8192;
    private final static double backoff_cutoff = 120;
    private final static double initial_interval = 10;
    private final static int timeout = 20;
    private static double time = initial_interval;
    private static int count = 0;

    public static void main(String args[]) throws Exception {

        // Create client socket
        MyThread timer;
        InetAddress inetAddress = InetAddress.getLocalHost();
        DatagramSocket socket = new DatagramSocket(68);
        socket.setBroadcast(true);
        int transactionID = new Random().nextInt(10);
        boolean flag = false;
        Options option;
        while (!flag) {
            sendDiscoverMessage(socket, inetAddress, transactionID);
            timer = new MyThread(socket, inetAddress, transactionID, true);
            option = receiveOfferMessage(socket, inetAddress);
            timer.stop();
            if (option.getRequested_ip() == 0 || option.getDhcp_message_type() != 2) continue;
            sendRequestMessage(socket, inetAddress, transactionID, option);
            timer = new MyThread(socket, inetAddress, transactionID, false);
            flag = receiveAckMessage(socket, inetAddress);
            timer.stop();

        }

    }

    private static boolean receiveAckMessage(DatagramSocket socket, InetAddress address) throws IOException {
        byte[] inBuf = new byte[BUF_SIZE];
        ByteArrayInputStream inBufArray = new ByteArrayInputStream(inBuf);
        DataInputStream input = new DataInputStream(inBufArray);
        DatagramPacket response = new DatagramPacket(inBuf, inBuf.length);
        socket.receive(response);
        boolean isOurs = false;
        if (response.getLength() != 0) {
            input.reset();
            input.skipBytes(28); //jump to chaddr (client hardware address)
            //and check if the received datagram is for our MAC address
            isOurs = true;
            NetworkInterface network = NetworkInterface.getByInetAddress(address);
            byte[] macAddress = network.getHardwareAddress();
            for (int i = 0; i < 6; i++)
                if (input.readUnsignedByte() != macAddress[i]) {
                    isOurs = false;
                    break;
                }

            if (isOurs) {
                input.reset();
                input.skipBytes(16);
                int ipAddress = input.readInt();
                if (ipAddress == 0) System.out.println("Server send nack!");
                else System.out.println("Server send ack! Ip address is " + convertIntegerToIp(ipAddress));
            }
        }
        return isOurs;
    }

    private static void sendRequestMessage(DatagramSocket socket, InetAddress address, int transactionID, Options option) throws IOException {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream(BUF_SIZE);
        DataOutputStream output = new DataOutputStream(outBuf);
        output.write(1); // OP: request
        output.write(1); // hw_type: ethernet
        output.write(6); // hw_addr_len: 6 for ethernet
        output.write(0); // hops
        output.writeInt(transactionID); // transaction ID
        output.writeShort(0); // secs: time elapsed since start of DHCP request
        // but must be the same as at discover!!!!
        output.writeShort(0x0000); // flags: MSB = uni/broadcast; rest must be zero
        output.writeInt(0); // client ip address
        output.writeInt(0); // your (client) ip address
        output.writeInt(0); // server ip address
        output.writeInt(0); // relay agent ip address
        NetworkInterface network = NetworkInterface.getByInetAddress(address);
        byte[] macAddress = network.getHardwareAddress();
        for (int i = 0; i < macAddress.length; i++) {
            output.write(macAddress[i]); // client hardware address
        }
        output.writeShort(0);
        output.writeInt(0);
        output.writeInt(0);
        output.writeLong(0); // server host name
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0); // boot file name
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeInt(0x63825363); // Magic cookie

        // option value
        output.writeInt(53);
        output.writeInt(3); // DHCP request
        output.writeInt(61);
        output.writeInt(61); // Client identifier
        output.writeInt(50);
        output.writeInt(option.getRequested_ip());// Requested ip address
        output.writeInt(54);
        output.writeInt(option.getServer_identifier());// server identifier
        output.writeInt(255); // End of Options

        output.flush();
        DatagramPacket datagram = new DatagramPacket(outBuf.toByteArray(), outBuf.size(), address, 67);
        socket.send(datagram);
    }

    private static void sendDiscoverMessage(DatagramSocket socket, InetAddress address, int transactionID) throws IOException {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream(BUF_SIZE);
        DataOutputStream output = new DataOutputStream(outBuf);
        output.write(1); // OP: request
        output.write(1); // hw_type: ethernet
        output.write(6); // hw_addr_len: 6 for ethernet
        output.write(0); // hops
        output.writeInt(transactionID); // transaction ID
        output.writeShort(0); // secs: time elapsed since start of DHCP request
        output.writeShort(0); // flags: MSB set rest must be zero
        output.writeInt(0); // client ip address
        output.writeInt(0); // your (client) ip address
        output.writeInt(0); // server ip address
        output.writeInt(0); // relay agent ip address
        NetworkInterface network = NetworkInterface.getByInetAddress(address);
        byte[] macAddress = network.getHardwareAddress();
        for (int i = 0; i < macAddress.length; i++) {
            output.write(macAddress[i]); // client hardware address
        }
        output.writeShort(0); // s.a.a.
        output.writeInt(0); // s.a.a.
        output.writeInt(0); // s.a.a.
        output.writeLong(0); // server host name
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0); // boot file name
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeLong(0);
        output.writeInt(0x63825363); // Magic cookie

        // option value
        output.writeInt(53);
        output.writeInt(1); // DHCP discover
        output.writeInt(61);
        output.writeInt(61); // Client identifier
        output.writeInt(255); // End of Options

        output.flush();
        DatagramPacket datagram = new DatagramPacket(outBuf.toByteArray(), outBuf.size(), address, 67);
        socket.send(datagram);
    }

    private static Options receiveOfferMessage(DatagramSocket socket, InetAddress address) throws IOException {
        byte[] inBuf = new byte[BUF_SIZE];
        NetworkInterface network = NetworkInterface.getByInetAddress(address);
        byte[] macAddress = network.getHardwareAddress();
        ByteArrayInputStream inBufArray = new ByteArrayInputStream(inBuf);
        DataInputStream input = new DataInputStream(inBufArray);
        DatagramPacket response = new DatagramPacket(inBuf, inBuf.length);
        socket.receive(response);
        int ip = 0;
        if (response.getLength() != 0) {
            input.skipBytes(28); //jump to chaddr (client hardware address)
            //and check if the received datagram is for our MAC address
            boolean isOurs = true;
            for (int i = 0; i < 6; i++)
                if (input.readUnsignedByte() != macAddress[i]) {
                    isOurs = false;
                    break;
                }

            if (isOurs) {
                System.out.println("Offer received!");
                input.reset();
                input.skipBytes(16);
                ip = input.readInt();
            }
        }
        Options option = new Options(input);
        option.setRequested_ip(ip);
        return option;
    }

    public static String convertIntegerToIp(int ip) {
        return ((ip >> 24) & 0xFF) +
                "." + ((ip >> 16) & 0xFF) +
                "." + ((ip >> 8) & 0xFF) +
                "." + (ip & 0xFF);
    }

    static class MyThread implements Runnable {

        // to stop the thread
        private boolean exit;
        DatagramSocket s;
        InetAddress address;
        boolean b;
        int id;
        Thread t;

        MyThread(DatagramSocket socket, InetAddress inetAddress, int transactionID, boolean b) {
            s = socket;
            address = inetAddress;
            id = transactionID;
            this.b = b;
            t = new Thread(this);
            exit = false;
            t.start(); // Starting the thread
        }

        // execution of thread starts from run() method
        public void run() {
            if (b) {
                while (!exit || time == 0) {
                    time--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Caught:" + e);
                    }
                }
                if (time == 0) {
                    time = (initial_interval * count) * 2 * (Math.random());
                    count++;
                    if (time > backoff_cutoff) time = backoff_cutoff;
                    System.out.println("Resend discover message!");
                    try {
                        sendDiscoverMessage(s, address, id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                time = timeout;
                while (!exit || time == 0) {
                    time--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("Caught:" + e);
                    }
                }
                if (time == 0) {
                    System.out.println("Resend discover message!");
                    try {
                        sendDiscoverMessage(s, address, id);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // for stopping the thread
        public void stop() {
            exit = true;
            System.out.println("Timer Stopped.");
        }
    }
}


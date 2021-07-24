import java.io.*;
import java.net.*;
import java.util.HashMap;


public class DHCPServer {
    private final static int BUF_SIZE = 8192;


    public static void main(String args[]) throws IOException, InterruptedException {
        // Create server Socket
        DatagramSocket socket = new DatagramSocket(67);
        byte[] inBuf = new byte[BUF_SIZE];
        DatagramPacket packet = new DatagramPacket(inBuf, inBuf.length);
        while (true) {
            socket.receive(packet);
            Options options = new Options(new DataInputStream(new ByteArrayInputStream(packet.getData())));
            // connect it to client socket
            try {
                ClientHandler clientSock = new ClientHandler(socket, packet, options);
                // create a new thread object
                // This thread will handle the client separately
                new Thread(clientSock).start();

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private static void sendACK(DatagramSocket socket, int transactionID, InetAddress address, Options option) throws IOException {
        //readFromFile(address.getHostAddress());
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
        output.writeInt(option.getRequested_ip()); // your (client) ip address
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

        // Options (1 byte option type, 1 byte option length in bytes, n bytes
        // option value)
        output.write(53);
        output.write(4); // DHCP ack
        output.writeInt(61);
        output.writeInt(option.getClient_identifier()); // Client identifier
        output.writeInt(58);
        output.writeInt(10);// Renew time
        output.writeInt(59);
        output.writeInt(10);// Rebind time
        output.writeInt(51);
        output.writeInt(10);// Lease Time
        output.writeInt(54);
        output.writeInt(option.getServer_identifier());// Server identifier
        output.writeInt(255); // End of Options

        output.flush();
        DatagramPacket datagram = new DatagramPacket(outBuf.toByteArray(), outBuf.size(), address, 68);
        socket.send(datagram);
        System.out.println("Assigned Ip address is: " + DHCPClient.convertIntegerToIp(option.getRequested_ip()) + "\n");
    }

    private static void sendOffer(DatagramSocket socket, int transactionID, InetAddress address, Options option, int offered_ip_address) throws Exception {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream(BUF_SIZE);
        DataOutputStream output = new DataOutputStream(outBuf);
        output.write(2); // OP: reply
        output.write(1); // hw_type: ethernet
        output.write(6); // hw_addr_len: 6 for ethernet
        output.write(0); // hops
        output.writeInt(transactionID); // transaction ID
        output.writeShort(0); // secs: time elapsed since start of DHCP request
        // but must be the same as at discover!!!!
        output.writeShort(0x0000); // flags: MSB = uni/broadcast; rest must be zero
        output.writeInt(0); // client ip address
        output.writeInt(offered_ip_address); // your (client) ip address
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

        // Options (1 byte option type, 1 byte option length in bytes, n bytes
        // option value)
        output.writeInt(53);
        output.writeInt(2); // DHCP offer
        output.writeInt(61);
        output.writeInt(option.getClient_identifier()); // Client identifier
        output.writeInt(58);
        output.writeInt(10);// Renew time
        output.writeInt(59);
        output.writeInt(10);// Rebind time
        output.writeInt(51);
        output.writeInt(10);// Lease Time
        output.writeInt(54);
        output.writeInt(54);// Server identifier
        output.writeInt(255); // End of Options

        output.flush();
        DatagramPacket datagram = new DatagramPacket(outBuf.toByteArray(), outBuf.size(), address, 68);
        socket.setBroadcast(true);
        socket.send(datagram);
        writeToFile(address.getHostAddress(), offered_ip_address);
    }

    private static void writeToFile(String hostAddress, int offered_ip_address) throws IOException {
        File file = new File("Storage.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(hostAddress + "_" + offered_ip_address);
        bw.close();
    }

    private static int readFromFile(String hostAddress) throws IOException {
        String fileName = "Storage.txt";
        File file = new File(fileName);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split("_");
            if (split[0].trim().equals(hostAddress))
                return Integer.parseInt(split[1]);
        }
        return 0;
    }

    private static int offeredIPAddress(Storage storage, String macAddr, Options options) throws Exception {
        int ip;
        // Reserved IP
        if (storage.reservation_list.containsKey(macAddr))
            ip = storage.reservation_list.get(macAddr);
            // Black list
        else if (contain(storage.black_list, macAddr)) ip = 0;
            // Renew
        else if (storage.allocated.containsKey(macAddr)) {
            if (options.getLease_time() > 0)
                ip = storage.allocated.get(macAddr);
            else  ip = (int) (Math.random() * (storage.limited_area[1] - storage.limited_area[0])) + storage.limited_area[0];
        }
        // New IP
        else ip = (int) (Math.random() * (storage.limited_area[1] - storage.limited_area[0])) + storage.limited_area[0];
        return ip;
    }

    private static boolean contain(String[] black_list, String macAddr) {
        for (int i = 0; i < black_list.length; i++) {
            if (black_list[i].equals(macAddr))
                return true;
        }
        return false;
    }

    private static Storage readJsonFile() throws Exception {
        Storage storage = new Storage();
        String fileName = "C:\\Users\\Delaram\\Desktop\\Computer Networks\\P#\\P3\\configs.json";
        File jsonFile = new File(fileName);
        BufferedReader br = new BufferedReader(new FileReader(jsonFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitted = line.trim().split("\"");
            // Pool Mode
            if (splitted.length < 2) continue;
            if (splitted[1].equals("pool_mode")) storage.pool_mode = splitted[3];
                // Range and Subnet
            else if (splitted[1].equals(storage.pool_mode)) {
                for (int i = 0; i < 2; i++) {
                    storage.limited_area[i] = convertIpToInteger(br.readLine().replace("\"", "").replace(",", "").trim().split(":")[1]);
                }
            }
            // Lease time
            else if (splitted[1].equals("lease_time"))
                storage.lease_time = Integer.parseInt(splitted[2].replace(":", "").replace(",", "").trim());
                // Reservation List
            else if (splitted[1].equals("reservation_list")) {
                for (String reserved = br.readLine(); !reserved.contains("}"); reserved = br.readLine().trim()) {
                    String[] split = reserved.split("\"");
                    storage.reservation_list.put(split[1], convertIpToInteger(split[3]));
                }
            }
            // Black List
            else if (splitted[1].equals("black_list")) {
                storage.black_list = br.readLine().replace("\"", "").trim().split(",");
            }
        }
        return storage;
    }

    private static int convertIpToInteger(String ipAddr) {
        ipAddr = ipAddr.replace(" ", "").trim();
        String[] parts = ipAddr.split("\\.");
        int ip = 0;
        for (int i = 0; i < 4; i++) {
//            ipAddr += Integer.parseInt(parts[i]);
            ip = ip << 8;
            ip |= Integer.parseInt(parts[i]);
        }
        return ip;
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        private final DatagramSocket socket;
        private DatagramPacket packet;
        private Options options;

        // Constructor
        public ClientHandler(DatagramSocket socket, DatagramPacket packet, Options options) {
            this.socket = socket;
            this.packet = packet;
            this.options = options;
        }

        public void run() {
            try {
                ByteArrayInputStream inBufArray = new ByteArrayInputStream(packet.getData());
                DataInputStream input = new DataInputStream(inBufArray);
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();
                if (clientPort == 67) return;
                Storage storage = readJsonFile();
                input.skipBytes(4);
                int transactionID = input.readInt();
                switch (options.getDhcp_message_type()) {
                    case 1: // Discover
                        System.out.println("New client connected _ Discover Message received!");
                        int offered_ip_address = offeredIPAddress(storage, NetworkInterface.getByInetAddress(clientAddress).getHardwareAddress().toString(), options);
                        if (offered_ip_address == 0) return;
                        sendOffer(socket, transactionID, clientAddress, options, offered_ip_address);
                        break;
                    case 3: // Request
                        boolean isOurs = true;
                        if (packet.getLength() != 0) {
                            input.reset();
                            input.skipBytes(28); //jump to chaddr (client hardware address)
                            //and check if the received datagram is for our MAC address
                            byte[] macAddress = NetworkInterface.getByInetAddress(clientAddress).getHardwareAddress();
                            for (int i = 0; i < 6; i++)
                                if (input.readUnsignedByte() != macAddress[i]) {
                                    isOurs = false;
                                    break;
                                }
                            if (isOurs) {
                                input.skipBytes(-34);
                                System.out.println("Request received!");
                                if (options.getRequested_ip() != readFromFile(clientAddress.getHostAddress())) {
                                    System.out.println("Not the same offer!");
                                    isOurs = false;
                                }
                            }
                        }
                        if (isOurs) sendACK(socket, transactionID, clientAddress, options);
                        break;
                }
//                DatagramPacket response = new DatagramPacket(inBuf, inBuf.length, clientAddress, clientPort);
//                socket.send(response);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class Storage {
        public String pool_mode;
        public int[] limited_area = new int[2];
        public int lease_time;
        public HashMap<String, Integer> reservation_list = new HashMap<>();
        public String[] black_list;
        public HashMap<String, Integer> allocated = new HashMap<>();
    }
}



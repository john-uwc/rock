package uwc.carry.chan;

public final class Address {
    public static Address toLocal(Address address, int port) {
        return to(address, port, "127.0.0.1");
    }

    public static Address to(Address address, int port, String addr){
        address.port = port;
        address.addr = addr;
        return address;
    }

    public int port;
    public String addr;
}

package uwc.carry.chan;

import uwc.carry.Gate;
import uwc.util.Logger;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public abstract class Net extends Gate.Channel {

    private DatagramSocket mDatagramSocket = null;
    private byte[] mSlidWindow = null;

    public Net(int port, int sws) {
        try {
            if (0 >= sws || port <= 0)
                throw new IllegalArgumentException();
            mSlidWindow = new byte[sws];
            mDatagramSocket = new DatagramSocket(port);
            mDatagramSocket.setBroadcast(true);
            mDatagramSocket.setReuseAddress(true);
        } catch (Exception e) {
            Logger.Holder.obtain().e(TAG, e.toString());
        }
    }

    protected abstract Object resolve(String flat);

    @Override
    protected Object receive() {
        try {
            Arrays.fill(mSlidWindow, (byte) 0);
            mDatagramSocket.receive(new DatagramPacket(mSlidWindow, mSlidWindow.length));
            return resolve(new String(mSlidWindow).trim());
        } catch (Exception e) {
            Logger.Holder.obtain().e(TAG, e.toString());
        }
        return null;
    }

    @Override
    protected void send(Object obj) {
        try {
            Address dest = new Address();
            String flat = encapsulate(obj, dest);
            mDatagramSocket.send(
                    new DatagramPacket(flat.getBytes(), flat.getBytes().length, InetAddress.getByName(dest.addr), dest.port));
        } catch (Exception e) {
            Logger.Holder.obtain().e(TAG, e.toString());
        }
    }


    protected abstract String encapsulate(Object obj, Address dest);
}

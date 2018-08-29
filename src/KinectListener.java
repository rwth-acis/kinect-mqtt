import edu.ufl.digitalworlds.j4k.J4KSDK;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.nustaq.serialization.FSTConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.Deflater;

public class KinectListener extends J4KSDK {
    MqttPublisher publisher;
    FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    public KinectListener() {
//        super();
        publisher = new MqttPublisher();
        new Thread(new Runnable() {
            @Override
            public void run() {
                publisher.connect("tcp://192.168.0.24:1883", UUID.randomUUID().toString());
            }
        }).start();

        conf.registerClass(DepthModel.class);
//        setColorResolution(640, 480);

        start(J4KSDK.INFRARED | J4KSDK.DEPTH | J4KSDK.XYZ);
        setColorResolution(640, 480);
    }

    @Override
    public void onDepthFrameEvent(short[] depth_frame, byte[] player_index, float[] XYZ, float[] UV) {
//        System.out.println("DEPTH");
//        System.out.println("DEPTH");
        DepthModel model = new DepthModel(depth_frame, player_index, XYZ, UV, getDepthWidth(), getDepthHeight());


        byte[] obj = compress(conf.asByteArray(model));
        publisher.publish("depth", new MqttMessage(obj));
    }

    public static byte[] compress(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer); // returns the generated code... index
            outputStream.write(buffer, 0, count);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] output = outputStream.toByteArray();
        return output;
    }

    @Override
    public void onInfraredFrameEvent(short[] data) {
        int sz = getInfraredWidth() * getInfraredHeight();
        /*
        byte bgra[] = new byte[sz * 4];
        int idx = 0;
        int iv = 0;
        short sv = 0;
        byte bv = 0;
        for (int i = 0; i < sz; i++) {
            sv = data[i];
            iv = sv >= 0 ? sv : 0x10000 + sv;
            bv = (byte) ((iv & 0xfff8) >> 6);
            bgra[idx] = bv;
            idx++;
            bgra[idx] = bv;
            idx++;
            bgra[idx] = bv;
            idx++;
            bgra[idx] = 0;
            idx++;
        }
//        System.out.println("h"+getInfraredHeight());
//        System.out.println(getInfraredWidth());
*/

        publisher.publish("infrared", new MqttMessage(compress(conf.asByteArray(data))));
    }


    //TODO:
    @Override
    public void onSkeletonFrameEvent(boolean[] booleans, float[] floats, float[] floats1, byte[] bytes) {
//        publisher.publish("infrared", new MqttMessage(bytes));
    }

    @Override
    public void onColorFrameEvent(byte[] bytes) {
        publisher.publish("color", new MqttMessage(compress(bytes)));
    }
}

package bgu.spl.net.srv;

import bgu.spl.net.srv.bidi.BGSProtocol;
import bgu.spl.net.srv.bidi.BidiMessageEncoderDecoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {

    private static int len = 0;
    private static byte [] bytes = new byte[1<<10];

    private static void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private static byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    public static void main(String[] args) throws IOException {

        byte[] opcode = shortToBytes((short)1);
        String user = "user";
        byte [] username = user.getBytes();
//        String content = "hello from the other side";
//        byte[] cont = content.getBytes();
//        String pass = "pass";
//        byte [] password = pass.getBytes();
//        String birth = "11-11-1111";
//        byte [] birthday = birth.getBytes();
        String users="user1|user2|user3|";
        byte[] usersArr = users.getBytes();

        for (byte b : opcode) {
            pushByte(b);
        }
        for (byte b : usersArr){
            pushByte(b);
        }
//        for (byte b: cont){
//            pushByte(b);
//        }
        pushByte((byte) '\0');
//        for (byte b : username) {
//            pushByte(b);
//        }
//        pushByte((byte) '\0');
//        for (byte b : password) {
//            pushByte(b);
//        }
//        pushByte((byte) '\0');
//        for (byte b : birthday) {
//            pushByte(b);
//        }
//        pushByte((byte) '\1');
        pushByte((byte) ';');
        System.out.println("bytes list: " + Arrays.toString(bytes));
        Object[] os = {(short)2,"username", (byte)0, "password",  (byte)0,  (byte)1};
        List<Object> toEncode = Arrays.asList(os);
        System.out.println("Object list to encode: " + toEncode);
        BidiMessageEncoderDecoder encdec =  new BidiMessageEncoderDecoder();
        byte [] bs = encdec.encode(toEncode);
        System.out.println("encoded bytes array: " + Arrays.toString(bs));
        List<Object> msg = null;
        int i=0;
        while (msg == null){
            msg =  encdec.decodeNextByte(bs[i++]);
        }
        System.out.println("msg: " + msg);
        //BGSProtocol b= new BGSProtocol();
       // b.process(msg);

    }

}

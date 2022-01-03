package bgu.spl.net.impl.BGSServer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientMain {
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

    public static void main(String[] args) throws Exception {
        // need host and port, we want to connect to the ServerSocket at port 8080
        Socket socket = new Socket("localhost", 7777);
        System.out.println("Connected!");

        OutputStream outputStream = socket.getOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        System.out.println("Sending string to the ServerSocket");
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);

//        byte[] opcode = shortToBytes((short)1);
//        String user = "Miryam";
//        byte [] username = user.getBytes();
//        String pass = "pass";
//        byte [] password = pass.getBytes();
//        String birth = "11-11-1111";
//        byte [] birthday = birth.getBytes();
//
//        pushByte((byte) '\0');
//        for (byte b : opcode) {
//            pushByte(b);
//        }
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
//        pushByte((byte) '\0');
//        pushByte((byte) ';');
//
//
//        // write the message we want to send
//        dataOutputStream.write(bytes);
//        dataOutputStream.flush(); // send the message
//
//        System.out.println("message from server: " );
//        for ( int i = 0; i < dataInputStream.available(); i++ )
//        {
//            byte ch = dataInputStream.readByte();
//            System.out.print((char) ch + "(" + ch + ")");
//        }




        byte[] opcode = shortToBytes((short)3);

    //    pushByte((byte) '\0');
        for (byte b : opcode) {
            pushByte(b);
        }
        pushByte((byte) ';');

        dataOutputStream.write(bytes);
        dataOutputStream.flush(); // send the message

//        System.out.println("message from server: " );
//        for ( int i = 0; i < dataInputStream.available(); i++ )
//        {
//            byte ch = dataInputStream.readByte();
//            System.out.print((char) ch + "(" + ch + ")");
//        }


        dataOutputStream.close(); // close the output stream when we're done.
        dataInputStream.close();

//        System.out.println("Closing socket and terminating program.");
//        socket.close();

//        // get the output stream from the socket.
//        OutputStream outputStream = socket.getOutputStream();
//        // create a data output stream from the output stream so we can send data through it
//        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
//        System.out.println("Sending string to the ServerSocket");

//        dataOutputStream.close(); // close the output stream when we're done.
//
//        System.out.println("Closing socket and terminating program.");
//        socket.close();


//        // write the message we want to send
//        dataOutputStream.writeUTF("Hello from the other side!");
//        dataOutputStream.flush(); // send the message
//        dataOutputStream.close(); // close the output stream when we're done.
//
//        System.out.println("Closing socket and terminating program.");
        socket.close();
    }




}

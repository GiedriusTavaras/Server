/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author tavar
 */
public class Server {

    private static boolean run = true;
    private static ServerSocket ss;
    public static final int PORT = 8080;
    public static final String ENCODING = "UTF-8";
    public static final String WEB_DIR = "C:\\Users\\tavar\\OneDrive\\Documents\\webas";

    public static void stop() {
        run = false;
        try {
            ss.close();
        } catch (Exception ex) {
            // ignored
        } finally {
            System.exit(0);
        }
    }

    public static void start() {
        while (true) {
            try {
                Socket s = ss.accept();
                handle(s);
            } catch (IOException ex) {
                if (!run) {
                    System.err.println("Connection Failed");
                }
            }
        }
    }

    public static void handle(Socket s) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), ENCODING));) {
            String req = br.readLine();
            System.out.println(req);
            if (req == null) {
                sendError(s, 404, "Not found");
                return;
            }
            req = req.split(" ")[1];

            if ("/end".equals(req)) {
                send(s, "<html><body>Bye</body></html>");
                stop();
            } else {
                if ("/".equals(req)) {
                    req = "index.html";
                } else {
                    req = req.substring(1);
                }
//                send(s, "<html><body>Hello</body></html>");
                sendFile(req, s);
            }
        } catch (IOException ex) {
            System.err.println("Error handling connection");
        } finally {
            try {
                s.close();
            } catch (Exception ex) {
                // ignored
            }
        }
    }

    public static void send(Socket s, String content) throws IOException {
        send(s, content, "text/html;charset=" + ENCODING + "");
    }

    public static void send(Socket s, String content, String type) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), ENCODING));) {
            bw.write("HTTP/1.1 200 OK\r\n");
            bw.write("Content-Type: " + type + "\r\n");
            bw.write("Content-Lenght: " + content.getBytes(ENCODING).length + "\r\n");
            bw.write("\r\n");
            bw.write(content);
            bw.flush();
        }
    }

    public static void sendFile(String name, Socket s) throws IOException {

        Path p = Paths.get(WEB_DIR, name);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(p.toFile()), ENCODING));) {
            String content = "";
            String line;

            while ((line = br.readLine()) != null) {
                content += line + "\r\n";
            }
            try {
                send(s, content);
            } catch (IOException ex) {
                System.err.println("");
            }

        } catch (FileNotFoundException ex) {
            sendError(s, 404, "File Not Found");
        } catch (IOException ex) {
            sendError(s, 404, "File Not Found");
        }
    }

    public static void sendError(Socket s, int code, String message) throws IOException {

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), ENCODING));) {
            bw.write("HTTP/1.1 " + code + " " + message + "\r\n");
            bw.write("\r\n");
            bw.flush();
        }
    }

    public static void main(String[] args) {
        try {
            ss = new ServerSocket(PORT);
            start();
        } catch (IOException ex) {
            System.err.println("Faild to bind to port:" + PORT);
        }
    }
}

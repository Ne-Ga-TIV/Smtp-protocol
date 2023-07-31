import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SmtpClient {

    private SSLSocket socket;
    private String[] commands = {"AUTH LOGIN", "MAIL FROM: ", "RCPT TO: ", "DATA", "QUIT"};
    private String address, subject, user, pass;
    private String boundary = "boundary";
    StringBuilder message = new StringBuilder();
    private BufferedReader in;
    private PrintWriter out;
    private boolean connect = false;

    SmtpClient(String host, String port) throws IOException {

        socket = (SSLSocket)((SSLSocketFactory)SSLSocketFactory.getDefault()).createSocket(host, Integer.valueOf(port));
        connect = true;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        
    }

    public String sendHello() throws IOException {
       
        out.println("EHLO smtp.gmail.com\r\n");
        String resp = in.readLine();

        while(true){
            resp =in.readLine();
            if(!in.ready()) break;
        }
        return resp;
    }
    public String sendEmail() throws IOException{
        
        out.println(commands[3]);
        in.readLine();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        message.append("From: <" + address + ">\n");
        message.append("Subject: " + subject + "\r\n");
        message.append("MIME-Version: 1.0\r\n");
        message.append("Content-Type: multipart/mixed; boundary=" + boundary + "\r\n");
        message.append("--" + boundary + "\r\n");
        message.append("Content-Type: text/plain; charset=utf-8\r\n");
        message.append( "--" + boundary + "\r\n");
        

        System.out.println("You can write an email, if you want to stop, press CTRL + F: ");
        
        while (true) {
            int c = input.read();
            if (c != 6)  // CTRL + F
            message.append ((char)c);
            else
                break;
        }
        message.append( "\r\n--" + boundary + "\r\n");
        return "2";

    }
    public String auht() throws IOException{

        Base64.Encoder encoder = Base64.getEncoder();
        String username = encoder.encodeToString(user.getBytes(StandardCharsets.UTF_8));
        String password = encoder.encodeToString(pass.getBytes(StandardCharsets.UTF_8));
        out.println(username);
        in.readLine();
        out.println(password);
        return in.readLine();
    }
    public void close() throws IOException{
        connect = false;
    }
    public boolean getConnect() { return connect; }
    
    public void readyForFile(String fileName){

        message.append("Content-Type: application/" + fileName.substring(fileName.indexOf(".")+1) + "; " + "name=\"" + fileName + "\"\r\n");
        message.append("Content-Transfer-Encoding: base64\r\n");
        message.append("Content-Disposition: attachment; filename=\"" + fileName + "\"\r\n");

    }
    public void addFiles(String[] files) throws FileNotFoundException, IOException{

        for(int i = 0; i < files.length; i++){
            readyForFile(files[i]);
            FileInputStream fis = new FileInputStream(files[i]);
            byte[] content = new byte[fis.available()];
            fis.read(content);
            String encodedContent = Base64.getEncoder().encodeToString(content);
            message.append(encodedContent + "\r\n");
            message.append( "--" + boundary + "\r\n");
            fis.close();
        }

        end();
    }

    public String sendCommand(int n, String arg) throws IOException{
        
        String resp = null;

        switch (n) {
            case 1:
                out.println(commands[n-1]);
                resp  = in.readLine();
                user = arg;
                break;
            case 2:
                pass = arg;
                resp = auht();
                break;
            case 7:
                out.println(commands[n - 3]);
                resp = in.readLine();
                this.close();
                break;
            case 5:
                this.subject = arg;
                resp = sendEmail();
                break;
        
            default:
                out.println(commands[n - 2] + "<" + arg + ">");
                resp = in.readLine();
                if(n == 3) address = arg;
                break;
        }
        return errors(resp);
    }
    public void end() {
        out.println(message);
        out.println("\r\n.\r\n");
        message.delete(0, message.length());
        address = subject = "";
    }
    public String errors(String output)throws IOException{

        if(output.startsWith("2") || output.startsWith("3"))
            return "Ok";

        if(output.startsWith("4")){
            throw new IOException("Server error: " + output.substring(4));
        }
        
        if(output.startsWith("5")){
            output = output.substring(4);
            output = "Command error: " + output + ".\nTry again!";
        }
        return output;

    }

}

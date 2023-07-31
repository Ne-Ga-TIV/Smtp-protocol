import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        
        String[] clientMsgs = {"User: ",
                            "Password: ",
                           "From: ", 
                           "To: ",
                           "Subject: ",
                           "Add files?(y/n)",
                           "1. New email\n2. Exit\n"};
        

        try {
            String resp, returnCode;
            int step = 1;
            Scanner in = new Scanner(System.in);
           
            SmtpClient smtp = new SmtpClient(args[0], args[1]);
           
            returnCode = smtp.sendHello();

            if(!returnCode.startsWith("2")){
                System.out.println(returnCode);
            }
           
            System.out.println("Smtp client successfully started\n\nHELLO");
           
            while(true){
                
                if(!smtp.getConnect()){
                    System.out.println("Connect closed, good bye");
                    break;
                }
               
                System.out.print(clientMsgs[step - 1]);
                resp = in.nextLine();

                if(step == clientMsgs.length){
                    if(resp.startsWith("1")){ 
                        step = 3;
                        continue;
                    }
                }

                
                if(step == clientMsgs.length - 1){
                    if(resp.startsWith("y")){ 
                        System.out.println("Enter file names separated by commas: ");;
                        resp = in.nextLine();
                        smtp.addFiles(files(resp));
                        step++;
                        continue;
                    }else
                    {
                        smtp.end();
                        continue;
                    } 
                }

                returnCode = smtp.sendCommand(step, resp);

                if(!returnCode.equals("Ok")){
                    System.out.println(returnCode);
                }
                else {
                    step++;
                }

            }
            in.close();
            
            

        } catch (IOException e) {
           
            throw new RuntimeException(e);
        }
        catch (Exception e) {

            throw new RuntimeException(e);
        }


    }

    public static String[] files(String files){

        ArrayList<String> fl = new ArrayList<String>(Arrays.asList(files.split(", |,")));
        
        for(int i = 0; i < fl.size(); i++){

            if (isFileExists(new File(fl.get(i)))) 
                System.out.println(fl.get(i) + ": OK");
            else {
                System.out.println(fl.get(i) + ": Not found");
                fl.remove(i);           
            }
        }
        return (fl.toArray(new String[0]));
    }

    public static boolean isFileExists(File file) {
        return file.exists() && !file.isDirectory();
    }
}

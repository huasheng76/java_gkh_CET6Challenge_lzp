import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class Server extends JFrame implements Runnable{
    private Socket s = null;
    private ServerSocket ss = null;
    private JTextArea jta = new JTextArea();

    private Random rnd = new Random();
    private ArrayList<ChatThread>clients = new ArrayList<ChatThread>();//线程数组
    public Server()throws Exception{
        this.setTitle("服务器端");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(jta,BorderLayout.CENTER);
        jta.setBackground(Color.yellow);
        this.setSize(400,200);
        this.setVisible(true);
        ss = new ServerSocket(9999);//监听端口
        new Thread(this).start();
    }
    public void run(){
        try{
            while(true){
                //将客户端连接到服务器端
                s=ss.accept();

                ChatThread ct = new ChatThread(s);
                clients.add(ct);
                ct.start();
            }
        }catch(Exception ex){
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(this,"游戏异常退出！");
            System.exit(0);
        }
    }


    public class ChatThread extends Thread{
        private Socket s = null;
        private BufferedReader br =null;
        private PrintStream ps =null;
        private boolean canRun = true;
        //客户端线程
        public ChatThread(Socket s)throws Exception{
            this.s = s;
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            ps = new PrintStream(s.getOutputStream());
        }
        public void run(){
            try{
                int wn = rnd.nextInt(2220);
                //System.out.println("Start"+wn);
                String swn = "START#"+Integer.toString(wn);
                sendMessage(swn);
                while(canRun){
                    String str = br.readLine();
                    String[] strs =str.split("#");
                    if(strs[0].equals("LIFE")){
                        //收到扣除生命值消息
                        int rn = rnd.nextInt(2220);
                        String srn = "START#" + Integer.toString(rn);
                        sendMessage("LIFE#"+strs[1]+"#"+srn);
                    }
                    else if(strs[0].equals("WIN")){
                        //收到生命值为0消息
                        String msgWIN = "UWIN";
                        sendMessage(msgWIN);
                    }else if(strs[0].equals("ASKRN")){
                        //收到未回答消息
                        int rn1 = rnd.nextInt(640);
                        String swn1 = "START#" + Integer.toString(rn1);
                        //System.out.println("仅用于同步");
                        //System.out.println(swn1);
                        sendMessage("NOASK#"+swn1);
                    }
                }
            }catch (Exception ex){
                canRun=false;
                clients.remove(this);
            }
        }
    }

    //将信息转发给所有的客户端
    public void sendMessage(String msg){
        for(ChatThread ct:clients){
            ct.ps.println(msg);
        }
    }

    public static void main(String[] args)throws Exception{
        new Server();
    }
}



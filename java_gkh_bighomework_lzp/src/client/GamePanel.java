package client;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.Timer;
import java.util.Scanner;
import java.util.Vector;

public class GamePanel extends JPanel implements Runnable,ActionListener,KeyListener{
    private int life=10;
    private String tips = new String();
    private  JLabel lbMoveChar = new JLabel();//下落字符
    private  JLabel lbLife = new JLabel();//生命值
    private  JLabel lbempty = new JLabel();//提示字符
    private  JTextField tfword = new JTextField();//单词输入框
    private  JTextField tftips = new JTextField();//回答正确错误提示框
    private JTextField tfseparate = new JTextField();//分隔用的线（框高度为一）
    private JTextField tfseparate1 = new JTextField();//截止线
    private  JButton btn = new JButton("提交");//提交按钮
    private  char Keychar;

    private  Socket s = null;
    private Timer timer =new Timer(300,this);

    private Random rnd = new Random();
    private BufferedReader br = null;
    private PrintStream ps = null;

    private Vector<String> inputWords = new Vector<String>();//单词数组
    private String word = null;//单词英文
    private String Chinese = null;//单词中文
    private int il;//随机数
    String strSave = null;//要保存的单词
    String  Keystr = null;

    private boolean canRun = true;

    public  GamePanel(){
        this.setLayout(null);
        this.setBackground(Color.DARK_GRAY);
        this.setSize(900,500);

        //设置生命
        this.add(lbLife);
        lbLife.setFont(new Font("黑体",Font.BOLD,15));
        lbLife.setBackground(Color.yellow);
        lbLife.setForeground(Color.PINK);
        lbLife.setBounds(0,0,500,20);

        //设置单词提示
        this.add(lbempty);
        lbempty.setForeground(Color.PINK);
        lbempty.setBackground(Color.yellow);
        lbempty.setFont(new Font("黑体",Font.BOLD,15));
        lbempty.setBounds(450,400,300,50);

        //添加下落单词
        this.add(lbMoveChar);
        lbMoveChar.setFont(new Font("黑体",Font.BOLD,20));
        lbMoveChar.setForeground(Color.yellow);

        //下方输入区线
        this.add(tfseparate);
        tfseparate.setSize(900, 1);
        tfseparate.setLocation(0, 343);

        //掉落线
        this.add(tfseparate1);
        tfseparate1.setSize(900, 6);
        tfseparate1.setLocation(0, 295);
        tfseparate1.setBackground(Color.black);

        //设置单词框
        this.add(tfword);
        tfword.setLocation(100,355);
        tfword.setSize(500,40);

        //设置回答正确错误提示框
        this.add(tftips);
        tftips.setLocation(100,405);
        tftips.setSize(300,35);

        //设置提交按钮
        this.add(btn);
        btn.setBackground(Color.white);
        btn.setLocation(700, 355);
        btn.setSize(100, 50);

        //初始化
        this.init();
        this.addKeyListener( this);
        //连接服务器
        try{
            s = new Socket("127.0.0.1",9999);

            //JOptionPane.showMessageDialog(this,"连接成功");
            InputStream is = s.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            OutputStream os = s.getOutputStream();
            ps = new PrintStream(os);
            new Thread(this).start();

        }catch (Exception ex){
            javax.swing.JOptionPane.showMessageDialog(this,"游戏异常退出！");
            System.exit(0);

        }

        //提交按钮动作
        btn.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{if(tfword.getText().equals(word)){
                    writeFile("C:\\Users\\25498\\IdeaProjects\\java_gkh_bighomework_lzp\\right.exe",strSave);
                    life+=1;
                    System.out.println("恭喜回答正确");
                    tips="恭喜回答正确";
                    ps.println("LIFE#0");
                }

                else{
                    writeFile("C:\\Users\\25498\\IdeaProjects\\java_gkh_bighomework_lzp\\wrong.exe",strSave+" 答错");
                    life-=2;
                    ps.println("LIFE#0");
                    System.out.println("回答错误，答案是"+strSave);
                    tips="回答错误，答案是"+strSave;
                }
                    init();
                    checkFail();
                }catch (Exception ex){
                    canRun=false;
                    System.out.println("游戏异常退出");
                    System.exit(0);
                }

            }
        });

        timer.start();
    }

    //写文件
    public void writeFile(String filename,String str){
        try {
            FileOutputStream fos = new FileOutputStream(filename,true);
            byte[] b = str.getBytes();
            fos.write(b);
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //将单词变为随机一至二个提示字母
    public StringBuffer getText(String word,int index){
        StringBuffer empty = new StringBuffer();
        int length = word.length();
        for(int i=0;i<length;i++){
            if(i!=index){
                empty.append("_");
            }
            else {
                empty.append(word.charAt(i));
            }
        }
        return empty;
    }

    public StringBuffer getText(String word,int index1,int index2){
        StringBuffer empty = new StringBuffer();
        int length = word.length();
        for(int i=0;i<length;i++){
            if(i==index1 || i==index2){
                empty.append(word.charAt(i));
            }
            else {
                empty.append("_");
            }
        }
        return empty;
    }

    //从文件中读取单词
    public void readWords(String filename){
        try
        {
            FileInputStream fi = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fi,"UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String str = null;
            while(null != (str= br.readLine())){
                inputWords.add(str);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /*public synchronized void readLineFile(String filename, int il){
        try{
             FileInputStream fi = new FileInputStream(filename);
             InputStreamReader isr = new InputStreamReader(fi, "UTF-8");
             BufferedReader br = new BufferedReader(isr);
             System.out.println("读文件中的il是"+il);
        //br此处用来按行读取文档
        //in++;
        //System.out.println("readLineFile开始执行:"+in);
            while(br.readLine() != null && il >= 0){
                il--;
                //String str;
                if(il < 0) {
                    //用来按行读取，并分割出单词和解释

                    //String的split方法支持正则表达式；
                    //正则表达式\s表示匹配任何空白字符，+表示匹配一次或多次。

                    String str1 = br.readLine();
                    //保存的字符串
                    strSave = str1 + "\r\n";

                    String[] strs1 = str1.split("\\s+");
                    word = strs1[0];
                    Chinese = strs1[1];
                    System.out.println("1单词：" + word);
                    System.out.println("1单词源：" + Chinese);
                    //将题目给lbempty
                    Random rnd1 = new Random();
                    Random rnd2 = new Random();
                    int length = word.length();
                    int first = rnd1.nextInt(length);
                    int second = rnd2.nextInt(length);

                    if(first==second){
                        lbempty.setText(getText(word,first).toString());
                    }
                    else {
                        lbempty.setText(getText(word,first,second).toString());
                    }

                }
                break;
        }
        }catch (Exception e){
        e.printStackTrace();
    }
}*/
    //随机抽取单词
    public void readLineFile(String filename,int il){
        readWords(filename);
        String str = inputWords.get(il);
        strSave = str+"\r\n";
        String[] strs = str.split("\\s+");
        word = strs[0];
        Chinese = strs[1];
        //将题目给lbempty
        Random rnd1 = new Random();
        Random rnd2 = new Random();
        int length = word.length();
        int first = rnd1.nextInt(length);
        int second = rnd2.nextInt(length);

        if(first==second){
            lbempty.setText(getText(word,first).toString());
        }
        else {
            lbempty.setText(getText(word,first,second).toString());
        }
    }

    //初始化
    public void init(){
        lbLife.setText("当前生命值"+ life);

        readLineFile("C:\\Users\\25498\\IdeaProjects\\java_gkh_bighomework_lzp\\Word.txt",il);

        //TODO:位置设置
        lbempty.setBounds(450,400,300,50);
        tftips.setText(tips);
        lbMoveChar.setText(Chinese);
        lbMoveChar.setBounds(370,0,200,50);
        tfword.setText("");//将输入框置空
    }

    //运行函数
    public void run(){
        try{
            while(canRun){
                String str = br.readLine();
                //System.out.println(str);
                //System.out.println(il);
                //System.out.println("本轮单词是"+word);
                String[] strs = str.split("#");//将接收到的消息按#分割
                if(strs[0].equals("START")){
                    //开始游戏
                    il=Integer.parseInt(strs[1]);
                    checkFail();
                }
                else if(strs[0].equals("LIFE")){
                    //扣除生命值
                    int score = Integer.parseInt(strs[1]);
                    life+=score;
                    checkFail();
                    if(strs[2].equals("START")){
                        il=Integer.parseInt(strs[3]);
                    }
                }
                else if(strs[0].equals("UWIN")){
                    //赢下游戏
                    timer.stop();
                    javax.swing.JOptionPane.showMessageDialog(this,"游戏结束，你赢了！");
                    System.exit(0);
                }else if(strs[0].equals("NOASK")){
                    //双方都没有回答
                    //if(strs[1].equals("START"))
                    life--;
                    il = Integer.parseInt(strs[2]);
                    tips = "您没有回答，答案是"+strSave;
                }
                init();
            }
        }
        catch(Exception ex){
            canRun = false;
            javax.swing.JOptionPane.showMessageDialog(this,"游戏异常退出！");
            System.exit(0);
        }
    }

    //检查生命是否为0
    public void checkFail(){
        //init();
        //System.out.println("开始执行checkFail："+ic);
        lbLife.setText("当前生命值：" + life);

        //ic++;

        //System.out.println("结束ccheckFail："+ic);
        //客户端生命为0则发送消息
        if(life <= 0){
            ps.println("WIN#");
            timer.stop();
            javax.swing.JOptionPane.showMessageDialog(this,"生命值耗尽，游戏失败！");
            System.exit(0);
        }
    }

    //单词掉落动画
    public void actionPerformed(ActionEvent e){
        if(lbMoveChar.getY() >= 260){
            writeFile("C:\\Users\\25498\\IdeaProjects\\java_gkh_bighomework_lzp\\wrong.txt",strSave+" 没答");
            //life--;
            checkFail();
            System.out.println("您没有回答，答案是"+strSave);
            //tips = "您没有回答，答案是"+strSave;
            //随机数由服务器产生
            ps.println("ASKRN#");
            //System.out.println("底部调用checkFail");
        }
        lbMoveChar.setLocation(lbMoveChar.getX(),lbMoveChar.getY()+10);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e){

    }

    @Override
    public void keyReleased(KeyEvent e){
       /* Keychar=e.getKeyChar();
        try{if(Keychar=='q'){
            //Scanner sc = new Scanner(System.in);
            //String s = sc.nextLine();
            String s= JOptionPane.showInputDialog("请输入词汇：");
            //try{Thread.sleep(5000);}catch(Exception ex){}
            if(s.equals(word)){
                writeFile("C:\\Users\\25498\\IdeaProjects\\java_gkh_bighomework_lzp\\right.exe",strSave);
                life+=1;
                javax.swing.JOptionPane.showMessageDialog(this,"恭喜回答正确！");

                ps.println("LIFE#0");

            }else {
                writeFile("C:\\Users\\25498\\IdeaProjects\\java_gkh_bighomework_lzp\\wrong.exe",strSave+" 答错");
                life-=2;
                javax.swing.JOptionPane.showMessageDialog(this,"回答错误，答案是"+strSave);

                ps.println("LIFE#0");

            }
        }
            //init();
            checkFail();}catch (Exception ex){
            canRun=false;
            javax.swing.JOptionPane.showMessageDialog(this,"游戏异常退出！");
            System.exit(0);
        }*/
    }

    public static void main(String[] args){
        new GameFrame();
    }
}

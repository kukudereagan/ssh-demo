package com.lg.test.ctrl;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shell {
    //远程主机的ip地址
    private String ip;
    //远程主机登录用户名
    private String username;
    //远程主机的登录密码
    private String password;
    //设置ssh连接的远程端口
    public static final int DEFAULT_SSH_PORT = 22;
    //保存输出内容的容器
    private ArrayList<String> stdout;

    /**
     * 初始化登录信息
     * @param ip
     * @param username
     * @param password
     */
    public Shell(final String ip, final String username, final String password) {
        this.ip = ip;
        this.username = username;
        this.password = password;
        stdout = new ArrayList<String>();
    }
    /**
     * get stdout
     * @return
     */
    public ArrayList<String> getStandardOutput() {
        return stdout;
    }
    /**
     * 执行shell命令
     * @param command
     * @return
     */
    public int execute(final String command) {
        int returnCode = 0;
        JSch jsch = new JSch();
        MyUserInfo userInfo = new MyUserInfo();
        try {
            //创建session并且打开连接，因为创建session之后要主动打开连接
            Session session = jsch.getSession(username, ip, DEFAULT_SSH_PORT);
            session.setPassword(password);
            session.setUserInfo(userInfo);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            //打开通道，设置通道类型，和执行的命令
            Channel channel = session.openChannel("exec");
            ChannelExec channelExec = (ChannelExec)channel;
            channelExec.setCommand(command);

            channelExec.setInputStream(null);
            BufferedReader input = new BufferedReader(new InputStreamReader
                    (channelExec.getInputStream()));

            channelExec.connect();
            System.out.println("The remote command is :" + command);

            //接收远程服务器执行命令的结果
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
            }
            input.close();

            // 得到returnCode
            if (channelExec.isClosed()) {
                returnCode = channelExec.getExitStatus();
            }

            // 关闭通道
            channelExec.disconnect();
            //关闭session
            session.disconnect();

        } catch (JSchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnCode;
    }


    public static void main(String[] args) {
        Shell shell = new Shell("192.168.0.114", "root", "zaq12wsx#");
       // shell.execute("su - oracle <<! ./u01/app/ogg/ggsci <<! info all ");
        shell.execute("./../u01/app/ogg/test.sh");
        ArrayList<String> stdout = shell.getStandardOutput();
        List list = new ArrayList<>();
        for (String str : stdout) {
            if(str.startsWith("EXTRACT")){
                System.out.println(str);
                String  EXTRACT = str.substring(str.indexOf("EXTRACT")+"EXTRACT".length(),str.indexOf("Last Started"));
                String  LastStarted = str.substring(str.indexOf("Last Started")+"Last Started".length(),str.indexOf("Status"));
                String  Status = str.substring(str.indexOf("Status")+"Status".length(),str.length());
                Map map = new HashMap();
                map.put("EXTRACT",EXTRACT.replaceAll(" ",""));
                map.put("LastStarted",LastStarted.replaceAll("  ",""));
                map.put("Status",Status.replaceAll(" ",""));
                list.add(map);
            }
        }

        System.out.println(list.toString());


        /**
         * EXTRACT    EXTA
         * Last Started 2018-06-20 15:37
         * Status ABENDED
         * Checkpoint Lag       00:00:00 (updated 2683:12:09 ago)
         * Log Read Checkpoint  Oracle Redo Logs                     2018-06-29 16:29:00
         * Seqno 2670,
         * RBA 10435584
         * SCN 0.55572924 (55572924)
         *
         * EXTRACT    EXTB
         * Last Started 2018-06-20 17:13
         * Status ABENDEDCheckpoint Lag       00:00:00 (updated 2683:12:04 ago)Log Read Checkpoint  Oracle Redo Logs                     2018-06-29 16:29:05  Seqno 2670, RBA 10466816                     SCN 0.55572968 (55572968)EXTRACT    EXTC      Last Started 2018-05-24 10:10   Status STOPPEDCheckpoint Lag       00:00:00 (updated 2900:04:10 ago)Log Read Checkpoint  Oracle Redo Logs                     2018-06-20 15:36:57  Seqno 2557, RBA 32289280                     SCN 0.52397557 (52397557)EXTRACT    EXTKAFKA  Last Started 2018-09-04 17:44   Status ABENDEDCheckpoint Lag       00:00:00 (updated 572:11:08 ago)Log Read Checkpoint  Oracle Redo Logs                     2018-09-25 15:30:00  Seqno 3550, RBA 13998080                     SCN 0.82205776 (82205776)
         */
    }
}

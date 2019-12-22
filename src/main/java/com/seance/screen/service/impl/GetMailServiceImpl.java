package com.seance.screen.service.impl;

import com.seance.screen.common.DateStyle;
import com.seance.screen.common.DateUtils;
import com.seance.screen.dao.MailMessageDto;
import com.seance.screen.dao.MyMail;
import com.seance.screen.service.GetMailService;
import com.seance.screen.util.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

@Slf4j
@Service
public class GetMailServiceImpl implements GetMailService {

    @Autowired
    private RedisService redisService;

    private static String redisKey = "linDate";

    private static String repeat = "enclosure:";


    @Override
    public void getMail() {
        System.out.println("-------------开始时间-----------" + System.currentTimeMillis());
        Map<String, MailMessageDto> outData = new HashMap<>();
        List<MailMessageDto> messageDtos = new ArrayList<>();
        List<String> errorMsg = new ArrayList<>();
        String storeDir = "C:" + File.separator + "tmp";
        File file = new File(storeDir);
        if (!file.exists()) {
            file.mkdir();
        }
        File dateFile = new File(storeDir + File.separator + DateUtils.DateToString(new Date(), DateStyle.YYYY_MM_DD));
        if (!dateFile.exists()) {
            dateFile.mkdir();
        }
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", "imap.263.net");
        props.setProperty("mail.imap.port", "143");
        props.setProperty("mail.imap.connectiontimeout", "7,200,000");
        props.setProperty("mail.imap.timeout", "7,200,000");
        // 创建Session实例对象
        Session session = Session.getInstance(props);
        // 创建IMAP协议的Store对象
        try (Store store = session.getStore("imap");) {
            System.out.println("------------开始连接服务器---------" + System.currentTimeMillis());
            // 连接邮件服务器
            store.connect("linqingqing@camelotchina.com", "linjiamei4");
//            store.connect("sunshaobo@camelotchina.com", "nba2012");
            System.out.println("------------打开邮件---------" + System.currentTimeMillis());
            // 获得收件箱
            Folder folder = store.getFolder("INBOX");
            // 以读写模式打开收件箱
            folder.open(Folder.READ_ONLY);
            SearchTerm st = new ReceivedDateTerm(6, new Date());
//            SearchTerm stAnd = new AndTerm(new NotTerm(new OrTerm(new SubjectTerm("转发"), new SubjectTerm("回复"))), st);
            System.out.println("------------开始获取邮件---------" + System.currentTimeMillis());
            // 获得收件箱的邮件列表
            Message[] messages = folder.search(st);
            System.out.println("------------获取到:" + messages.length + "封邮件---------" + System.currentTimeMillis());
            String dateTime = "2019-01-01 00:00:00";
            Object obj = redisService.get(redisKey);
            if (obj != null) {
                dateTime = String.valueOf(obj);
            }
            Date oneDate = DateUtils.StringToDate(dateTime, DateStyle.YYYY_MM_DD_HH_MM_SS);
            Date saveDate = DateUtils.StringToDate(dateTime, DateStyle.YYYY_MM_DD_HH_MM_SS);
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                if (message.getReceivedDate().before(oneDate)) {
                    continue;
                }
                String subjectName = message.getSubject();
                if (subjectName.contains("回复") || subjectName.contains("转发") || subjectName.toLowerCase().contains("Re")) {
                    continue;
                }
                if (!(subjectName.contains("推") && (subjectName.contains("学信") || subjectName.contains("民教")))) {
                    continue;
                }
                System.out.println("========开始第-----------------" + i + "封邮件===========" + subjectName);
                MyMail mm = new MyMail((MimeMessage) message);
                try {
                    long time = System.currentTimeMillis();
//                    this.handleEnclosure(mm,(Part) message);
//                    mm.getMailContent((Part) message);
                    mm.writePart((Part) message);
                    messageDtos.add(mm.getMailMessageDto());
                    long time2 = System.currentTimeMillis();
                    System.out.println(time2 - time);
//                    boolean flag = this.handleMail(mm, outData, (Part) message);
                    System.out.println(System.currentTimeMillis() - time2);
                }catch (Exception e) {
                    log.error(e.getMessage(), e);
                    errorMsg.add(subjectName + "," + DateUtils.DateToString(message.getReceivedDate(), DateStyle.YYYY_MM_DD_HH_MM_SS));
                    continue;
                }
                if (message.getReceivedDate().after(saveDate)) {
                    saveDate = message.getReceivedDate();
                }
            }
            folder.close();
            System.out.println(messageDtos.size());
            redisService.set(redisKey, DateUtils.DateToString(saveDate, DateStyle.YYYY_MM_DD_HH_MM_SS));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        this.outPrintExcel(outData);
        if (errorMsg.size() > 0) {
            this.outErrorExcel(errorMsg);
        }
        System.out.println("===============结束==============" + System.currentTimeMillis());
    }

    private void outErrorExcel(List<String> errorMsg) {
        String path = "C:" + File.separator + "tmp" + File.separator + "错误" + System.currentTimeMillis() + ".csv";
        File file = new File(path);
        try (FileOutputStream out = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(out, "gbk");
             BufferedWriter bw = new BufferedWriter(osw)) {
            bw.append("主题").append(",")
                    .append("时间").append("\r");
            for (String s : errorMsg) {
                bw.append(s).append("\r");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void outPrintExcel(Map<String, MailMessageDto> outData) {
        String path = "C:" + File.separator + "tmp" + File.separator + System.currentTimeMillis() + ".csv";
        File file = new File(path);
        try (FileOutputStream out = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(out, "gbk");
             BufferedWriter bw = new BufferedWriter(osw)) {
            bw.append("组别").append(",")
                    .append("姓名").append(",")
                    .append("年限").append(",")
                    .append("岗位").append(",")
                    .append("现在薪资").append(",")
                    .append("期望薪资").append(",")
                    .append("预计到岗时间").append(",")
                    .append("HR").append(",")
                    .append("重复简历").append(",")
                    .append("附件问题").append("\r");
            if (outData != null && !outData.isEmpty()) {
                for (Map.Entry<String, MailMessageDto> entry : outData.entrySet()) {
                    MailMessageDto dto = entry.getValue();
                    bw.append(this.handleNull(dto.getGroup())).append(",")
                            .append(dto.getName()).append(",")
                            .append(this.handleNull(dto.getYears())).append(",")
                            .append(dto.getPost()).append(",")
                            .append(this.handleNull(dto.getSalaryNow())).append(",")
                            .append(this.handleNull(dto.getSalaryExpectation())).append(",")
                            .append(this.handleNull(dto.getExpectedArrivalTime())).append(",")
                            .append(dto.getHr()).append(",")
                            .append(this.handleNull(dto.getRepeat())).append(",")
                            .append(this.handleNull(dto.getEnclosure())).append("\r");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(String path, String s, MailMessageDto messageDto) {
        if (!StringUtils.isEmpty(path)) {
            try {
                this.copyFileUsingFileChannels(new File(path), new File(s));
                System.out.println("从--" + path + "--复制到--" + s);
            } catch (IOException e) {
                messageDto.setEnclosure("附件复制错误");
                e.printStackTrace();
            }
        }
    }


    private void handleEnclosure(MyMail myMail, Part message) {
        boolean flag = false;
        try {
            flag = myMail.isContainAttach(message);
        } catch (Exception e) {
            myMail.getMailMessageDto().setEnclosure("读取附件发生未知错误");
        }
        if (flag) {
            try {
                System.out.println("开始下载=================");
                //保存文件
                myMail.saveAttachMent(message);
            } catch (IllegalArgumentException e) {
                myMail.getMailMessageDto().setEnclosure("附件下载错误");
            } catch (Exception e) {
                myMail.getMailMessageDto().setEnclosure("附件下载错误");
            }
        } else if (StringUtils.isEmpty(myMail.getMailMessageDto().getEnclosure())) {
            myMail.getMailMessageDto().setEnclosure("附件为空");
        }

    }


    private String handleNull(String str) {
        return str != null ? str.trim() : "";
    }

    private void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

}

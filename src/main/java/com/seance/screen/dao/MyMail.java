package com.seance.screen.dao;

import com.seance.screen.common.DateStyle;
import com.seance.screen.common.DateUtils;
import com.seance.screen.common.ThreadPoolUtil;
import org.apache.tomcat.jni.Time;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.util.StringUtils;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.*;

public class MyMail {


    private MimeMessage mimeMessage = null;

    /**
     * 存放邮件内容的StringBuffer对象
     */
    private StringBuffer bodyText = new StringBuffer();

    private int length = 0;

    private int length1 = 0;

    /**
     * 分组
     */
    private String group;

    private MailMessageDto mailMessageDto;


    /**
     * 构造函数,初始化一个MimeMessage对象
     */
    public MyMail() {
    }

    public MailMessageDto getMailMessageDto() {
        return mailMessageDto;
    }

    public void setMailMessageDto(MailMessageDto mailMessageDto) {
        this.mailMessageDto = mailMessageDto;
    }

    public MyMail(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
        this.mailMessageDto = new MailMessageDto();
        try {
            this.mailMessageDto.setSubject(mimeMessage.getSubject());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 　*　获得邮件正文内容
     */
    public String getBodyText() {
        return bodyText.toString();
    }


    /**
     * 　　*　解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件
     * 　　*　主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
     */

    public void getMailContent(Part part) throws Exception {
        // 获得邮件的MimeType类型
        boolean conName = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/plain") && !conName) {
            // text/plain 类型
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !conName) {
            // text/html 类型
            try {
                bodyText.append((String) part.getContent());
                this.handleHtml();
            } catch (Exception e) {
                this.handleText();
            }
            System.out.println("============获取内容循环===" + length + "次========");
        } else if (part.isMimeType("message/rfc822")) {
            length++;
            System.out.println("==============获取" + length + "=========");
            getMailContent((Part) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                length++;
                System.out.println("==============获取" + length + "=========");
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailContent(bodyPart);
            }
        }
        System.out.println(part.getContentType());
        System.out.println(part.getContent());
    }

    public void parseMultipart(Part part) throws MessagingException, IOException {
        Multipart multipart = (Multipart) part.getContent();
        int count = multipart.getCount();
        System.out.println("===========获取内容开始============总共:" + count);
        for (int idx = 0; idx < count; idx++) {
            BodyPart bodyPart = multipart.getBodyPart(idx);
            if (bodyPart.isMimeType("text/plain")) {
                bodyText.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("text/html")) {
                bodyText.append(bodyPart.getContent());
            } else if (bodyPart.isMimeType("multipart/*")) {
                length++;
                System.out.println("==========获取内容==" + length + "次");
                parseMultipart((Part) bodyPart.getContent());
            }
            System.out.println(bodyText.toString());
        }
    }

    /**
     * 判断此邮件是否包含附件
     */
    public boolean isContainAttach(Part part) throws Exception {
        boolean attachFlag = false;
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE)))) {
                    attachFlag = true;
                } else if (mPart.isMimeType("multipart/*")) {
                    attachFlag = isContainAttach((Part) mPart);
                } else {
                    String conType = mPart.getContentType();
                    if (conType.toLowerCase().indexOf("application") != -1) {
                        attachFlag = true;
                    }
                    if (conType.toLowerCase().indexOf("name") != -1) {
                        attachFlag = true;
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            attachFlag = isContainAttach((Part) part.getContent());
        }
        return attachFlag;
    }


    /**
     * 　*　保存附件
     */

    public void saveAttachMent(Part part) throws Exception {
        String fileName = "";
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE)))) {
                    fileName = mPart.getFileName();
                    if (fileName.toLowerCase().indexOf("gb2312") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    if (fileName.toLowerCase().indexOf("utf-8") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    saveFile(fileName, mPart.getInputStream());
                } else if (mPart.isMimeType("multipart/*")) {
                    length1++;
                    System.out.println("==============下载" + length1 + "=========");
                    saveAttachMent(mPart);
                } else {
                    fileName = mPart.getFileName();
                    if ((fileName != null)) {
                        if ((fileName.toLowerCase().indexOf("gb2312") != -1)) {
                            fileName = MimeUtility.decodeText(fileName);
                        } else if (fileName.toLowerCase().indexOf("utf-8") != -1) {
                            fileName = MimeUtility.decodeText(fileName);
                        }
                        try (InputStream is = mPart.getInputStream()) {
                            this.saveFile(fileName, is);
                        } catch (IOException | MessagingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachMent((Part) part.getContent());
        }
    }

    /**
     * 　*　真正的保存附件到指定目录里
     */
    private void saveFile(String fileName, InputStream in) {
        System.out.println("=====下载文件循环了" + length1 + "======次===========");
        if (!(fileName.indexOf(".doc") > -1 || fileName.indexOf(".docx") > -1 || fileName.indexOf(".jpg") > -1)) {
            return;
        }
        String osName = System.getProperty("os.name");
        String storeDir = null;
        String separator = File.separator;
        if (osName == null) {
            osName = "";
        }
        if (osName.toLowerCase().indexOf("win") != -1) {
            storeDir = "c:\\tmp";
        } else {
            storeDir = "/tmp";
        }
        String path = storeDir + separator
                + DateUtils.DateToString(new Date(), DateStyle.YYYY_MM_DD) + separator
                + "我的临时文件夹";
        File groupFile = new File(path);
        if (!groupFile.exists()) {
            groupFile.mkdir();
        }
        File storeFile = new File(path + separator + fileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(storeFile));
             BufferedInputStream bis = new BufferedInputStream(in)) {
            int c;
            while ((c = bis.read()) != -1) {
                bos.write(c);
                bos.flush();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new IllegalArgumentException("文件保存失败!");
        }
        this.mailMessageDto.addFiles(storeFile.getName(), storeFile.getPath());
        System.out.println("=========" + storeFile.getName() + "下载完成");
    }

    private void handleText() {
        this.mailMessageDto.setEffective(false);
        System.out.println(this.getBodyText());
        this.mailMessageDto.setCount(this.getBodyText());
    }

    private void handleHtml() {
        this.mailMessageDto.setEffective(false);
        Document doc = Jsoup.parse(this.getBodyText());
        if (doc.select("table").isEmpty()) {
            return;
        }
        Element table = doc.select("table").get(0);
        if (table == null) {
            return;
        }
        Elements rows = table.select("tr");
        if (rows.isEmpty() || rows.get(0) == null) {
            return;
        }
        if (rows.get(0).select("td").isEmpty() || rows.get(0).select("td").get(0) == null) {
            return;
        }
        if (!"组别".equals(rows.get(0).select("td").get(0).text())) {
            return;
        }
        if (rows.get(1) == null) {
            return;
        }
        Elements cols = rows.get(1).select("td");
        if (cols.isEmpty()) {
            return;
        }
        this.mailMessageDto.setEffective(true);
        this.copyMessage(cols);
    }

    private void copyMessage(Elements cols) {
        this.group = this.handleNull(cols.get(0).text());
        this.handleGroups();
        this.mailMessageDto.setName(this.handleNull(cols.get(1).text()));
        this.mailMessageDto.setYears(this.handleNull(cols.get(2).text()));
        this.mailMessageDto.setPost(this.handleNull(cols.get(3).text()));
        this.mailMessageDto.setSalaryNow(this.handleNull(cols.get(4).text()));
        this.mailMessageDto.setSalaryExpectation(this.handleNull(cols.get(5).text()));
        this.mailMessageDto.setExpectedArrivalTime(this.handleNull(cols.get(6).text()));
        this.mailMessageDto.setHr(this.handleNull(cols.get(7).text()));
        this.mailMessageDto.setPhone(this.handleNull(cols.get(8).text()));
    }

    private void handleGroups() {
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、 ？]";
        String[] split = this.group.split(regEx);
        for (String s : split) {
            if (!StringUtils.isEmpty(s)) {
                this.mailMessageDto.addGroups(s);
            }
        }
    }

    private String handleNull(String str) {
        return str != null ? str.trim() : null;
    }

    public void writeEnvelope(Message m) throws Exception {
        System.out.println("This is the message envelope");
        System.out.println("---------------------------");
        Address[] a;
        // FROM
        if ((a = m.getFrom()) != null) {
            for (int j = 0; j < a.length; j++) {
                System.out.println("FROM: " + a[j].toString());
            }
        }
        // TO
        if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
            for (int j = 0; j < a.length; j++) {
                System.out.println("TO: " + a[j].toString());
            }
        }
    }

    public void writePart(Part p) throws Exception {
//        if (p instanceof Message) {
//            writeEnvelope((Message) p);
//        }
        System.out.println("==============CONTENT-TYPE: " + p.getContentType() + "=============");
        if (p.isMimeType("application/octet-stream")) {
            System.out.println("========================错误流-------------------");
            return;
        }
        //check if the content is plain text
        if (p.isMimeType("text/plain")) {
            bodyText.append((String) p.getContent());
        } else if (p.isMimeType("multipart/*")) {
            System.out.println("This is a Multipart");
            System.out.println("---------------------------");
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                writePart(mp.getBodyPart(i));
            }
        } else if (p.isMimeType("message/rfc822")) {
            System.out.println("This is a Nested Message");
            System.out.println("---------------------------");
            writePart((Part) p.getContent());
        } else if (p.isMimeType("image/jpeg")) {
            System.out.println("--------> image/jpeg");
        } else if (p.getContentType().contains("image/")) {
            System.out.println("--------> image/jpeg");
        } else if (p.isMimeType("text/html")) {
            System.out.println(p.getClass());
            System.out.println(p.getSize());
            System.out.println(p.getLineCount());
            System.out.println("getContent开始");
            final Object[] o = {""};
            final boolean[] flag = {true, true};
            System.out.println("进try了");
            long nowTime = System.currentTimeMillis();
            final Part[] p1 = {p};
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        o[0] = p1[0].getContent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        flag[0] = false;
                        System.out.println("空指针异常了");
                    } finally {
                        flag[1] = false;
                    }
                }
            });
            thread.start();
            while (flag[1]) {
                Thread.sleep(1000L);
                if (System.currentTimeMillis() - nowTime > 10000 && o[0].equals("")) {
                    thread.interrupt();
                    System.out.println("超长中断了~~~================" + (System.currentTimeMillis() - nowTime));
                    flag[0] = false;
                    flag[1] = false;
                }
            }
            bodyText.append((String) o[0]);
            if (!flag[0]) {
                this.handleText();
            } else {
                this.handleHtml();
            }
        } else {
//            if (p.getContent() instanceof String) {
//                System.out.println("==============字符串==============");
//                System.out.println("---------------------------");
//            } else if (p.getContent() instanceof InputStream) {
//                System.out.println("==============输入流==============");
//                System.out.println("---------------------------");
//                ((InputStream) p.getContent()).close();
//                InputStream is = (InputStream) o;
//                is = (InputStream) o;
//                int c;
//                while ((c = is.read()) != -1) {
//                    System.out.write(c);
//                }
//            } else {
            System.out.println("==============未知类型==============");
            System.out.println("---------------------------");
//                System.out.println(p.getContent().toString());
//            }
        }
    }

}

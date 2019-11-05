package com.seance.screen.dao;

import com.seance.screen.common.DateStyle;
import com.seance.screen.common.DateUtils;
import com.sun.mail.imap.IMAPBodyPart;
import org.springframework.util.StringUtils;

import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.util.*;

public class MyMail {


    private MimeMessage mimeMessage = null;

    private int length = 0;

    /**
     * 存放邮件内容的StringBuffer对象
     */
    private StringBuffer bodyText = new StringBuffer();

    /**
     * 分组
     */
    private String group;

    private Map<String, String> files;


    /**
     * 构造函数,初始化一个MimeMessage对象
     */
    public MyMail() {
    }

    public MyMail(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Map<String, String> getFiles() {
        return this.files;
    }

    public void addFiles(String name, String path) {
        if (this.files != null) {
            this.files.put(name, path);
        } else {
            this.files = new HashMap<>();
            this.files.put(name, path);
        }

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
        if (length > 20) {
            return;
        }
        String contentType = part.getContentType();
        // 获得邮件的MimeType类型
        int nameIndex = contentType.indexOf("name");
        boolean conName = false;
        if (nameIndex != -1) {
            conName = true;
        }
        if (part.isMimeType("text/plain") && conName == false) {
            // text/plain 类型
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && conName == false) {
            // text/html 类型
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
            // multipart/*
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                length++;
                getMailContent(multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) {
            // message/rfc822
            length++;
            getMailContent((Part) part.getContent());
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
                    saveAttachMent(mPart);
                } else {
                    fileName = mPart.getFileName();
                    if ((fileName != null)) {
                        if ((fileName.toLowerCase().indexOf("gb2312") != -1)) {
                            fileName = MimeUtility.decodeText(fileName);
                        } else if (fileName.toLowerCase().indexOf("utf-8") != -1) {
                            fileName = MimeUtility.decodeText(fileName);
                        }
                        saveFile(fileName, mPart.getInputStream());
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
    private void saveFile(String fileName, InputStream in) throws Exception {
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
        String group = StringUtils.isEmpty(this.group) ? "空分组" : this.group;
        String path = storeDir + separator
                + DateUtils.DateToString(new Date(), DateStyle.YYYY_MM_DD) + separator
                + group;
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
        this.addFiles(storeFile.getName(), storeFile.getPath());
        System.out.println("=========" + storeFile.getName() + "下载完成");
    }


}

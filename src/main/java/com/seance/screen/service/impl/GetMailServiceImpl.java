package com.seance.screen.service.impl;

import com.seance.screen.common.DateStyle;
import com.seance.screen.common.DateUtils;
import com.seance.screen.dao.EasyEntryDto;
import com.seance.screen.dao.MailMessageDto;
import com.seance.screen.dao.MyMail;
import com.seance.screen.service.GetMailService;
import com.seance.screen.util.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Slf4j
@Service
public class GetMailServiceImpl implements GetMailService {

    @Autowired
    private RedisService redisService;

    private static String redisKey = "linDate";

    private static String repeat = "enclosure:";


    @Override
    public void getMail(String emailName, String passWord,String typeName) {
        System.out.println("-------------开始时间-----------" + System.currentTimeMillis());
        List<EasyEntryDto> outData = new ArrayList();
        List<String> errorMsg = new ArrayList<>();
        String storeDir = "C:" + File.separator + "tmp";
        File file = new File(storeDir);
        if (!file.exists()) {
            file.mkdir();
        }
//        File dateFile = new File(storeDir + File.separator + DateUtils.DateToString(new Date(), DateStyle.YYYY_MM_DD));
//        if (!dateFile.exists()) {
//            dateFile.mkdir();
//        }
        Date oneDate = this.getBeginTime(storeDir);
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
            store.connect(emailName, passWord);
//            store.connect("sunshaobo@camelotchina.com", "nba2012");
            System.out.println("------------打开邮件---------" + System.currentTimeMillis());
            // 获得收件箱
            String fold = "INBOX";
            if (emailName.contains("sunshaobo") && "百度".equals(typeName)) fold = "百度IDG";
            Folder folder = store.getFolder(fold);
//            Folder defaultFolder = store.getDefaultFolder();
//            Folder[] allFolder = defaultFolder.list();
            // 以读写模式打开收件箱
            folder.open(Folder.READ_ONLY);
            SearchTerm st = new ReceivedDateTerm(6, oneDate);
//            SearchTerm stAnd = new AndTerm(new NotTerm(new OrTerm(new SubjectTerm("转发"), new SubjectTerm("回复"))), st);
            System.out.println("------------开始获取邮件---------" + System.currentTimeMillis());
            // 获得收件箱的邮件列表
            Message[] messages = folder.search(st);
            System.out.println("------------获取到:" + messages.length + "封邮件---------" + System.currentTimeMillis());
            Date saveDate = oneDate;
            int emailCont = 0;
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];
                if (message.getReceivedDate().after(oneDate)) {
                    String subjectName = message.getSubject();
                    if (subjectName.replace(" ", "").contains(typeName) && !subjectName.contains("答复")
                            && !subjectName.contains("回复") && !subjectName.toLowerCase().contains("re")&& !subjectName.toLowerCase().contains("fw")) {
                        emailCont++;
                        Address[] from = message.getFrom();
                        String addressFrom = InternetAddress.toString(from);
                        if (!StringUtils.isEmpty(addressFrom)) {
                            if (addressFrom.indexOf("<") > 0) {
                                addressFrom = addressFrom.split("<")[1].split("@")[0];
                            } else if (addressFrom.indexOf("@") > 0) {
                                addressFrom = addressFrom.split("@")[0];
                            }
                        }

                        EasyEntryDto entryDto = new EasyEntryDto();
                        entryDto.setKey(addressFrom);
                        entryDto.setValue(subjectName);
                        entryDto.setDateTime(DateUtils.DateToString(message.getReceivedDate(),"yyyy-MM-dd HH:mm:ss"));
                        outData.add(entryDto);
                        System.out.println("========开始第-----------------" + i + "封邮件===========" + subjectName);
                        if (message.getReceivedDate().after(saveDate)) {
                            saveDate = message.getReceivedDate();
                        }
                    }
                }
            }
            folder.close();
            this.setgetBeginTime(storeDir, saveDate);
            System.out.println("带"+typeName+"标题简历不算回复邮件总计："+emailCont+"封邮件");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        this.outPrintExcel(outData,typeName);
        if (errorMsg.size() > 0) {
            this.outErrorExcel(errorMsg);
        }
        System.out.println("===============结束==============" + System.currentTimeMillis());
        try {
            Runtime.getRuntime().exec("cmd /c start C:/tmp");
        } catch (IOException e) {
        }
    }

    private void setgetBeginTime(String storeDir, Date saveDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        File file = new File(storeDir + File.separator + "time.txt");
        if (!file.isFile() || !file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException var18) {
                var18.printStackTrace();
            }
        }
        try {
            FileWriter fw = new FileWriter(file);
            Throwable var6 = null;

            try {
                fw.write(sdf.format(saveDate));
            } catch (Throwable var17) {
                var6 = var17;
                throw var17;
            } finally {
                if (fw != null) {
                    if (var6 != null) {
                        try {
                            fw.close();
                        } catch (Throwable var16) {
                            var6.addSuppressed(var16);
                        }
                    } else {
                        fw.close();
                    }
                }

            }
        } catch (Exception var20) {
        }
    }

    private Date getBeginTime(String storeDir) {
        File file = new File(storeDir + File.separator + "time.txt");
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd 01:00:00");
        Date returnDate = new Date();
        try {
            returnDate = sf.parse(sf.format(new Date()));
            if (file.isFile() && file.exists()) {
                FileInputStream fileInput = new FileInputStream(file);
                InputStreamReader read = new InputStreamReader(fileInput, "gbk");
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    if (!StringUtils.isEmpty(lineTxt)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        returnDate = sdf.parse(lineTxt);
                    }
                }
            }
        } catch (Exception e) {

        }

        return returnDate;
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


    private List<EasyEntryDto> getHandleData(List<EasyEntryDto> outData,String typename) throws Exception {
        Workbook wb = null;
        String path = "C:" + File.separator + "tmp" + File.separator + "nameCode.xlsx";
        File file = new File(path);
        InputStream fis = new FileInputStream(file);
        wb = new XSSFWorkbook(fis);
        Sheet sheet = wb.getSheetAt(0);
        Map<String, String> nameMap = new HashMap();
        Iterator var8 = sheet.iterator();
        while (var8.hasNext()) {
            Row row = (Row) var8.next();
            if (row.getRowNum() > 0) {
                if("快手".equals(typename)){
                    nameMap.put(row.getCell(0).toString(), row.getCell(1).toString());
                }else if ("百度".equals(typename)){
                    String name= row.getCell(1).toString();
                    if (!ObjectUtils.isEmpty(row.getCell(2))){
                        name = name + "-_-" + row.getCell(2);
                    }
                    nameMap.put(row.getCell(0).toString(), name);
                }

            }
        }
        for (EasyEntryDto outDatum : outData) {
            if (nameMap.containsKey(outDatum.getKey())) {
                if ("快手".equals(typename)){
                    outDatum.setName(nameMap.get(outDatum.getKey()));
                }else if ("百度".equals(typename)){
                    String name = nameMap.get(outDatum.getKey());
                    String team = "";
                    if (name.contains("-_-")){
                        team = name.split("-_-")[1];
                        name = name.split("-_-")[0];
                    }
                    outDatum.setName(name);
                    outDatum.setTeam(team);
                }
            }
        }
        return outData;
    }


    private void outPrintExcel(List<EasyEntryDto> outData,String typeName) {
        String path = "C:" + File.separator + "tmp" + File.separator + DateUtils.stampToDate(System.currentTimeMillis()) + ".csv";
        File file = new File(path);

        try {
            this.getHandleData(outData,typeName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (FileOutputStream out = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(out, "gbk");
             BufferedWriter bw = new BufferedWriter(osw)) {
            if ("快手".equals(typeName)){
                bw.append("HR").append(",")
                        .append("候选人").append(",")
                        .append("主题").append("\r");
            }else if("百度".equals(typeName)){
                bw.append("团队").append(",")
                        .append("HR").append(",")
                        .append("候选人").append(",")
                        .append("职位").append(",")
                        .append("区域").append(",")
                        .append("推荐时间").append(",")
                        .append("主题").append("\r");
            }
            String subjectName;
            String name,post,area;
            if (!CollectionUtils.isEmpty(outData)) {
                if ("快手".equals(typeName)){
                    for (EasyEntryDto outDatum : outData) {
                        name = "";
                        subjectName = outDatum.getValue();
                        if (subjectName.contains("【") && subjectName.contains("】")) {
                            name = subjectName.substring(subjectName.indexOf("【") + 1, subjectName.indexOf("】"));
                        }
                        bw.append(StringUtils.isEmpty(outDatum.getName()) ? outDatum.getKey() : outDatum.getName()).append(",")
                                .append(name).append(",")
                                .append(subjectName).append("\r");
                    }
                }else if("百度".equals(typeName)){
                    for (EasyEntryDto outDatum : outData) {
                        name = "";
                        post = "";
                        area = "";
                        subjectName = outDatum.getValue();
                        if (subjectName.contains("_")) {
                            String[] subName = subjectName.split("_");
                            for (int i = 0; i < subName.length; i++) {
                                switch (i){
                                    case 1:
                                        post = subName[i];
                                        break;
                                    case 2:
                                        name = subName[i];
                                        break;
                                    case 3:
                                        area = subName[i];
                                        break;
                                }
                            }
                        }
                        bw.append(StringUtils.isEmpty(outDatum.getTeam()) ? "" : outDatum.getTeam()).append(",")
                                .append(StringUtils.isEmpty(outDatum.getName()) ? outDatum.getKey() : outDatum.getName()).append(",")
                                .append(name).append(",")
                                .append(post).append(",")
                                .append(area).append(",")
                                .append(outDatum.getDateTime()).append(",")
                                .append(subjectName).append("\r");
                    }
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

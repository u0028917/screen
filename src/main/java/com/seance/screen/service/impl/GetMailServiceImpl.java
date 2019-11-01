package com.seance.screen.service.impl;

import com.seance.screen.common.DateStyle;
import com.seance.screen.common.DateUtils;
import com.seance.screen.common.ShowMail;
import com.seance.screen.dao.MyMail;
import com.seance.screen.service.GetMailService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.search.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GetMailServiceImpl implements GetMailService {


    @Override
    public void getMail() {
        Map<String, List<String>> outData = new HashMap<>();
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", "imap.263.net");
        props.setProperty("mail.imap.port", "143");
        // 创建Session实例对象
        Session session = Session.getInstance(props);
        // 创建IMAP协议的Store对象
        try (Store store = session.getStore("imap");) {
            // 连接邮件服务器
            store.connect("sunshaobo@camelotchina.com", "*******");
            // 获得收件箱
            Folder folder = store.getFolder("INBOX");
            // 以读写模式打开收件箱
            folder.open(Folder.READ_ONLY);
            SearchTerm st = new ReceivedDateTerm(6, DateUtils.StringToDate("2019-10-23", DateStyle.YYYY_MM_DD));
            SearchTerm stAnd = new AndTerm(new NotTerm(new OrTerm(new SubjectTerm("转发"), new SubjectTerm("回复"))), st);
            // 获得收件箱的邮件列表
            Message[] messages = folder.search(stAnd);
            for (Message message : messages) {
                String subjectName = message.getSubject();
                if (subjectName.contains("回复") || subjectName.contains("转发")) {
                    continue;
                }
                if (!(subjectName.contains("推") && (subjectName.contains("学信") || subjectName.contains("民教")))) {
                    continue;
                }
                MyMail mm = new MyMail((MimeMessage) message);
                try {
                    mm.getMailContent((Part) message);
                    this.handleMail(mm, outData);
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }


    }

    private boolean handleMail(MyMail myMail, Map<String, List<String>> outData) {
        Document doc = Jsoup.parse(myMail.getBodyText());
        Element table = doc.select("table").get(0);
        Elements rows = table.select("tr");
        if (!"组别".equals(rows.get(0).select("td").get(0).text())) {
            return false;
        }
        Elements cols = rows.get(1).select("td");
        List<String> collect = cols.stream().map(col -> col.text()).collect(Collectors.toList());
        outData.put(collect.get(1).trim() + collect.get(8).trim(), collect);
        return true;
    }

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imap");
        props.setProperty("mail.imap.host", "imap.263.net");
        props.setProperty("mail.imap.port", "143");

        // 创建Session实例对象
        Session session = Session.getInstance(props);
        // 创建IMAP协议的Store对象
        Store store = session.getStore("imap");
        // 连接邮件服务器
        store.connect("sunshaobo@camelotchina.com", "*******");
        // 获得收件箱
        Folder folder = store.getFolder("INBOX");
        // 以读写模式打开收件箱
        folder.open(Folder.READ_ONLY);
        SearchTerm st = new ReceivedDateTerm(6, DateUtils.StringToDate("2019-10-23", DateStyle.YYYY_MM_DD));
        SearchTerm stAnd = new AndTerm(new NotTerm(new OrTerm(new SubjectTerm("转发"), new SubjectTerm("回复"))), st);
        // 获得收件箱的邮件列表
        Message[] messages = folder.search(stAnd);
        System.out.println("---------------------");
        for (Message message : messages) {
            System.out.println(message.getSubject());
        }
        System.out.println("---------------------");
        ShowMail sm = new ShowMail((MimeMessage) messages[1]);
        sm.getMailContent((Part) messages[1]);
        System.out.println("有附件" + sm.isContainAttach((Part) messages[1]));
        Document doc = Jsoup.parse(sm.getBodyText());
        Element table = doc.select("table").get(0);
        Elements rows = table.select("tr");
        Elements cols = rows.get(0).select("td");
        System.out.println("===========");
        rows.get(1).select("td").stream().map(col -> col.text()).forEach(System.out::println);
        System.out.println("===========");
        System.out.println(sm.getBodyText());
        System.out.println("邮件数量" + messages.length);
        folder.close();
        store.close();
    }
}

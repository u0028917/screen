package com.seance.screen.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MailMessageDto {
    /**
     * 推送时间
     */
    private String pushTime;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 期望薪资
     */
    private String salaryExpectation;
    /**
     * 姓名
     */
    private String name;
    /**
     * 性别
     */
    private String sex;
    /**
     * 年限
     */
    private String years;
    /**
     * 岗位
     */
    private String post;
    /**
     * 学历
     */
    private String salaryNow;

    /**
     * 预计到岗时间
     */
    private String expectedArrivalTime;
    /**
     * HR
     */
    private String hr;
    /**
     * 项目名称
     */
    private String subject;
    /**
     * 重复简历
     */
    private String repeat;
    /**
     * 附件
     */
    private String enclosure;


    private Boolean isEffective;

    private List<String> groups;

    private Map<String, String> files;

    private String count;

    public String getPushTime() {
        return pushTime;
    }

    public void setPushTime(String pushTime) {
        this.pushTime = pushTime;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYears() {
        return years;
    }

    public void setYears(String years) {
        this.years = years;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getSalaryNow() {
        return salaryNow;
    }

    public void setSalaryNow(String salaryNow) {
        this.salaryNow = salaryNow;
    }

    public String getSalaryExpectation() {
        return salaryExpectation;
    }

    public void setSalaryExpectation(String salaryExpectation) {
        this.salaryExpectation = salaryExpectation;
    }

    public String getExpectedArrivalTime() {
        return expectedArrivalTime;
    }

    public void setExpectedArrivalTime(String expectedArrivalTime) {
        this.expectedArrivalTime = expectedArrivalTime;
    }

    public String getPhone() {
        return phone;
    }

    public String getHr() {
        return hr;
    }

    public void setHr(String hr) {
        this.hr = hr;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public void addGroups(String group) {
        if (this.groups == null) {
            this.groups = new ArrayList<>();
        }
        this.groups.add(group);
    }

    public List<String> getGroups() {
        return this.groups;
    }

    public Boolean getEffective() {
        return isEffective;
    }

    public void setEffective(Boolean effective) {
        isEffective = effective;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public Map<String, String> getFiles() {
        return this.files;
    }

    public void addFiles(String name, String path) {
        if (this.files == null) {
            this.files = new HashMap<>();
        }
        this.files.put(name, path);
    }
}

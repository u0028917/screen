package com.seance.screen.dao;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MailMessageDto {
    /**
     * 组别
     */
    private String group;
    /**
     * 姓名
     */
    private String name;
    /**
     * 年限
     */
    private String years;
    /**
     * 岗位
     */
    private String post;
    /**
     * 现在薪资
     */
    private String salaryNow;
    /**
     * 期望薪资
     */
    private String salaryExpectation;
    /**
     * 预计到岗时间
     */
    private String expectedArrivalTime;
    /**
     * HR
     */
    private String hr;
    /**
     * 联系电话
     */
    private String phone;

    /**
     * 重复简历
     */
    private String repeat;
    /**
     * 附件
     */
    private String enclosure;


    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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
}

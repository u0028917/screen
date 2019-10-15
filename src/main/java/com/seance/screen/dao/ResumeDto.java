package com.seance.screen.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * @author master
 */

public class ResumeDto {
    /**
     * 姓名
     */
    String name;
    /**
     * 电话
     */
    String phone;
    /**
     * 职位
     */
    String position;

    /**
     * 出生日期
     */
    String birthday;

    /**
     * 工作年限
     */
    Integer workExperience;

    /**
     * 毕业时间
     */
    String graduationTime;

    /**
     * 双层学历
     */
    Boolean doubleEducation;

    /**
     * 分组
     */
    String group;

    /**
     * 技能
     */
    String skill;
    /**
     * 经历
     */
    String experience;

    /**
     * 评价
     */
    String evaluate;

    /**
     * 出现次数
     */
    Integer occ;

    /**
     * 名字是否相同
     */
    Boolean equalNme;
    /**
     * 文件名称
     */
    String fileName;

    /**
     * 符合条件
     */
    Map<String,Boolean> effective;

    public ResumeDto() {
        this.doubleEducation = false;
        this.equalNme = false;
        this.effective = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getEvaluate() {
        return evaluate;
    }

    public void setEvaluate(String evaluate) {
        this.evaluate = evaluate;
    }

    public Integer getOcc() {
        return occ;
    }

    public void setOcc(Integer occ) {
        this.occ = occ;
    }

    public Boolean getEqualNme() {
        return equalNme;
    }

    public void setEqualNme(Boolean equalNme) {
        this.equalNme = equalNme;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGraduationTime() {
        return graduationTime;
    }

    public void setGraduationTime(String graduationTime) {
        this.graduationTime = graduationTime;
    }

    public Boolean getDoubleEducation() {
        return doubleEducation;
    }

    public void setDoubleEducation(Boolean doubleEducation) {
        this.doubleEducation = doubleEducation;
    }

    public Integer getWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(Integer workExperience) {
        this.workExperience = workExperience;
    }

    public Map<String, Boolean> getEffective() {
        return effective;
    }

    public void setEffective(Map<String, Boolean> effective) {
        this.effective = effective;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ResumeDto) {
            ResumeDto resumeDto = (ResumeDto) obj;
            if (resumeDto.name.equals(this.name)
                    && resumeDto.phone.equals(this.phone)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        return result;
    }
}

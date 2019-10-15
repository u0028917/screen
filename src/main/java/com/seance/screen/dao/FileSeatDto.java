package com.seance.screen.dao;

/**
 * @author master
 */
public class FileSeatDto {

    /**
     * 基本信息位子
     */
    private int baseInformationNum = 0;
    /**
     * 基本信息
     */
    private boolean baseInformation = false;
    /**
     * 教育背景位子
     */
    private int educationalNum = 0;
    /**
     * 教育背景
     */
    private boolean educational = false;
    /**
     * 专业技能位子
     */
    private int skillsNum = 0;
    /**
     * 专业技能
     */
    private boolean skills = false;
    /**
     * 工作经历位子
     */
    private int workExperienceNum = 0;
    /**
     * 工作经历
     */
    private boolean workExperience = false;
    /**
     * 项目经验位子
     */
    private int projectExperienceNum = 0;
    /**
     * 项目经验
     */
    private boolean projectExperience = false;

    public int getBaseInformationNum() {
        return baseInformationNum;
    }

    public void setBaseInformationNum(int baseInformationNum) {
        this.baseInformationNum = baseInformationNum;
    }

    public boolean isBaseInformation() {
        return baseInformation;
    }

    public void setBaseInformation(boolean baseInformation) {
        this.baseInformation = baseInformation;
    }

    public int getEducationalNum() {
        return educationalNum;
    }

    public void setEducationalNum(int educationalNum) {
        this.educationalNum = educationalNum;
    }

    public boolean isEducational() {
        return educational;
    }

    public void setEducational(boolean educational) {
        this.educational = educational;
    }

    public int getSkillsNum() {
        return skillsNum;
    }

    public void setSkillsNum(int skillsNum) {
        this.skillsNum = skillsNum;
    }

    public boolean isSkills() {
        return skills;
    }

    public void setSkills(boolean skills) {
        this.skills = skills;
    }

    public int getWorkExperienceNum() {
        return workExperienceNum;
    }

    public void setWorkExperienceNum(int workExperienceNum) {
        this.workExperienceNum = workExperienceNum;
    }

    public boolean isWorkExperience() {
        return workExperience;
    }

    public void setWorkExperience(boolean workExperience) {
        this.workExperience = workExperience;
    }

    public int getProjectExperienceNum() {
        return projectExperienceNum;
    }

    public void setProjectExperienceNum(int projectExperienceNum) {
        this.projectExperienceNum = projectExperienceNum;
    }

    public boolean isProjectExperience() {
        return projectExperience;
    }

    public void setProjectExperience(boolean projectExperience) {
        this.projectExperience = projectExperience;
    }
}

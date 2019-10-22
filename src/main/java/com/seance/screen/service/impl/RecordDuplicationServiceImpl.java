package com.seance.screen.service.impl;

import com.seance.screen.dao.FileSeatDto;
import com.seance.screen.dao.ResumeDto;
import com.seance.screen.service.RecordDuplicationService;
import com.seance.screen.util.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author master
 */
@Slf4j
@Service
public class RecordDuplicationServiceImpl implements RecordDuplicationService {

    ThreadLocal<Map<String, List<String>>> threadLocal = new ThreadLocal<Map<String, List<String>>>();

    private static Pattern yearP = Pattern.compile("\\d{4}");

    private static Pattern number = Pattern.compile("\\d");

    @Autowired
    private RedisService redisService;

    @Override
    public void recordWeight(String path) {
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("路径是空的");
        }
        String[] split = path.split("\\\\");
        String str = split[split.length - 1];
        int index = this.getFastChinese(str);
        String date = str.substring(0, index);
        redisService.setMap(date, takeNotes(path));
//        this.exportCsv(new File("C:/Users/master/Desktop/out.csv"), threadLocal.get());
        threadLocal.remove();
    }


    private void exportCsv(File file, Map<String, List<ResumeDto>> dataMap) {
        try (FileOutputStream out = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(out, "gbk");
             BufferedWriter bw = new BufferedWriter(osw)) {
            bw.append("分组").append(",")
                    .append("职位").append(",")
                    .append("姓名").append(",")
                    .append("电话").append(",")
                    .append("出生日期").append(",")
                    .append("年龄").append(",")
                    .append("毕业日期").append(",")
                    .append("毕业年龄").append(",")
                    .append("学历").append(",")
                    .append("工作年限").append(",")
                    .append("双层学历").append(",")
                    .append("重复次数").append(",")
                    .append("评价").append(",")
                    .append("错误简历").append(",")
                    .append("简历名称").append("\r");
            if (dataMap != null && !dataMap.isEmpty()) {
                for (Map.Entry<String, List<ResumeDto>> entry : dataMap.entrySet()) {
                    for (ResumeDto dto : entry.getValue()) {
                        int i = 0;
                        int nowAge = 0;
                        try {
                            nowAge = 2019 - Integer.parseInt(dto.getBirthday());
                            i = Integer.parseInt(dto.getGraduationTime()) - Integer.parseInt(dto.getBirthday());
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        bw.append(dto.getGroup()).append(",")
                                .append(dto.getPosition()).append(",")
                                .append(dto.getName()).append(",")
                                .append(dto.getPhone()).append(",")
                                .append(dto.getBirthday()).append(",")
                                .append(String.valueOf(nowAge)).append(",")
                                .append(dto.getGraduationTime()).append(",")
                                .append(String.valueOf(i)).append(",")
                                .append(dto.getEducation() != null ? dto.getEducation() : "").append(",")
                                .append(String.valueOf(dto.getWorkExperience())).append(",")
                                .append(dto.getDoubleEducation() ? "是" : "否").append(",")
                                .append(String.valueOf(dto.getOcc())).append(",")
                                .append(dto.getEvaluate() != null ? dto.getEvaluate() : "").append(",")
                                .append(dto.getEqualNme() ? "正确" : "错误").append(",")
                                .append(dto.getFileName()).append("\r");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map takeNotes(String path) {
        Map map = new HashMap<String, List>(6);
        File file = new File(path);
        for (File listFile : file.listFiles()) {
            List<String> list = Arrays.asList(listFile.list());
            map.put(listFile.getName(), list);
            this.setLocalMap(listFile.getName());
            for (String name : list) {
                if (redisService.setSet("goethe", name) == 0) {
                    List add = (List) threadLocal.get().get(listFile.getName());
                    add.add(name);
                }
            }
        }
        return map;
    }

    private void setLocalMap(String name) {
        Map map = threadLocal.get();
        if (map == null) {
            threadLocal.set(new HashMap<String, List<String>>());
            map = threadLocal.get();
        }
        if (!map.containsKey(name)) {
            threadLocal.get().put(name, new ArrayList());
        }
    }

    @Override
    public void main(String path, Map<String, List<String>> screenJd, Boolean openDelete) {
        threadLocal.set(screenJd);
        Map<String, List<ResumeDto>> data = this.getMessage(path, openDelete);
        this.exportCsv(new File("C:/Users/master/Desktop/out/out" + System.currentTimeMillis() + ".csv")
                , data);
        threadLocal.remove();
    }

    private void saveData(Map<String, List<ResumeDto>> data) {

    }

    /**
     * 获取简历信息
     *
     * @param path
     * @param openDelete
     * @return
     */
    private Map<String, List<ResumeDto>> getMessage(String path, Boolean openDelete) {
        Map<String, Integer> resumeDtos = new HashMap<>();
        List<ResumeDto> dtos = new ArrayList<>();
        File file = new File(path);
        for (File listFile : file.listFiles()) {
            for (File world : listFile.listFiles()) {
                ResumeDto dto = new ResumeDto();
                dto.setFileName(world.getName());
                dto.setName(getRealName(world.getName()));
                dto.setGroup(listFile.getName());
                this.getWorldContent(world.getPath(), dto);
                this.setSkillEffective(dto);
                dtos.add(dto);
                this.mapSet(resumeDtos, dto);
                if ("不符合".equals(dto.getEvaluate()) && openDelete) {
                    world.delete();
                }
            }
        }
        //设置到每个简历上简历重复的数量
        this.setResumeOcc(resumeDtos, dtos);
        return this.packageMapData(dtos);
    }

    /**
     * 设置根据简历技能判断简历是否符合要求  临时
     *
     * @param dto
     */
    private void setSkillEffective(ResumeDto dto) {
        boolean flag = false;
        for (Map.Entry<String, List<String>> entry : threadLocal.get().entrySet()) {
            if (dto.getGroup().equals(entry.getKey())) {
                flag = true;
                if (dto.getEffective().get(entry.getKey()) != null && dto.getEffective().get(entry.getKey()) != null) {
                    dto.setEvaluate("符合");
                }
            }
        }
        if (StringUtils.isEmpty(dto.getEvaluate()) && flag) {
            dto.setEvaluate("不符合");
        }
    }

    /**
     * 打包简历数据
     *
     * @param dtos
     * @return
     */
    private Map<String, List<ResumeDto>> packageMapData(List<ResumeDto> dtos) {
        Map<String, List<ResumeDto>> mapData = new HashMap<>();
        for (ResumeDto dto : dtos) {
            if (mapData.containsKey(dto.getGroup())) {
                mapData.get(dto.getGroup()).add(dto);
            } else {
                List<ResumeDto> list = new ArrayList<>();
                list.add(dto);
                mapData.put(dto.getGroup(), list);
            }
        }
        return mapData;
    }

    /**
     * 设置到每个简历上简历重复的数量
     *
     * @param resumeDtos
     * @param dtos
     */
    private void setResumeOcc(Map<String, Integer> resumeDtos, List<ResumeDto> dtos) {
        for (ResumeDto dto : dtos) {
            dto.setOcc(resumeDtos.get(dto.getName() + "-" + dto.getPhone()));
        }
    }

    /**
     * 设置简历重复数量
     *
     * @param resumeDtos
     * @param dto
     */
    private void mapSet(Map<String, Integer> resumeDtos, ResumeDto dto) {
        String key = dto.getName() + "-" + dto.getPhone();
        if (resumeDtos.containsKey(key)) {
            resumeDtos.put(key, resumeDtos.get(key) + 1);
        } else {
            resumeDtos.put(key, 1);
        }
    }

    /**
     * 区分world格式获取内容
     *
     * @param filePath
     * @param dto
     */
    private void getWorldContent(String filePath, ResumeDto dto) {
        try (InputStream is = new FileInputStream(filePath)) {
            if (filePath.endsWith(".doc")) {
                HWPFDocument doc = new HWPFDocument(is);
                Range range = doc.getRange();
                this.readDocContent(range, dto);
            } else if (filePath.endsWith(".docx")) {
                this.readDocxContent(new XWPFDocument(is), dto);
            }
        } catch (Exception e) {
            dto.setEvaluate("无效简历");
            e.printStackTrace();
        }

    }

    /**
     * 读取docx内容
     *
     * @param doc
     * @param dto
     */
    private void readDocxContent(XWPFDocument doc, ResumeDto dto) {
        doc.getParagraphs().get(0).getText();
        List<String> textList = new ArrayList<>();
        textList.add(doc.getParagraphs().get(0).getText());
        for (XWPFTableRow row : doc.getTables().get(0).getRows()) {
            textList.add(row.getTableCells().get(0).getText());
        }
        int num = textList.size();
        FileSeatDto fsDto = new FileSeatDto();
        for (int i = 0; i < num; i++) {
            this.handleText(textList.get(i), fsDto, dto, i);
        }
    }

    /**
     * 读取world内容
     *
     * @param range
     * @param dto
     */
    private void readDocContent(Range range, ResumeDto dto) {
        int num = range.numParagraphs();
        Paragraph para;
        FileSeatDto fsDto = new FileSeatDto();
        for (int i = 0; i < num; i++) {
            para = range.getParagraph(i);
            if ("\u0007".equals(para.text())) {
                continue;
            }
            this.handleText(para.text(), fsDto, dto, i);
        }
    }

    private void handleText(String paraText, FileSeatDto fsDto, ResumeDto dto, int i) {
        String text = deleteSpace(paraText);
        text = text.replaceAll("\u0007", "");
        //设置职业
        if (fsDto.getBaseInformationNum() == 0 && text.contains("推荐职位")) {
            String[] strs = text.split("推荐职位");
            String position = strs[strs.length - 1];
            dto.setPosition(position.substring(1, position.length()));
        }
        //设置基本信息
        if (fsDto.isBaseInformation() && fsDto.getEducationalNum() == 0) {
            String[] kvs = text.split("：");
            if (kvs.length > 1) {
                this.setBase(kvs, dto);
            }
        }
        //设置毕业年限
        if (fsDto.isEducational() && fsDto.getSkillsNum() == 0) {
            if (Pattern.matches(".*\\d{4}.*", text)) {
                Matcher m = yearP.matcher(text);
                String year = "";
                int n = 0;
                while (m.find()) {
                    year = m.group(0);
                    n++;
                }
                if (n > 3) {
                    dto.setDoubleEducation(true);
                }
                dto.setGraduationTime(year);
            }
            if (text.contains("本科")) {
                dto.setEducation("本科");
            } else if (text.contains("专科")) {
                dto.setEducation("专科");
            } else if (text.contains("高中")) {
                dto.setEducation("高中");
            } else if (text.contains("大专")) {
                dto.setEducation("大专");
            }
        }
        // 筛选技能用方法  临时
        this.setScreenSkill(fsDto, dto, text);
        this.setSeat(fsDto, text, i);
    }


    /**
     * 筛选技能用方法  临时
     *
     * @param fsDto
     * @param dto
     * @param text
     */
    private void setScreenSkill(FileSeatDto fsDto, ResumeDto dto, String text) {
        if (fsDto.getSkillsNum() > 0) {
            Map<String, List<String>> map = threadLocal.get();
            if (map.containsKey(dto.getGroup())) {
                for (String screen : map.get(dto.getGroup())) {
                    if (text.toLowerCase().contains(screen)) {
                        fsDto.getCoincidenceDegree().add(screen);
                    }
                }
                if (map.get(dto.getGroup()).size() == fsDto.getCoincidenceDegree().size()) {
                    dto.getEffective().put(dto.getGroup(), true);
                }
            }
        }

    }


    /**
     * 设置基础信息
     *
     * @param kvs
     * @param dto
     */
    private void setBase(String[] kvs, ResumeDto dto) {
        switch (kvs[0]) {
            case "姓名":
                if (dto.getName().equals(kvs[1])) {
                    dto.setEqualNme(true);
                } else {
                    dto.setEqualNme(false);
                }
                break;
            case "出生日期":
                dto.setBirthday(kvs[1]);
                if (Pattern.matches(".*\\d{4}.*", kvs[1])) {
                    Matcher m = yearP.matcher(kvs[1]);
                    if (m.find()) {
                        dto.setBirthday(m.group(0));
                    }
                }
                break;
            case "电话":
            case "联系方式":
                dto.setPhone(kvs[1]);
                break;
            case "工作经验":
                Integer year = 0;
                try {
                    if (Pattern.matches(".*\\d.*", kvs[1])) {
                        Matcher m = number.matcher(kvs[1]);
                        if (m.find()) {
                            year = Integer.valueOf(m.group(0));
                        }
                    }
                } catch (Exception e) {
                    log.error("工作年限转换错误");
                }
                dto.setWorkExperience(year);
                break;
            default:
                break;
        }
    }

    private void setSeat(FileSeatDto fsDto, String text, int i) {
        if (fsDto.getBaseInformationNum() == 0 && text.contains("基本信息")) {
            fsDto.setBaseInformation(true);
            fsDto.setBaseInformationNum(i);
        }
        if (fsDto.getEducationalNum() == 0 && text.contains("教育背景")) {
            fsDto.setEducational(true);
            fsDto.setEducationalNum(i);
            fsDto.setBaseInformation(false);
        }
        if (fsDto.getSkillsNum() == 0) {
            if (text.contains("专业技能") || text.contains("技能介绍")) {
                fsDto.setSkills(true);
                fsDto.setSkillsNum(i);
                fsDto.setEducational(false);
            }
        }
        if (fsDto.getWorkExperienceNum() == 0 && text.contains("工作经历")) {
            fsDto.setWorkExperience(true);
            fsDto.setWorkExperienceNum(i);
            fsDto.setSkills(false);
        }
        if (fsDto.getEducationalNum() == 0 && text.contains("项目经验")) {
            fsDto.setProjectExperience(true);
            fsDto.setProjectExperienceNum(i);
            fsDto.setWorkExperience(false);
        }
    }

    /**
     * 删除空格
     *
     * @param text text
     */
    private String deleteSpace(String text) {
        return text.replaceAll("\\s*", "");
    }

    /**
     * 获取名字
     *
     * @param name 名字
     */
    private String getRealName(String name) {
        String spcName = "";
        try {
            spcName = this.deleteSpace(name).split("-")[1];
        } catch (Exception e) {
            log.error(name + "格式错误", e);
        }
        return spcName;
    }

    @Override
    public void duplicateRemoval() {


    }

    /**
     * 找第一个汉字的位置，如果找不到返回-1
     *
     * @param str 字符串
     * @return int
     */
    private int getFastChinese(String str) {
        //找第一个汉字
        for (int index = 0; index <= str.length() - 1; index++) {
            //将字符串拆开成单个的字符
            String w = str.substring(index, index + 1);
            if (w.compareTo("\u4e00") > 0 && w.compareTo("\u9fa5") < 0) {
                return index;
            }
        }
        return -1;
    }
}

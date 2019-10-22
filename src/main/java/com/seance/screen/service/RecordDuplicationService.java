package com.seance.screen.service;

import java.util.List;
import java.util.Map;

/**
 * @author master
 */
public interface RecordDuplicationService {

    /**
     * 记录信息
     */
    void recordWeight(String path);

    /**
     * 主干方法
     */
    void main(String path, Map<String, List<String>> screenJd, Boolean openDelete);

    /**
     * 筛选简历
     */
    void duplicateRemoval();
}

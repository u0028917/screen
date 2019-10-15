package com.seance.screen.service;

import org.springframework.stereotype.Service;

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
    void main(String path);

    /**
     * 去重
     */
    void duplicateRemoval();
}

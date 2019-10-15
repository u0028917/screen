package com.seance.screen.util.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>Description: [redis服务]</p>
 * Created on 2018/8/30
 *
 * @author <a href="mailto:sunshaobo@camelotchina.com">孙少波</a>
 * @version 1.0
 */
public interface RedisService {

    public final static String DEL_NO = "dateNos:";

    /**
     * 设置 key 的值为 value 如果key不存在添加key 保存值为value 如果key存在则对value进行覆盖
     */
    public Boolean set(String key, Object value);

    /**
     * 设置 key 的值为 value 超期时间 其它规则与 set(K key, V value)一样
     *
     * @param key      不能为空
     * @param value    设置的值
     * @param timeout  设置过期的时间
     * @param timeUnit 时间单位。不能为空
     */
    public Boolean set(String key, Object value, Long timeout, TimeUnit timeUnit);

    /**
     * 根据 key 获取对应的value 如果key不存在则返回null
     *
     * @param key 不能为null
     * @return Object
     * @author:[孙少波]
     */
    public Object get(String key);

    /**
     * 删除key
     *
     * @param key 不能为null
     */
    public Boolean delete(String key);

    /**
     * 批量删除key
     *
     * @param keys 不能为null
     */
    Long delete(Collection<String> keys);

    /**
     * <p>Discription:[模糊查询根据key]</p>
     * Created on 2018/9/5
     *
     * @param key key
     * @return Set
     * @author:[孙少波]
     */
    Set<String> findByKeys(String key);

    /**
     * <p>Discription:[存储List]</p>
     * Created on 2018/9/6
     *
     * @param key  建值
     * @param list 数据
     * @author:[孙少波]
     */
    void listPut(String key, Collection<String> list);

    /**
     * <p>Discription:[list put值]</p>
     * Created on 2018/9/6
     *
     * @param key key值 value 值
     * @author:[孙少波]
     */
    void addList(String key, String value);

    /**
     * 获取lis所有值
     * @param key
     * @return
     */
    List findList(String key);

    /**
     * 获取lis范围值
     * @param key
     * @return
     */
    List findList(String key, long start, long end);

    /**
     * <p>Discription:[向右移出redis的值]</p>
     * Created on 2018/9/6
     *
     * @param key key值
     * @return Object
     * @author:[孙少波]
     */
    Object rightPop(String key);

    /**
     * <p>Discription:[获取list数量]</p>
     * Created on 2018/9/6
     *
     * @param key key值
     * @return Long 数量
     * @author:[孙少波]
     */
    Long getListSize(String key);

    /**
     * <p>Discription:[根据key获取自增ID]</p>
     * Created on 2018/9/19
     *
     * @param key key值
     * @return Long id值
     * @author:[孙少波]
     */
    Long getKeyId(String key);

    /**
     * <p>Discription:[根据key获取自增ID]</p>
     * Created on 2018/9/19
     *
     * @param key key值
     * @param num id需要自增的数量
     * @return Long id值
     * @author:[孙少波]
     */
    Long getKeyId(String key, long num);

    /**
     * <p>Discription:[设置Key超时时间]</p>
     * Created on 2018/10/31
     *
     * @param key      key值
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     * @return Boolean 成功失败
     * @author:[孙少波]
     */
    Boolean setExpIre(String key, Long timeout, TimeUnit timeUnit);

    /**
     * <p>Discription:[存储map]</p>
     * Created on 2018/10/31
     *
     * @param key key值
     * @param map hashMap对象
     * @author:[孙少波]
     */
    void setMap(String key, Map map);

    /**
     * <p>Discription:[map存储]</p>
     * Created on 2018/10/31
     *
     * @param key     key值
     * @param hashKey map的key值
     * @param value   map的value值
     * @author:[孙少波]
     */
    void mapPull(String key, Object hashKey, Object value);

    /**
     * <p>Discription:[map存储允许map空]</p>
     * Created on 2018/10/31
     *
     * @param key     key值
     * @param hashKey map的key值
     * @param value   map的value值
     * @author:[孙少波]
     */
    void mapPullIfNull(String key, Object hashKey, Object value);

    /**
     * <p>Discription:[map取值]</p>
     * Created on 2018/10/31
     *
     * @param key     key值
     * @param hashKey map的key值
     * @return Object map的value值
     * @author:[孙少波]
     */
    Object mapGet(String key, Object hashKey);

    /**
     * <p>Discription:[获取map]</p>
     * Created on 2018/10/31
     *
     * @param key key值
     * @return Map
     * @author:[孙少波]
     */
    Map getMap(String key);

    /**
     * <p>Discription:[获取list中的所有值]</p>
     * Created on 2018/10/31
     *
     * @param key key值
     * @return Map
     * @author:[孙少波]
     */
    List getAllList(String key);

    /**
     * <p>Discription:[获取list中的所有值]</p>
     * Created on 2019/10/9
     *
     * @param key key值
     * @return Map
     * @author:[孙少波]
     */
    Long setSet(String key,Object value);

}

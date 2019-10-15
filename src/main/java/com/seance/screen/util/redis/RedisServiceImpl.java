package com.seance.screen.util.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>Description: [redis服务实现类]</p>
 * Created on 2018/8/30
 *
 * @author <a href="mailto:sunshaobo@camelotchina.com">孙少波</a>
 * @version 1.0
 */
public class RedisServiceImpl implements RedisService {

    private RedisTemplate redisTemplate;

    RedisServiceImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 设置 key 的值为 value 如果key不存在添加key 保存值为value 如果key存在则对value进行覆盖
     */
    @Override
    public Boolean set(String key, Object value) {
        return redisTemplate.opsForValue().setIfAbsent(key, value);
    }

    /**
     * 设置 key 的值为 value 超期时间 其它规则与 set(K key, V value)一样
     *
     * @param key      不能为空
     * @param value    设置的值
     * @param timeout  设置过期的时间
     * @param timeUnit 时间单位。不能为空
     */
    @Override
    public Boolean set(String key, Object value, Long timeout, TimeUnit timeUnit) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout, timeUnit);
    }

    /**
     * 根据 key 获取对应的value 如果key不存在则返回null
     *
     * @param key 不能为null
     */
    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除key
     *
     * @param key 不能为null
     */
    @Override
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除key
     *
     * @param keys 不能为null
     */
    @Override
    public Long delete(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    /**
     * <p>Discription:[模糊查询根据key]</p>
     * Created on 2018/9/5
     *
     * @param key 模糊查询的key
     * @return Set
     * @author:[孙少波]
     */
    @Override
    public Set<String> findByKeys(String key) {
        return redisTemplate.keys(key);
    }


    /**
     * <p>Discription:[存储List]</p>
     * Created on 2018/9/6
     *
     * @param key  建值
     * @param list 数据
     * @author:[孙少波]
     */
    @Override
    public void listPut(String key, Collection<String> list) {
        redisTemplate.opsForList().rightPushAll(key, list);
    }

    /**
     * <p>Discription:[list put值]</p>
     * Created on 2018/9/6
     *
     * @param key key值 value 值
     * @author:[孙少波]
     */
    @Override
    public void addList(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public List findList(String key) {
        Long size = this.getListSize(key);
        if (size != null) {
            return this.findList(key, 0, this.getListSize(key));
        } else {
            return null;
        }
    }

    @Override
    public List findList(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }


    /**
     * <p>Discription:[向右移出redis的值]</p>
     * Created on 2018/9/6
     *
     * @param key key值
     * @return String
     * @author:[孙少波]
     */
    @Override
    public Object rightPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * <p>Discription:[获取list数量]</p>
     * Created on 2018/9/6
     *
     * @param key key值
     * @return Long 数量
     * @author:[孙少波]
     */
    @Override
    public Long getListSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * <p>Discription:[根据key获取自增ID]</p>
     * Created on 2018/9/19
     *
     * @param key key值
     * @return Long id值
     * @author:[孙少波]
     */
    @Override
    public Long getKeyId(String key) {
        return this.getKeyId(key, 1);
    }

    /**
     * <p>Discription:[根据key获取自增ID]</p>
     * Created on 2018/9/19
     *
     * @param key key值
     * @param num id需要自增的数量
     * @return Long id值
     * @author:[孙少波]
     */
    @Override
    public Long getKeyId(String key, long num) {
        return redisTemplate.opsForValue().increment(key, num);
    }

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
    @Override
    public Boolean setExpIre(String key, Long timeout, TimeUnit timeUnit) {
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * <p>Discription:[存储map]</p>
     * Created on 2018/10/31
     *
     * @param key key值
     * @param map hashMap对象
     * @author:[孙少波]
     */
    @Override
    public void setMap(String key, Map map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * <p>Discription:[map存储]</p>
     * Created on 2018/10/31
     *
     * @param key     key值
     * @param hashKey map的key值
     * @param value   map的value值
     * @author:[孙少波]
     */
    @Override
    public void mapPull(String key, Object hashKey, Object value) {
        if (redisTemplate.opsForHash().entries(key) != null
                && redisTemplate.opsForHash().entries(key).size() > 0) {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } else {
            throw new IllegalArgumentException("map不存在");
        }
    }

    /**
     * <p>Discription:[map存储允许map空]</p>
     * Created on 2018/10/31
     *
     * @param key     key值
     * @param hashKey map的key值
     * @param value   map的value值
     * @author:[孙少波]
     */
    @Override
    public void mapPullIfNull(String key, Object hashKey, Object value) {
        if (redisTemplate.opsForHash().entries(key) != null
                && redisTemplate.opsForHash().entries(key).size() > 0) {
            redisTemplate.opsForHash().put(key, hashKey, value);
        } else {
            Map map = new HashMap();
            map.put(hashKey, value);
            redisTemplate.opsForHash().putAll(key, map);
        }
    }


    /**
     * <p>Discription:[map取值]</p>
     * Created on 2018/10/31
     *
     * @param key     key值
     * @param hashKey map的key值
     * @return Object map的value值
     * @author:[孙少波]
     */
    @Override
    public Object mapGet(String key, Object hashKey) {
        if (redisTemplate.opsForHash().entries(key) != null
                && redisTemplate.opsForHash().entries(key).size() > 0) {
            return redisTemplate.opsForHash().get(key, hashKey);
        } else {
            throw new IllegalArgumentException("map不存在");
        }
    }

    /**
     * <p>Discription:[获取map]</p>
     * Created on 2018/10/31
     *
     * @param key key值
     * @return Map
     * @author:[孙少波]
     */
    @Override
    public Map getMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * <p>Discription:[获取list中的所有值]</p>
     * Created on 2018/10/31
     *
     * @param key key值
     * @return Map
     * @author:[孙少波]
     */
    @Override
    public List getAllList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * <p>Discription:[获取list中的所有值]</p>
     * Created on 2019/10/9
     *
     * @param key key值
     * @return Map
     * @author:[孙少波]
     */
    @Override
    public Long setSet(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }


}

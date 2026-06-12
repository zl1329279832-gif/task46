package com.service.impl;

import com.utils.StringUtil;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.dao.DaozhanRemindDao;
import com.entity.DaozhanRemindEntity;
import com.service.DaozhanRemindService;
import com.entity.view.DaozhanRemindView;

/**
 * 到站提醒订阅 服务实现类
 */
@Service("daozhanRemindService")
@Transactional
public class DaozhanRemindServiceImpl extends ServiceImpl<DaozhanRemindDao, DaozhanRemindEntity> implements DaozhanRemindService {

    @Override
    public PageUtils queryPage(Map<String,Object> params) {
        if(params != null && (params.get("limit") == null || params.get("page") == null)){
            params.put("page","1");
            params.put("limit","10");
        }
        Page<DaozhanRemindView> page =new Query<DaozhanRemindView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page,params));
        return new PageUtils(page);
    }

    /**
     * 订阅到站提醒
     * 处理场景：同一用户重复订阅
     */
    @Override
    public String subscribe(Integer yonghuId, Integer gongjiaoxianluId, String stopName) {
        // 参数校验
        if(yonghuId == null){
            return "用户ID不能为空";
        }
        if(gongjiaoxianluId == null){
            return "线路ID不能为空";
        }
        if(stopName == null || stopName.trim().isEmpty()){
            return "站点名称不能为空";
        }

        String normalized = normalizeStopName(stopName);

        // 检查是否已存在相同订阅（同一用户、同一线路、同一站点、未触发的）
        // 取出该线路该用户的所有未触发订阅，用归一化名称比对，防止破折号/空格差异导致重复订阅
        Wrapper<DaozhanRemindEntity> queryWrapper = new EntityWrapper<DaozhanRemindEntity>()
            .eq("yonghu_id", yonghuId)
            .eq("gongjiaoxianlu_id", gongjiaoxianluId)
            .eq("remind_status", 0);

        List<DaozhanRemindEntity> existingList = this.selectList(queryWrapper);
        for(DaozhanRemindEntity ex : existingList){
            if(normalizeStopName(ex.getStopName()).equals(normalized)){
                return "您已订阅该线路该站点的到站提醒，无需重复订阅";
            }
        }

        // 创建新订阅（存储归一化后的站名，保证后续触发匹配一致）
        DaozhanRemindEntity entity = new DaozhanRemindEntity();
        entity.setYonghuId(yonghuId);
        entity.setGongjiaoxianluId(gongjiaoxianluId);
        entity.setStopName(normalized);
        entity.setRemindStatus(0);
        entity.setCreateTime(new Date());
        this.insert(entity);
        return null; // null表示成功
    }

    /**
     * 取消订阅
     * 处理场景：用户只能取消自己的订阅（防越权）
     */
    @Override
    public boolean cancel(Integer id, Integer yonghuId) {
        if(id == null){
            return false;
        }
        DaozhanRemindEntity entity = this.selectById(id);
        if(entity == null){
            return false;
        }
        // 验证是否为当前用户的订阅
        if(!entity.getYonghuId().equals(yonghuId)){
            return false;
        }
        // 删除订阅记录
        this.deleteById(id);
        return true;
    }

    /**
     * 查询用户的所有订阅
     */
    @Override
    public List<DaozhanRemindEntity> listByYonghu(Integer yonghuId) {
        Wrapper<DaozhanRemindEntity> queryWrapper = new EntityWrapper<DaozhanRemindEntity>()
            .eq("yonghu_id", yonghuId)
            .orderBy("create_time", false);
        return this.selectList(queryWrapper);
    }

    /**
     * 检查并触发到站提醒
     * 处理场景：管理员更新车辆位置后提醒状态变化
     *
     * 当车辆位置的下一站名称与用户订阅的站点名称匹配时，触发提醒
     */
    @Override
    public int checkAndTriggerReminders(Integer gongjiaoxianluId, String stopName) {
        if(gongjiaoxianluId == null || stopName == null || stopName.trim().isEmpty()){
            return 0;
        }

        // 查找该线路所有未触发订阅，用归一化站名匹配（避免破折号/空格差异导致提醒不触发）
        String normalizedStop = normalizeStopName(stopName);
        Wrapper<DaozhanRemindEntity> queryWrapper = new EntityWrapper<DaozhanRemindEntity>()
            .eq("gongjiaoxianlu_id", gongjiaoxianluId)
            .eq("remind_status", 0);

        List<DaozhanRemindEntity> subscriptions = this.selectList(queryWrapper);
        int triggeredCount = 0;

        for(DaozhanRemindEntity sub : subscriptions){
            if(normalizeStopName(sub.getStopName()).equals(normalizedStop)){
                sub.setRemindStatus(1);
                sub.setTriggerTime(new Date());
                this.updateById(sub);
                triggeredCount++;
            }
        }

        return triggeredCount;
    }

    /**
     * 查询当前用户被触发的提醒
     */
    @Override
    public List<DaozhanRemindEntity> listAlerts(Integer yonghuId) {
        Wrapper<DaozhanRemindEntity> queryWrapper = new EntityWrapper<DaozhanRemindEntity>()
            .eq("yonghu_id", yonghuId)
            .eq("remind_status", 1)
            .orderBy("trigger_time", false);
        return this.selectList(queryWrapper);
    }

    /**
     * 归一化站点名称，消除破折号类型、空格等差异
     * 例如 "大学城 — 北门" 和 "大学城-北门" 归一化后相同
     */
    private String normalizeStopName(String name) {
        if(name == null) return "";
        // 各类破折号/连字符统一为标准半角连字符
        String normalized = name.replaceAll("[—–－‐‑⁃﹣\\u2014\\u2013\\uFF0D]", "-");
        // 去除连字符两侧空格
        normalized = normalized.replaceAll("\\s*-\\s*", "-");
        // 合并连续空白
        normalized = normalized.replaceAll("\\s+", " ");
        return normalized.trim();
    }

}

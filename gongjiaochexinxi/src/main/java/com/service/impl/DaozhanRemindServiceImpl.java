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

        // 检查是否已存在相同订阅（同一用户、同一线路、同一站点、未触发的）
        Wrapper<DaozhanRemindEntity> queryWrapper = new EntityWrapper<DaozhanRemindEntity>()
            .eq("yonghu_id", yonghuId)
            .eq("gongjiaoxianlu_id", gongjiaoxianluId)
            .eq("stop_name", stopName.trim())
            .eq("remind_status", 0);

        DaozhanRemindEntity existing = this.selectOne(queryWrapper);
        if(existing != null){
            return "您已订阅该线路该站点的到站提醒，无需重复订阅";
        }

        // 创建新订阅
        DaozhanRemindEntity entity = new DaozhanRemindEntity();
        entity.setYonghuId(yonghuId);
        entity.setGongjiaoxianluId(gongjiaoxianluId);
        entity.setStopName(stopName.trim());
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

        // 查找该线路所有未触发订阅（不在此处过滤站名，改用Java层模糊匹配）
        // 司机报站名与用户订阅名可能有差异（如"大学城-北门" vs "北门"），精确匹配会漏掉
        Wrapper<DaozhanRemindEntity> queryWrapper = new EntityWrapper<DaozhanRemindEntity>()
            .eq("gongjiaoxianlu_id", gongjiaoxianluId)
            .eq("remind_status", 0);

        List<DaozhanRemindEntity> subscriptions = this.selectList(queryWrapper);
        int triggeredCount = 0;
        String trimmedStopName = stopName.trim();

        for(DaozhanRemindEntity sub : subscriptions){
            String subStopName = sub.getStopName();
            if(subStopName == null){
                continue;
            }
            // 模糊匹配：精确匹配 或 双向子串包含
            if(subStopName.equals(trimmedStopName)
                    || subStopName.contains(trimmedStopName)
                    || trimmedStopName.contains(subStopName)){
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

}

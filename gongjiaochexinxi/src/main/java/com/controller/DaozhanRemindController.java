
package com.controller;

import java.util.*;
import com.alibaba.fastjson.JSONObject;
import javax.servlet.http.HttpServletRequest;
import com.service.TokenService;
import com.utils.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.alibaba.fastjson.*;

/**
 * 到站提醒订阅
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/daozhanRemind")
public class DaozhanRemindController {
    private static final Logger logger = LoggerFactory.getLogger(DaozhanRemindController.class);

    @Autowired
    private DaozhanRemindService daozhanRemindService;

    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service
    @Autowired
    private GongjiaoxianluService gongjiaoxianluService;

    @Autowired
    private YonghuService yonghuService;


    /**
     * 后端列表（管理员用）
     */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = daozhanRemindService.queryPage(params);

        //字典表数据转换
        List<DaozhanRemindView> list =(List<DaozhanRemindView>)page.getList();
        for(DaozhanRemindView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
     * 后端详情
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        DaozhanRemindEntity daozhanRemind = daozhanRemindService.selectById(id);
        if(daozhanRemind !=null){
            //entity转view
            DaozhanRemindView view = new DaozhanRemindView();
            BeanUtils.copyProperties( daozhanRemind , view );

            //级联表-线路
            if(daozhanRemind.getGongjiaoxianluId() != null){
                GongjiaoxianluEntity gongjiaoxianlu = gongjiaoxianluService.selectById(daozhanRemind.getGongjiaoxianluId());
                if(gongjiaoxianlu != null){
                    view.setGongjiaoxianluName(gongjiaoxianlu.getGongjiaoxianluName());
                }
            }
            //级联表-用户
            if(daozhanRemind.getYonghuId() != null){
                YonghuEntity yonghu = yonghuService.selectById(daozhanRemind.getYonghuId());
                if(yonghu != null){
                    view.setYonghuName(yonghu.getYonghuName());
                    view.setYonghuPhone(yonghu.getYonghuPhone());
                }
            }

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }
    }


    /**
     * 订阅到站提醒
     * 普通用户接口：用户订阅某条线路某一站点的到站提醒
     *
     * 处理场景：
     * - 同一用户重复订阅：返回提示信息，不创建重复记录
     * - 线路不存在：返回错误
     * - 参数缺失：返回错误
     */
    @RequestMapping("/subscribe")
    public R subscribe(@RequestBody Map<String, Object> params, HttpServletRequest request){
        logger.debug("subscribe方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 获取当前登录用户ID
        Integer yonghuId = (Integer) request.getSession().getAttribute("userId");
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(yonghuId == null || !"用户".equals(role)){
            return R.error(511,"请先以用户身份登录");
        }

        // 获取参数
        Integer gongjiaoxianluId = null;
        String stopName = null;
        try {
            if(params.get("gongjiaoxianluId") != null){
                gongjiaoxianluId = Integer.valueOf(String.valueOf(params.get("gongjiaoxianluId")));
            }
            if(params.get("stopName") != null){
                stopName = String.valueOf(params.get("stopName"));
            }
        } catch (Exception e){
            return R.error(511,"参数格式错误");
        }

        // 参数校验
        if(gongjiaoxianluId == null){
            return R.error(511,"线路ID不能为空");
        }
        if(stopName == null || stopName.trim().isEmpty()){
            return R.error(511,"站点名称不能为空");
        }

        // 校验线路是否存在
        GongjiaoxianluEntity route = gongjiaoxianluService.selectById(gongjiaoxianluId);
        if(route == null){
            return R.error(511,"线路不存在或已被删除");
        }

        // 调用service层处理订阅（内含重复订阅检查）
        String errorMsg = daozhanRemindService.subscribe(yonghuId, gongjiaoxianluId, stopName);
        if(errorMsg != null){
            return R.error(511, errorMsg);
        }

        return R.ok("订阅成功");
    }


    /**
     * 取消订阅
     * 普通用户接口：用户取消自己的到站提醒订阅
     *
     * 处理场景：
     * - 用户只能取消自己的订阅（防越权）
     * - 订阅不存在：返回错误
     */
    @RequestMapping("/cancel")
    public R cancel(@RequestBody Map<String, Object> params, HttpServletRequest request){
        logger.debug("cancel方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 获取当前登录用户ID
        Integer yonghuId = (Integer) request.getSession().getAttribute("userId");
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(yonghuId == null || !"用户".equals(role)){
            return R.error(511,"请先以用户身份登录");
        }

        // 获取订阅ID
        Integer id = null;
        try {
            if(params.get("id") != null){
                id = Integer.valueOf(String.valueOf(params.get("id")));
            }
        } catch (Exception e){
            return R.error(511,"参数格式错误");
        }

        if(id == null){
            return R.error(511,"订阅ID不能为空");
        }

        // 调用service层处理取消（内含越权检查）
        boolean success = daozhanRemindService.cancel(id, yonghuId);
        if(!success){
            return R.error(511,"取消失败，订阅不存在或无权操作");
        }

        return R.ok("取消订阅成功");
    }


    /**
     * 查询当前用户的所有订阅
     * 普通用户接口
     */
    @RequestMapping("/mySubscriptions")
    public R mySubscriptions(HttpServletRequest request){
        logger.debug("mySubscriptions方法:,,Controller:{}",this.getClass().getName());

        // 获取当前登录用户ID
        Integer yonghuId = (Integer) request.getSession().getAttribute("userId");
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(yonghuId == null || !"用户".equals(role)){
            return R.error(511,"请先以用户身份登录");
        }

        List<DaozhanRemindEntity> subscriptions = daozhanRemindService.listByYonghu(yonghuId);

        // 补充线路名称信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for(DaozhanRemindEntity sub : subscriptions){
            Map<String, Object> item = new HashMap<>();
            item.put("id", sub.getId());
            item.put("gongjiaoxianluId", sub.getGongjiaoxianluId());
            item.put("stopName", sub.getStopName());
            item.put("remindStatus", sub.getRemindStatus());
            item.put("createTime", sub.getCreateTime());
            item.put("triggerTime", sub.getTriggerTime());

            // 查询线路名称
            if(sub.getGongjiaoxianluId() != null){
                GongjiaoxianluEntity route = gongjiaoxianluService.selectById(sub.getGongjiaoxianluId());
                if(route != null){
                    item.put("gongjiaoxianluName", route.getGongjiaoxianluName());
                } else {
                    item.put("gongjiaoxianluName", "线路已删除");
                }
            }

            resultList.add(item);
        }

        return R.ok().put("data", resultList);
    }


    /**
     * 查询当前用户已触发的到站提醒
     * 普通用户接口
     */
    @RequestMapping("/myAlerts")
    public R myAlerts(HttpServletRequest request){
        logger.debug("myAlerts方法:,,Controller:{}",this.getClass().getName());

        // 获取当前登录用户ID
        Integer yonghuId = (Integer) request.getSession().getAttribute("userId");
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(yonghuId == null || !"用户".equals(role)){
            return R.error(511,"请先以用户身份登录");
        }

        List<DaozhanRemindEntity> alerts = daozhanRemindService.listAlerts(yonghuId);

        // 补充线路名称信息
        List<Map<String, Object>> resultList = new ArrayList<>();
        for(DaozhanRemindEntity alert : alerts){
            Map<String, Object> item = new HashMap<>();
            item.put("id", alert.getId());
            item.put("gongjiaoxianluId", alert.getGongjiaoxianluId());
            item.put("stopName", alert.getStopName());
            item.put("remindStatus", alert.getRemindStatus());
            item.put("triggerTime", alert.getTriggerTime());

            // 查询线路名称
            if(alert.getGongjiaoxianluId() != null){
                GongjiaoxianluEntity route = gongjiaoxianluService.selectById(alert.getGongjiaoxianluId());
                if(route != null){
                    item.put("gongjiaoxianluName", route.getGongjiaoxianluName());
                } else {
                    item.put("gongjiaoxianluName", "线路已删除");
                }
            }

            resultList.add(item);
        }

        return R.ok().put("data", resultList);
    }


    /**
     * 删除（管理员用）
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        daozhanRemindService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }


}

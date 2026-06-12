
package com.controller;

import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import com.service.TokenService;
import com.utils.*;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 乘客实时查车与到站提醒
 * 后端接口
 * @author
 * @email
 */
@RestController
@Controller
@RequestMapping("/bus/realtime")
public class BusRealtimeController {
    private static final Logger logger = LoggerFactory.getLogger(BusRealtimeController.class);

    /** 车辆位置超过此分钟数视为过期 */
    private static final int POSITION_EXPIRE_MINUTES = 30;

    @Autowired
    private CheliangweizhiService cheliangweizhiService;

    @Autowired
    private GongjiaoxianluService gongjiaoxianluService;

    @Autowired
    private GongjiaocheService gongjiaocheService;

    @Autowired
    private DaozhandingyueService daozhandingyueService;

    @Autowired
    private YonghuService yonghuService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private DictionaryService dictionaryService;


    /**
     * 按公交线路查询当前车辆实时位置列表
     * 返回该线路上所有车辆的大体位置、行驶方向、下一站名称、更新时间、预计到站时间
     * 自动过滤掉位置信息过期的车辆
     */
    @RequestMapping("/positions")
    public R getPositionsByRoute(@RequestParam Integer gongjiaoxianluId, HttpServletRequest request) {
        logger.debug("getPositionsByRoute方法:,,gongjiaoxianluId:{}", gongjiaoxianluId);

        if (gongjiaoxianluId == null) {
            return R.error(511, "线路ID不能为空");
        }

        // 查询线路是否存在
        GongjiaoxianluEntity xianlu = gongjiaoxianluService.selectById(gongjiaoxianluId);
        if (xianlu == null) {
            return R.error(511, "该公交线路不存在");
        }

        // 查询该线路下所有车辆位置
        Wrapper<CheliangweizhiEntity> wrapper = new EntityWrapper<CheliangweizhiEntity>()
                .eq("gongjiaoxianlu_id", gongjiaoxianluId);
        List<CheliangweizhiEntity> positionList = cheliangweizhiService.selectList(wrapper);

        if (positionList == null || positionList.isEmpty()) {
            return R.ok().put("data", new ArrayList<>()).put("msg", "当前该线路暂无车辆位置信息");
        }

        // 过滤过期的位置信息 & 组装返回数据
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, -POSITION_EXPIRE_MINUTES);
        Date expireTime = cal.getTime();

        List<Map<String, Object>> resultList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (CheliangweizhiEntity pos : positionList) {
            // 过滤位置过期的车辆
            if (pos.getCreateTime() != null && pos.getCreateTime().before(expireTime)) {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("id", pos.getId());
            item.put("cheliangweizhiDati", pos.getCheliangweizhiDati());
            item.put("cheliangweizhiFangxiang", pos.getCheliangweizhiFangxiang());
            item.put("cheliangweizhiMingcheng", pos.getCheliangweizhiMingcheng());
            item.put("updateTime", pos.getCreateTime() != null ? sdf.format(pos.getCreateTime()) : "");

            // 查询关联的车辆信息
            if (pos.getGongjiaocheId() != null) {
                GongjiaocheEntity che = gongjiaocheService.selectById(pos.getGongjiaocheId());
                if (che != null) {
                    item.put("gongjiaocheName", che.getGongjiaocheName());
                    item.put("gongjiaocheId", che.getId());
                }
            }

            // 预计到站时间: 根据位置更新时间估算（简单算法：按更新时间加上固定间隔）
            if (pos.getCreateTime() != null) {
                long elapsedMinutes = (now.getTime() - pos.getCreateTime().getTime()) / (1000 * 60);
                long estimateMinutes = Math.max(1, 5 - elapsedMinutes);
                item.put("yujiDaozhanMinutes", estimateMinutes);
                item.put("yujiDaozhanText", "预计" + estimateMinutes + "分钟后到达下一站");
            } else {
                item.put("yujiDaozhanMinutes", -1);
                item.put("yujiDaozhanText", "暂无预计到站信息");
            }

            resultList.add(item);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("gongjiaoxianluName", xianlu.getGongjiaoxianluName());
        data.put("gongjiaoxianluId", xianlu.getId());
        data.put("positions", resultList);
        data.put("totalActive", resultList.size());

        return R.ok().put("data", data);
    }


    /**
     * 用户订阅到站提醒
     * 同一用户对同一线路同一站点只能有一个活跃订阅
     */
    @RequestMapping("/subscribe")
    public R subscribe(@RequestBody DaozhandingyueEntity daozhandingyue, HttpServletRequest request) {
        logger.debug("subscribe方法:,,daozhandingyue:{}", daozhandingyue.toString());

        // 验证用户身份
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if (!"用户".equals(role)) {
            return R.error(511, "只有普通用户可以订阅到站提醒");
        }

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return R.error(401, "请先登录");
        }

        // 参数校验
        if (daozhandingyue.getGongjiaoxianluId() == null) {
            return R.error(511, "线路ID不能为空");
        }
        if (StringUtils.isBlank(daozhandingyue.getZhandianName())) {
            return R.error(511, "站点名称不能为空");
        }

        // 验证线路是否存在
        GongjiaoxianluEntity xianlu = gongjiaoxianluService.selectById(daozhandingyue.getGongjiaoxianluId());
        if (xianlu == null) {
            return R.error(511, "该公交线路不存在");
        }

        // 验证线路详情中是否包含该站点
        String content = xianlu.getGongjiaoxianluContent();
        if (StringUtils.isNotBlank(content)) {
            if (!content.contains(daozhandingyue.getZhandianName().trim())) {
                return R.error(511, "该线路中未找到站点[" + daozhandingyue.getZhandianName() + "]，请确认站点名称");
            }
        }

        // 检查是否有重复的活跃订阅（同一用户、同一线路、同一站点、状态为订阅中）
        Wrapper<DaozhandingyueEntity> duplicateWrapper = new EntityWrapper<DaozhandingyueEntity>()
                .eq("yonghu_id", userId)
                .eq("gongjiaoxianlu_id", daozhandingyue.getGongjiaoxianluId())
                .eq("zhandian_name", daozhandingyue.getZhandianName().trim())
                .eq("dingyue_status", 1);
        DaozhandingyueEntity existSub = daozhandingyueService.selectOne(duplicateWrapper);
        if (existSub != null) {
            return R.error(511, "您已订阅了该线路该站点的到站提醒，请勿重复订阅");
        }

        // 创建订阅
        daozhandingyue.setYonghuId(userId);
        daozhandingyue.setDingyueStatus(1);
        daozhandingyue.setTixingStatus(0);
        daozhandingyue.setZhandianName(daozhandingyue.getZhandianName().trim());
        daozhandingyue.setCreateTime(new Date());
        daozhandingyue.setUpdateTime(new Date());
        daozhandingyueService.insert(daozhandingyue);

        return R.ok().put("data", daozhandingyue.getId()).put("msg", "订阅成功，当车辆即将到达[" + daozhandingyue.getZhandianName() + "]站时将会通知您");
    }


    /**
     * 用户取消订阅到站提醒
     */
    @RequestMapping("/unsubscribe")
    public R unsubscribe(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        logger.debug("unsubscribe方法:,,params:{}", JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if (!"用户".equals(role)) {
            return R.error(511, "只有普通用户可以取消订阅");
        }

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return R.error(401, "请先登录");
        }

        Integer dingyueId = null;
        if (params.get("id") != null) {
            dingyueId = Integer.valueOf(params.get("id").toString());
        }
        if (dingyueId == null) {
            return R.error(511, "订阅ID不能为空");
        }

        // 查询订阅记录
        DaozhandingyueEntity sub = daozhandingyueService.selectById(dingyueId);
        if (sub == null) {
            return R.error(511, "订阅记录不存在");
        }

        // 验证是否是本人的订阅
        if (!userId.equals(sub.getYonghuId())) {
            return R.error(511, "只能取消自己的订阅");
        }

        // 验证是否可以取消（只有订阅中的才能取消）
        if (sub.getDingyueStatus() != 1) {
            return R.error(511, "该订阅已不处于订阅中状态，无法取消");
        }

        // 更新为已取消
        sub.setDingyueStatus(3);
        sub.setUpdateTime(new Date());
        daozhandingyueService.updateById(sub);

        return R.ok().put("msg", "取消订阅成功");
    }


    /**
     * 查询当前用户的订阅列表
     */
    @RequestMapping("/subscriptions")
    public R mySubscriptions(@RequestParam Map<String, Object> params, HttpServletRequest request) {
        logger.debug("mySubscriptions方法:,,params:{}", JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if (!"用户".equals(role)) {
            return R.error(511, "只有普通用户可以查看订阅");
        }

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return R.error(401, "请先登录");
        }

        // 设置只查询当前用户的订阅
        params.put("yonghuId", userId);
        if (params.get("orderBy") == null || params.get("orderBy") == "") {
            params.put("orderBy", "id");
        }

        PageUtils page = daozhandingyueService.queryPage(params);

        // 字典表数据转换
        List<DaozhandingyueView> list = (List<DaozhandingyueView>) page.getList();
        for (DaozhandingyueView c : list) {
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }


    /**
     * 查询用户未读的到站提醒（已触发但可能未查看的）
     */
    @RequestMapping("/notifications")
    public R getNotifications(HttpServletRequest request) {
        logger.debug("getNotifications方法");

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if (!"用户".equals(role)) {
            return R.error(511, "只有普通用户可以查看提醒");
        }

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return R.error(401, "请先登录");
        }

        // 查询该用户已触发的提醒（dingyue_status=2, tixing_status=1）
        Wrapper<DaozhandingyueEntity> wrapper = new EntityWrapper<DaozhandingyueEntity>()
                .eq("yonghu_id", userId)
                .eq("dingyue_status", 2)
                .eq("tixing_status", 1);
        List<DaozhandingyueEntity> notifications = daozhandingyueService.selectList(wrapper);

        List<Map<String, Object>> resultList = new ArrayList<>();
        if (notifications != null) {
            for (DaozhandingyueEntity n : notifications) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", n.getId());
                item.put("zhandianName", n.getZhandianName());
                item.put("tixingContent", n.getTixingContent());
                item.put("updateTime", n.getUpdateTime());

                // 获取线路名称
                if (n.getGongjiaoxianluId() != null) {
                    GongjiaoxianluEntity xianlu = gongjiaoxianluService.selectById(n.getGongjiaoxianluId());
                    if (xianlu != null) {
                        item.put("gongjiaoxianluName", xianlu.getGongjiaoxianluName());
                    }
                }
                resultList.add(item);
            }
        }

        return R.ok().put("data", resultList);
    }


    /**
     * 用户确认已读提醒
     */
    @RequestMapping("/notifications/read")
    public R readNotification(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        logger.debug("readNotification方法:,,params:{}", JSONObject.toJSONString(params));

        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return R.error(401, "请先登录");
        }

        Integer dingyueId = null;
        if (params.get("id") != null) {
            dingyueId = Integer.valueOf(params.get("id").toString());
        }
        if (dingyueId == null) {
            return R.error(511, "订阅ID不能为空");
        }

        DaozhandingyueEntity sub = daozhandingyueService.selectById(dingyueId);
        if (sub == null) {
            return R.error(511, "记录不存在");
        }
        if (!userId.equals(sub.getYonghuId())) {
            return R.error(511, "只能操作自己的提醒");
        }

        // 标记为已读（重置提醒状态,保留已触发状态）
        sub.setTixingStatus(0);
        sub.setUpdateTime(new Date());
        daozhandingyueService.updateById(sub);

        return R.ok().put("msg", "已确认");
    }

}

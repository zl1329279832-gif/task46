package com.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 实时公交位置信息 VO
 * 用于返回给前端的车辆实时位置数据
 */
public class BusLocationVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车辆位置记录ID
     */
    private Integer cheliangweizhiId;

    /**
     * 车辆ID
     */
    private Integer gongjiaocheId;

    /**
     * 车辆编号
     */
    private String gongjiaocheName;

    /**
     * 大体位置
     */
    private String cheliangweizhiDati;

    /**
     * 行驶方向
     */
    private String cheliangweizhiFangxiang;

    /**
     * 下一站名称
     */
    private String cheliangweizhiMingcheng;

    /**
     * 位置更新时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat
    private Date updateTime;

    /**
     * 预计到站时间（分钟）
     */
    private Integer estimatedMinutes;

    /**
     * 位置是否过期
     */
    private Boolean expired;

    /**
     * 所属线路ID
     */
    private Integer gongjiaoxianluId;

    /**
     * 所属线路名称
     */
    private String gongjiaoxianluName;

    public Integer getCheliangweizhiId() {
        return cheliangweizhiId;
    }

    public void setCheliangweizhiId(Integer cheliangweizhiId) {
        this.cheliangweizhiId = cheliangweizhiId;
    }

    public Integer getGongjiaocheId() {
        return gongjiaocheId;
    }

    public void setGongjiaocheId(Integer gongjiaocheId) {
        this.gongjiaocheId = gongjiaocheId;
    }

    public String getGongjiaocheName() {
        return gongjiaocheName;
    }

    public void setGongjiaocheName(String gongjiaocheName) {
        this.gongjiaocheName = gongjiaocheName;
    }

    public String getCheliangweizhiDati() {
        return cheliangweizhiDati;
    }

    public void setCheliangweizhiDati(String cheliangweizhiDati) {
        this.cheliangweizhiDati = cheliangweizhiDati;
    }

    public String getCheliangweizhiFangxiang() {
        return cheliangweizhiFangxiang;
    }

    public void setCheliangweizhiFangxiang(String cheliangweizhiFangxiang) {
        this.cheliangweizhiFangxiang = cheliangweizhiFangxiang;
    }

    public String getCheliangweizhiMingcheng() {
        return cheliangweizhiMingcheng;
    }

    public void setCheliangweizhiMingcheng(String cheliangweizhiMingcheng) {
        this.cheliangweizhiMingcheng = cheliangweizhiMingcheng;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Integer getGongjiaoxianluId() {
        return gongjiaoxianluId;
    }

    public void setGongjiaoxianluId(Integer gongjiaoxianluId) {
        this.gongjiaoxianluId = gongjiaoxianluId;
    }

    public String getGongjiaoxianluName() {
        return gongjiaoxianluName;
    }

    public void setGongjiaoxianluName(String gongjiaoxianluName) {
        this.gongjiaoxianluName = gongjiaoxianluName;
    }
}

package com.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.lang.reflect.InvocationTargetException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.beanutils.BeanUtils;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.enums.FieldFill;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * 到站提醒订阅
 *
 * @author
 * @email
 */
@TableName("to_station_remind")
public class DaozhanRemindEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;


	public DaozhanRemindEntity() {

	}

	public DaozhanRemindEntity(T t) {
		try {
			BeanUtils.copyProperties(this, t);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    @TableField(value = "id")

    private Integer id;


    /**
     * 用户
     */
    @TableField(value = "yonghu_id")

    private Integer yonghuId;


    /**
     * 线路
     */
    @TableField(value = "gongjiaoxianlu_id")

    private Integer gongjiaoxianluId;


    /**
     * 订阅站点名称
     */
    @TableField(value = "stop_name")

    private String stopName;


    /**
     * 提醒状态 0未触发 1已触发
     */
    @TableField(value = "remind_status")

    private Integer remindStatus;


    /**
     * 创建时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
    @TableField(value = "create_time",fill = FieldFill.INSERT)

    private Date createTime;


    /**
     * 触发时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
    @TableField(value = "trigger_time")

    private Date triggerTime;


	/**
	 * 设置：主键
	 */
    public Integer getId() {
        return id;
    }
    /**
	 * 获取：主键
	 */

    public void setId(Integer id) {
        this.id = id;
    }
    /**
	 * 设置：用户
	 */
    public Integer getYonghuId() {
        return yonghuId;
    }
    /**
	 * 获取：用户
	 */

    public void setYonghuId(Integer yonghuId) {
        this.yonghuId = yonghuId;
    }
    /**
	 * 设置：线路
	 */
    public Integer getGongjiaoxianluId() {
        return gongjiaoxianluId;
    }
    /**
	 * 获取：线路
	 */

    public void setGongjiaoxianluId(Integer gongjiaoxianluId) {
        this.gongjiaoxianluId = gongjiaoxianluId;
    }
    /**
	 * 设置：订阅站点名称
	 */
    public String getStopName() {
        return stopName;
    }
    /**
	 * 获取：订阅站点名称
	 */

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }
    /**
	 * 设置：提醒状态 0未触发 1已触发
	 */
    public Integer getRemindStatus() {
        return remindStatus;
    }
    /**
	 * 获取：提醒状态 0未触发 1已触发
	 */

    public void setRemindStatus(Integer remindStatus) {
        this.remindStatus = remindStatus;
    }
    /**
	 * 设置：创建时间
	 */
    public Date getCreateTime() {
        return createTime;
    }
    /**
	 * 获取：创建时间
	 */

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    /**
	 * 设置：触发时间
	 */
    public Date getTriggerTime() {
        return triggerTime;
    }
    /**
	 * 获取：触发时间
	 */

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }

    @Override
    public String toString() {
        return "DaozhanRemind{" +
            "id=" + id +
            ", yonghuId=" + yonghuId +
            ", gongjiaoxianluId=" + gongjiaoxianluId +
            ", stopName=" + stopName +
            ", remindStatus=" + remindStatus +
            ", createTime=" + createTime +
            ", triggerTime=" + triggerTime +
        "}";
    }
}

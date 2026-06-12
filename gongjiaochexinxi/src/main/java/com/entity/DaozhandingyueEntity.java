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
 * 到站订阅
 *
 * @author
 * @email
 */
@TableName("daozhandingyue")
public class DaozhandingyueEntity<T> implements Serializable {
    private static final long serialVersionUID = 1L;


	public DaozhandingyueEntity() {

	}

	public DaozhandingyueEntity(T t) {
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
     * 用户id
     */
    @TableField(value = "yonghu_id")

    private Integer yonghuId;


    /**
     * 线路id
     */
    @TableField(value = "gongjiaoxianlu_id")

    private Integer gongjiaoxianluId;


    /**
     * 站点名称
     */
    @TableField(value = "zhandian_name")

    private String zhandianName;


    /**
     * 订阅状态 1=订阅中 2=已触发 3=已取消
     */
    @TableField(value = "dingyue_status")

    private Integer dingyueStatus;


    /**
     * 提醒状态 0=未提醒 1=已提醒
     */
    @TableField(value = "tixing_status")

    private Integer tixingStatus;


    /**
     * 提醒信息
     */
    @TableField(value = "tixing_content")

    private String tixingContent;


    /**
     * 创建时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
    @TableField(value = "create_time",fill = FieldFill.INSERT)

    private Date createTime;


    /**
     * 更新时间
     */
    @JsonFormat(locale="zh", timezone="GMT+8", pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat
    @TableField(value = "update_time")

    private Date updateTime;


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
	 * 设置：用户id
	 */
    public Integer getYonghuId() {
        return yonghuId;
    }
    /**
	 * 获取：用户id
	 */

    public void setYonghuId(Integer yonghuId) {
        this.yonghuId = yonghuId;
    }
    /**
	 * 设置：线路id
	 */
    public Integer getGongjiaoxianluId() {
        return gongjiaoxianluId;
    }
    /**
	 * 获取：线路id
	 */

    public void setGongjiaoxianluId(Integer gongjiaoxianluId) {
        this.gongjiaoxianluId = gongjiaoxianluId;
    }
    /**
	 * 设置：站点名称
	 */
    public String getZhandianName() {
        return zhandianName;
    }
    /**
	 * 获取：站点名称
	 */

    public void setZhandianName(String zhandianName) {
        this.zhandianName = zhandianName;
    }
    /**
	 * 设置：订阅状态
	 */
    public Integer getDingyueStatus() {
        return dingyueStatus;
    }
    /**
	 * 获取：订阅状态
	 */

    public void setDingyueStatus(Integer dingyueStatus) {
        this.dingyueStatus = dingyueStatus;
    }
    /**
	 * 设置：提醒状态
	 */
    public Integer getTixingStatus() {
        return tixingStatus;
    }
    /**
	 * 获取：提醒状态
	 */

    public void setTixingStatus(Integer tixingStatus) {
        this.tixingStatus = tixingStatus;
    }
    /**
	 * 设置：提醒信息
	 */
    public String getTixingContent() {
        return tixingContent;
    }
    /**
	 * 获取：提醒信息
	 */

    public void setTixingContent(String tixingContent) {
        this.tixingContent = tixingContent;
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
	 * 设置：更新时间
	 */
    public Date getUpdateTime() {
        return updateTime;
    }
    /**
	 * 获取：更新时间
	 */

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Daozhandingyue{" +
            "id=" + id +
            ", yonghuId=" + yonghuId +
            ", gongjiaoxianluId=" + gongjiaoxianluId +
            ", zhandianName=" + zhandianName +
            ", dingyueStatus=" + dingyueStatus +
            ", tixingStatus=" + tixingStatus +
            ", tixingContent=" + tixingContent +
            ", createTime=" + createTime +
            ", updateTime=" + updateTime +
        "}";
    }
}

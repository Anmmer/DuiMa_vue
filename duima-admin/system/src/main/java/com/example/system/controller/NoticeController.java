package com.example.system.controller;

import com.example.system.configuration.aspectj.annotation.Log;
import com.example.system.configuration.aspectj.enums.BusinessType;
import com.example.system.dto.NoticeDto;
import com.example.system.service.NoticeService;
import com.example.system.configuration.utils.ShiroSecurityUtils;
import com.example.system.configuration.vo.AjaxResult;
import com.example.system.configuration.vo.DataGridView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @Auther:
 * @Description:通知公告控制器
 */
@RestController
@RequestMapping("system/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询
     */
    @GetMapping("listNoticeForPage")
    public AjaxResult listNoticeForPage(NoticeDto noticeDto){
        DataGridView gridView = this.noticeService.listNoticePage(noticeDto);
        return AjaxResult.success("查询成功",gridView.getData(),gridView.getTotal());
    }
    /**
     * 添加
     */
    @PostMapping("addNotice")
    @Log(title = "添加通知公告",businessType = BusinessType.INSERT)
    public AjaxResult addNotice(@Validated NoticeDto noticeDto) {
        noticeDto.setSimpleUser(ShiroSecurityUtils.getCurrentSimpleUser());
        return AjaxResult.toAjax(this.noticeService.addNotice(noticeDto));
    }

    /**
     * 修改
     */
    @PutMapping("updateNotice")
    @Log(title = "修改通知公告",businessType = BusinessType.UPDATE)
    public AjaxResult updateNotice(@Validated NoticeDto noticeDto) {
        noticeDto.setSimpleUser(ShiroSecurityUtils.getCurrentSimpleUser());
        return AjaxResult.toAjax(this.noticeService.updateNotice(noticeDto));
    }


    /**
     * 根据ID查询一个通知公告信息
     */
    @GetMapping("getNoticeById/{noticeId}")
    public AjaxResult getNoticeById(@PathVariable @Validated @NotNull(message = "通知公告ID不能为空") Long noticeId) {
        return AjaxResult.success(this.noticeService.getOne(noticeId));
    }

    /**
     * 删除
     */
    @DeleteMapping("deleteNoticeByIds/{noticeIds}")
    @Log(title = "删除通知公告",businessType = BusinessType.DELETE)
    public AjaxResult deleteNoticeByIds(@PathVariable @Validated @NotEmpty(message = "要删除的ID不能为空") Long[] noticeIds) {
        return AjaxResult.toAjax(this.noticeService.deleteNoticeByIds(noticeIds));
    }
}

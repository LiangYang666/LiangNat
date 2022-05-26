package com.liang.web.controller;

import com.liang.web.entity.IpEntity;
import com.liang.web.service.IpService;
import com.liang.web.utils.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/25 下午5:03
 **/
@Controller
public class IpController {

    @Autowired
    private IpService ipService;

    @GetMapping({"/ipList", "/", "/index"})
    public String table(Model model) {
        List<IpEntity> ipEntities = ipService.getAllIp();
        model.addAttribute("ips", ipEntities);
        return "ipList";
    }

    @RequestMapping("/insertPage")
    public String toInsertPage(HttpServletRequest request, Model model){
        String ip = IpUtil.getIp(request);
        System.out.println(ip);
        String ipAddress = IpUtil.getIpAddress(ip);
        IpEntity ipEntity = new IpEntity(ip, ipAddress, null, null);
        model.addAttribute("ip", ipEntity);
        return "insertPage";
    }

    @RequestMapping("/insert")
    public String save(IpEntity allowedIp){
        ipService.saveIp(allowedIp);
        System.out.println("新增了IP：" + allowedIp);
        return "redirect:/ipList";
    }

    @GetMapping("/delete/{ip}")
    public String delete(@PathVariable String ip){
        ipService.deleteByIp(ip);
        System.out.println("删除了IP：" + ip);
        return "redirect:/ipList";
    }

    @GetMapping("/updatePage/{ip}")
    public String updatePage(@PathVariable String ip, Model model){
        IpEntity allowedIp = ipService.getByIp(ip);
        model.addAttribute("ip", allowedIp);
        return "updatePage";
    }

    @PostMapping("/update")
    public String update(IpEntity allowedIp){
        ipService.updateIp(allowedIp);
        System.out.println("更新了IP：" + allowedIp);
        return "redirect:/ipList";
    }
}

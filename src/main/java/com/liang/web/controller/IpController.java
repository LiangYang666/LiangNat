package com.liang.web.controller;

import com.liang.web.entity.IpEntity;
import com.liang.web.entity.IpRegionEntity;
import com.liang.web.service.Ip2RegionService;
import com.liang.web.service.IpService;
import com.liang.web.utils.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description: TODO
 * @Author: LiangYang
 * @Date: 2022/5/25 下午5:03
 **/
@Controller
@Slf4j
public class IpController {
    private final IpService ipService;
    private final Ip2RegionService ip2RegionService;
    @Autowired
    public IpController(IpService ipService, Ip2RegionService ip2RegionService) {
        this.ipService = ipService;
        this.ip2RegionService = ip2RegionService;
    }

    @GetMapping({"/ipList", "/", "/index"})
    public String table(Model model) {
        List<IpEntity> ipEntities = ipService.getAllIp();
        model.addAttribute("ips", ipEntities);
        return "ipList";
    }

    @RequestMapping("/insertPage")
    public String toInsertPage(HttpServletRequest request, Model model){
        String ip = IpUtil.getIp(request);
        String ipAddress = "null";

        try {
            IpRegionEntity region = ip2RegionService.getRegion(ip);
            ipAddress = region.getCountry()+","+
                        region.getProvince()+","+
                        region.getCity()+
                        " ("+region.getIsp()+")";
        } catch (Exception e) {
            log.error(e.toString());
        }

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

    @GetMapping("/delete/**")   // 因为IP内容存在特殊符号‘/’，例如"192.168.0.0/24"，传统@PathVariable获取不全，所以用AntPathMatcher
    public String delete(HttpServletRequest request){
        final String pathq =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String ip = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern,pathq);
        ipService.deleteByIp(ip);
        System.out.println("删除了IP：" + ip);
        return "redirect:/ipList";
    }

    @GetMapping("/updatePage/**")
    public String updatePage(HttpServletRequest request, Model model){
        final String pathq =
                request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE).toString();
        final String bestMatchingPattern =
                request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        String ip = new AntPathMatcher().extractPathWithinPattern(bestMatchingPattern,pathq);

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

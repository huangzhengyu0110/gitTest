package cn.edu.fudan.codetracker.controller;

import cn.edu.fudan.codetracker.domain.apimodel.LifecycleInfoDTO;
import cn.edu.fudan.codetracker.domain.apimodel.LineInfoDTO;
import cn.edu.fudan.codetracker.domain.ResponseBean;
import cn.edu.fudan.codetracker.domain.resultmap.NodeInfo;
import cn.edu.fudan.codetracker.service.StatisticsService;
import cn.edu.fudan.codetracker.util.DateHandler;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.*;

/**
 * description:
 * @author fancying
 * create: 2019-11-11 21:25
 **/
@Api(value = "code-tracker statistics", tags= "统计逻辑行语句数，生存周期和修改文件数")
@RestController
@Slf4j
public class StatisticsController {

    private StatisticsService statisticsService;

    /**
     * 获取逻辑行增加，删除，修改，存活语句数和明细信息
     */
    @ApiOperation(value = "获取逻辑行增加，删除，修改，存活语句数和明细", httpMethod = "GET", response = LineInfoDTO.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "repo_uuid", dataType = "String"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid\n之间用\",\"分隔", dataType = "String"),
            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", dataType = "String", defaultValue = "1990-01-01"),
            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", dataType = "String", defaultValue = "当天"),
            @ApiImplicitParam(name = "developer", value = "开发人员名称", dataType = "String"),
            @ApiImplicitParam(name = "showDetails", value = "是否附上语句数明细", dataType = "Boolean", allowableValues = "true, false", defaultValue = "false")
    })
    @GetMapping(value = {"/codewisdom/code/line-count"})
    public ResponseBean<LineInfoDTO> getLineCount(@RequestParam(value = "repo_uuid", required = false) String repoUuid,
                                                  @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                                     @RequestParam(value = "branch", required = false) String branch,
                                     @RequestParam(value = "since", required = false) String beginDate,
                                     @RequestParam(value = "until", required = false) String endDate,
                                     @RequestParam(value = "developer", required = false) String developer,
                                     @RequestParam(value = "showDetails", required = false) Boolean showDetails){
        try{
            if(repoUuid == null && repoUuids != null){
                repoUuid = repoUuids;
            }
            if(showDetails == null){
                showDetails = false;
            }
            List<String> date= DateHandler.handleParamDate(beginDate, endDate);
            LineInfoDTO data= statisticsService.getLineCountInfo(repoUuid, branch, date.get(0), date.get(1), developer, showDetails);
            return new ResponseBean<>(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean<>(401, e.getMessage(), null);
        }
    }

    /**
     * 获取存活，损耗，修改，删除语句的生存周期（单位：天）
     */
    @ApiOperation(value = "获取存活，损耗，修改，删除语句的生存周期（天）", httpMethod = "GET", response = LifecycleInfoDTO.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "repo_uuid", dataType = "String"),
            @ApiImplicitParam(name = "repo_uuids", value = "多个repo_uuid\n之间用\",\"分隔", dataType = "String"),
            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", dataType = "String", defaultValue = "1990-01-01"),
            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", dataType = "String", defaultValue = "当天"),
            @ApiImplicitParam(name = "developer", value = "开发人员名称", dataType = "String"),
            @ApiImplicitParam(name = "type", value = "获取的数据类型(存活，损耗，修改，删除)", dataType = "String", defaultValue = "全部类型", allowableValues = "live, loss, change, delete"),
            @ApiImplicitParam(name = "showDetails", value = "是否附上语句数明细", dataType = "Boolean", allowableValues = "true, false", defaultValue = "false")
    })
    @GetMapping(value = {"/codewisdom/code/lifecycle"})
    public ResponseBean<LifecycleInfoDTO> getLifecycle(@RequestParam(value = "repo_uuid", required = false) String repoUuid,
                                                       @RequestParam(value = "repo_uuids", required = false) String repoUuids,
                                     @RequestParam(value = "branch", required = false) String branch,
                                     @RequestParam(value = "since", required = false) String beginDate,
                                     @RequestParam(value = "until", required = false) String endDate,
                                     @RequestParam(value = "type", required = false) String type,
                                     @RequestParam(value = "developer", required = false) String developer,
                                     @RequestParam(value = "showDetails", required = false) Boolean showDetails){
        try{
            if(repoUuid == null && repoUuids != null){
                repoUuid = repoUuids;
            }
            if(showDetails == null){
                showDetails = false;
            }
            List<String> dates= DateHandler.handleParamDate(beginDate, endDate);
            LifecycleInfoDTO data= statisticsService.getLifecycleInfo(repoUuid, branch, dates.get(0), dates.get(1), developer, type, showDetails);
            return new ResponseBean<>(200, "", data);
        }catch (Exception e){
            e.printStackTrace();
            // 需要修改code
            return new ResponseBean<>(401, e.getMessage(), null);
        }
    }


    @ApiOperation(value = "获取存活语句数排名前五的开发者信息", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "repo_uuid", dataType = "String", required = true),
            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", dataType = "String", defaultValue = "1990-01-01", required = true),
            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", dataType = "String", defaultValue = "当天", required = true)
    })
    @GetMapping(value = {"/statistics/top"})
    public ResponseBean<List<Map<String, Object>>> getTop5Developer(@RequestParam("repo_uuid") String repoUuid,
                                         @RequestParam("since") String beginDate,
                                         @RequestParam("until") String endDate) {
        try {
            List<String> dates= DateHandler.handleParamDate(beginDate, endDate);
            List<Map<String, Object>> data = statisticsService.getTop5LiveStatements(repoUuid, dates.get(0), dates.get(1));
            return new ResponseBean<>(200,"",data);
        } catch (Exception e) {
            return new ResponseBean<>(401,e.getMessage(),null);
        }
    }

    /**
     * 获取一段时间内文件修改的数量
     */
    @ApiOperation(value = "获取一段时间内文件修改的数量", httpMethod = "GET", notes="@return Map{\"developer\": Map{key: 开发者名称, value: 文件总数}, \"total\": 文件总数>")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "repo_uuid", dataType = "String"),
            @ApiImplicitParam(name = "since", value = "起始时间(yyyy-MM-dd)", dataType = "String", defaultValue = "1990-01-01"),
            @ApiImplicitParam(name = "until", value = "截止时间(yyyy-MM-dd)", dataType = "String", defaultValue = "当天"),
            @ApiImplicitParam(name = "developer", value = "开发人员名称", dataType = "String")
    })
    @GetMapping(value = {"/statistics/focus/file/num"})
    public ResponseBean getFocusFileNum(@RequestParam(value = "repo_uuid", required = false) String repoUuid,
                                        @RequestParam(value = "since", required = false) String beginDate,
                                        @RequestParam(value = "until", required = false) String endDate,
                                        @RequestParam(value = "developer", required = false) String developer) {
        try {
            List<String> dates= DateHandler.handleParamDate(beginDate, endDate);
            JSONObject data = statisticsService.getFocusFileNum(repoUuid, dates.get(0), dates.get(1), developer);
            return new ResponseBean<>(200,"",data);
        } catch (Exception e) {
            return new ResponseBean<>(401,e.getMessage(),null);
        }
    }

    /**
     * 获取文件列表
     * @param repoUuid repo uuid
     * @param key 模糊匹配所需要的关键词
     * @return list
     */
    @GetMapping(value = {"/codewisdom/code/files"})
    public ResponseBean<List<NodeInfo>> getFileList(@RequestParam(value = "repo_uuid") String repoUuid,
                                                    @RequestParam(value = "key", required = false) String key){
        try {
            return new ResponseBean<>(200, "success", statisticsService.getFileList(repoUuid, key));
        } catch (Exception e){
            return new ResponseBean(500, e.getMessage(), null);
        }
    }


    @Autowired
    public void setStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 如果传入日期不存在，则获取全部日期的数据
     * @param beginDate
     * @param endDate
     * @return
     */
    public static List<String> handleParamDate(String beginDate, String endDate){
        List<String> dates= new ArrayList<>(2);
        if(beginDate == null || endDate == null || beginDate.length() == 0 || endDate.length() == 0){
            beginDate= "1990-01-01";
            Calendar calendar = Calendar.getInstance();
            endDate= calendar.get(Calendar.YEAR)+ "-"+ (calendar.get(Calendar.MONTH)+ 1)+ "-" + calendar.get(Calendar.DATE);
        }
        dates.add(beginDate+ " 00:00:00");
        dates.add(endDate + " 24:00:00");
        return dates;
    }

}
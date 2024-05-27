package com.xiaopeng.workflow.controller;

import com.xiaopeng.workflow.common.dto.ConvertDTO;
import com.xiaopeng.workflow.converter.CamundaBpmnConverter;
import com.xiaopeng.workflow.converter.exception.ConversionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/bpmn")
public class XPComponentFormatController {


    @RequestMapping("/convert")
    public Object convert(@RequestBody ConvertDTO convertDTO) {
        return convertBPMN(convertDTO.getBpmnXml());
    }

    @RequestMapping("/convertXML")
    public Object convertXML(@RequestParam(name = "bpmnXml") String bpmnXml) {
        return convertBPMN(bpmnXml);
    }

    private static Object convertBPMN(String bpmnXml) {
        try {
            return CamundaBpmnConverter.convert(bpmnXml);
        } catch (ConversionException e) {
            String cause = "转换异常 ： " + e.getMessage();
            log.error(cause);
            return cause;
        } catch (Exception e) {
            String caseMsg = "exception ： " + e.getMessage();
            log.error(caseMsg);
            return caseMsg;
        }
    }


}

package com.xiaopeng.workflow;

import cn.hutool.json.JSONUtil;
import com.xiaopeng.workflow.converter.BpmnConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.getResource;

@SpringBootTest
@Slf4j
public class HelloBpmnTest {

    @Test
    public void testConvert() throws IOException {
        Resource resource = getResource("classpath:flow/converter/paral.bpmn");
        List<Map<String, Object>> convert = BpmnConverter.convert(resource.getInputStream());
        log.info("Model is:{}", JSONUtil.toJsonStr(convert));
    }
}

package com.xiaopeng.workflow;

import com.xiaopeng.workflow.converter.BpmnConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
class HelloEasyFlowBpmnApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    public void testConvert() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource("classpath:flow/converter/paral.bpmn");
        List<Map<String, Object>> convert = BpmnConverter.convert(resource.getInputStream());
        log.info("Model is:" + convert);
    }

}

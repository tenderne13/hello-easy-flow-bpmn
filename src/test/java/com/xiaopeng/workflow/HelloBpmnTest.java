package com.xiaopeng.workflow;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.getResource;

@SpringBootTest
@Slf4j
public class HelloBpmnTest {

    @Test
    public void testConvert() throws IOException {
        Resource resource = getResource("classpath:flow/converter/paral.bpmn");
    }
}

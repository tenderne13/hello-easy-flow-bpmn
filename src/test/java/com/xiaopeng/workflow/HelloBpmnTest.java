package com.xiaopeng.workflow;

import com.xiaopeng.workflow.components.XPComponentStep;
import com.xiaopeng.workflow.converter.CamundaBpmnConverter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.flows.work.WorkContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.commonExecute;
import static com.xiaopeng.workflow.HelloEasyFlowBpmnApplicationTests.getResource;

@SpringBootTest
@Slf4j
public class HelloBpmnTest {

    @BeforeAll
    public static void init() {
        HelloEasyFlowBpmnApplicationTests.init();
    }


    @Test
    public void testSimpleSequential() throws IOException {
        Resource resource = getResource("classpath:flow/converter/simple_sequential.bpmn");
        XPComponentStep componentStep = CamundaBpmnConverter.convert(resource.getInputStream());
        commonExecute(new WorkContext(), componentStep);
        log.info("testSimpleSequential done");
    }

    @Test
    public void testSimpleParallel() throws IOException {
        Resource resource = getResource("classpath:flow/converter/simple_paral.bpmn");
        XPComponentStep componentStep = CamundaBpmnConverter.convert(resource.getInputStream());
        commonExecute(new WorkContext(), componentStep);
        log.info("testSimpleParallel done");
    }
}

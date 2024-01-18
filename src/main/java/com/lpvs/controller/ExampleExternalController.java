package com.lpvs.controller;

import com.lpvs.example.LPVSExampleService;
import com.lpvs.util.LPVSWebhookUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExampleExternalController {

    private LPVSExampleService lpvsExampleService;

    @Autowired
    public ExampleExternalController(LPVSExampleService lpvsExampleService) {
        this.lpvsExampleService = lpvsExampleService;
    }

    @GetMapping("/example")
    @ResponseBody
    public ResponseEntity<String> exampleController() {
        HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();
        return ResponseEntity.ok().headers(headers).body(lpvsExampleService.example());
    }
}

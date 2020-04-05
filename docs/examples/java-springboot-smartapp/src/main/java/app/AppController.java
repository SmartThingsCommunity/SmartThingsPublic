package app;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.smartthings.sdk.smartapp.core.SmartApp;
import com.smartthings.sdk.smartapp.core.SmartAppDefinition;
import com.smartthings.sdk.smartapp.core.extensions.HttpVerificationService;
import com.smartthings.sdk.smartapp.core.models.AppLifecycle;
import com.smartthings.sdk.smartapp.core.models.ExecutionRequest;
import com.smartthings.sdk.smartapp.core.models.ExecutionResponse;


@RestController
public class AppController {
    private final SmartApp smartApp;
    private final HttpVerificationService httpVerificationService;

    public AppController(SmartAppDefinition smartAppDefinition,
            HttpVerificationService httpVerificationService) {
        smartApp = SmartApp.of(smartAppDefinition);
        this.httpVerificationService = httpVerificationService;
    }

    @GetMapping("/")
    public String home() {
        return "This app only functions as a SmartThings Automation webhook endpoint app";
    }

    @PostMapping("/smartapp")
    public ExecutionResponse handle(@RequestBody ExecutionRequest executionRequest, HttpServletRequest request) {
        Map<String, String> headers = Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(name -> name, name -> request.getHeader(name)));
        if (executionRequest.getLifecycle() != AppLifecycle.PING
                && !httpVerificationService.verify(request.getMethod(), request.getRequestURI(), headers)) {
            throw new UnauthorizedException("unable to verify request");
        }
        return smartApp.execute(executionRequest);
    }
}

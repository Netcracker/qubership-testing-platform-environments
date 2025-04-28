/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.environments.db;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.qubership.atp.environments.model.impl.Context;
import org.qubership.atp.environments.repo.impl.ContextRepository;
import org.qubership.atp.environments.service.direct.impl.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class
ContextInterceptor implements HandlerInterceptor {

    private final ContextRepository repo;
    private final MetricService metricService;

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Autowired
    public ContextInterceptor(ContextRepository repo, MetricService metricService) {
        this.repo = repo;
        this.metricService = metricService;
    }

    /**
     * Getting ip address from http request.
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (null != ip && !"".equals(ip.trim()) && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader(X_FORWARDED_FOR);
        if (null != ip && !"".equals(ip.trim()) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean isFull = request.getParameter("full") != null
                && request.getParameter("full").isEmpty()
                || Boolean.parseBoolean(request.getParameter("full"));
        String patternUuid = "[a-zA-Z0-9-,%20 ]+";
        String getPatternUrl =
                "/api(/v2)?/"
                        + "(projects|environments|tools|systems|connections|subscribers)/"
                        + patternUuid + "(/)?" + "(/systems|" + patternUuid + ")?"
                        + "(/update-versions|" + patternUuid + ")?" + "(/byName|" + ".*" + ")?";
        String postPatternUrl = "/api(/v2)?/(projects|environments|tools|systems|connections|subscribers)/"
                + "(search|filter)?";
        Pattern pattern;
        boolean matches = false;
        switch (request.getMethod()) {
            case "GET":
                pattern = Pattern.compile(getPatternUrl);
                matches = pattern.matcher(request.getRequestURI()).matches();
                break;
            case "POST":
                pattern = Pattern.compile(postPatternUrl);
                matches = pattern.matcher(request.getRequestURI()).matches();
                break;
            default:
                break;
        }
        @Deprecated
        String patternUrlDep = "/api/projects/" + patternUuid + "/environments";
        Pattern patternDep = Pattern.compile(patternUrlDep);
        boolean matchesDep = patternDep.matcher(request.getRequestURI()).matches();
        if (matches || matchesDep) {
            if (isFull && request.getRequestURI().matches(".*(projects).*")) {
                log.info("Endpoint projects/${id}?full used from this IP " + getIpAddr(request));
            }
            repo.setContext(new Context(isFull));
        } else {
            repo.setContext(new Context(false));
        }
        metricService.requestToService(getIpAddr(request), request.getRequestURI());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
    }

}

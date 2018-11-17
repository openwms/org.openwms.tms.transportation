/*
 * Copyright 2018 Heiko Scherrer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.common;

import feign.RequestInterceptor;
import org.ameba.Constants;
import org.ameba.http.RequestIDHolder;
import org.ameba.tenancy.TenantHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * A FeignConfiguration.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 */
@Configuration
class FeignConfiguration {

    public
    @Bean
    RequestInterceptor basicAuthRequestInterceptor() {
        return (t) -> {
            //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //User user = (User) authentication.getPrincipal();
            String username = "user";//user.getUsername();
            String pw = "sa";//(String) authentication.getCredentials();
            t.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + pw).getBytes(Charset.forName("UTF-8"))));
            t.header(Constants.HEADER_VALUE_X_TENANT, TenantHolder.getCurrentTenant());
            String reqId = RequestIDHolder.getRequestID();
            if (reqId != null) {
                t.header(Constants.HEADER_VALUE_X_REQUESTID, reqId);
            }
        };
    }
}

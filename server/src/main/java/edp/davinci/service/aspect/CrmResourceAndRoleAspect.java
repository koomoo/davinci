/*
 * <<
 *  Davinci
 *  ==
 *  Copyright (C) 2016 - 2020 EDP
 *  ==
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  >>
 */

package edp.davinci.service.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import edp.davinci.service.share.ShareFactor;
import lombok.extern.slf4j.Slf4j;

@Component
@Aspect
@Slf4j
public class CrmResourceAndRoleAspect {


	//切点
    @Pointcut("@annotation(edp.core.annotation.AuthShare)")
    public void shareAuth() {
    }

    public static final ThreadLocal<ShareFactor> SHARE_FACTOR_THREAD_LOCAL = new ThreadLocal<>();

}

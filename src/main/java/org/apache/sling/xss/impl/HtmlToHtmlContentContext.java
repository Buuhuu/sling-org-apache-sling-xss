/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or
 * more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the
 * Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 ******************************************************************************/
package org.apache.sling.xss.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements an escaping rule to be used for cleaning up existing HTML
 * content. The output will still be HTML.
 * <p>
 * The cleanup is performed using the AntiSamy library found at
 * <a href="http://www.owasp.org/index.php/AntiSamy">http://www.owasp.org/index.php/AntiSamy</a>
 */
public class HtmlToHtmlContentContext implements XSSFilterRule {

    /**
     * Logger
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @see XSSFilterRule#check(PolicyHandler, String)
     */
    @Override
    public boolean check(final PolicyHandler policyHandler, final String str) {
        if (StringUtils.isNotEmpty(str)) {
            try {
                return policyHandler.getAntiSamy().scan(str).getNumberOfErrors() == 0;
            } catch (final ScanException se) {
                log.warn("Unable to scan input.", se);
                log.debug("Provided input: {}", str);
            } catch (final PolicyException pe) {
                log.warn("Unable to check input.", pe);
                log.debug("Provided input: {}", str);
            }
        }
        return false;
    }

    /**
     * @see XSSFilterRule#filter(PolicyHandler, java.lang.String)
     */
    @Override
    public String filter(final PolicyHandler policyHandler, final String str) {
        if (StringUtils.isNotEmpty(str)) {
            try {
                log.debug("Protecting (HTML -> HTML) :\n{}", str);
                final CleanResults results = policyHandler.getAntiSamy().scan(str);
                final String cleaned = results.getCleanHTML();
                @SuppressWarnings("unchecked")
                final List<String> errors = results.getErrorMessages();
                for (final String error : errors) {
                    log.info("AntiSamy warning: {}", error);
                }
                log.debug("Protected (HTML -> HTML):\n{}", cleaned);

                return cleaned;
            } catch (final ScanException se) {
                log.warn("Unable to scan input.", se);
                log.debug("Provided input: {}", str);
            } catch (final PolicyException pe) {
                log.warn("Unable to check input.", pe);
                log.debug("Provided input: {}", str);
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * @see XSSFilterRule#supportsPolicy()
     */
    @Override
    public boolean supportsPolicy() {
        return true;
    }
}

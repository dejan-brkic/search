/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.search.rest.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(MonitoringController.URL_ROOT)
public class MonitoringController {

    public final static String URL_ROOT = "/api/1/monitoring";

    private String statusMessage;

    @Required
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Map<String, String> getStatus() {
        return Collections.singletonMap("status", statusMessage);
    }

}

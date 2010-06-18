/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admin.rest;

import java.util.HashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import com.sun.jersey.spi.container.ContainerRequest;
import java.util.Properties;

import org.glassfish.admin.rest.provider.ActionReportResult;
import org.glassfish.api.ActionReport;

/**
 *
 * @author ludovic champenois ludo@dev.java.net
 * Code moved from generated classes to here. Gen code inherits from this template class
 * that contains the logic for mapped commands RS Resources
 *
 */
public class TemplateCommandGetResource extends TemplateExecCommand{

    public TemplateCommandGetResource(String resourceName, String commandName, String commandMethod,
                                      HashMap<String, String> m, boolean b) {

        super ( resourceName,  commandName,  commandMethod,"","",  m,  b);
        parameterType= Constants.QUERY_PARAMETER;
    }

    @GET
    @Produces({
        MediaType.TEXT_HTML,
        MediaType.APPLICATION_JSON,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_FORM_URLENCODED})
    public ActionReportResult executeCommand() {
        try {
            Properties properties = new Properties();
            if (commandParams != null) {
//formulate parent-link attribute for this command resource
//Parent link attribute may or may not be the id/target attribute
                if (isLinkedToParent) {
                    ResourceUtil.resolveParentParamValue(commandParams, uriInfo);
                }
                properties.putAll(commandParams);
            }
            ResourceUtil.addQueryString(((ContainerRequest) requestHeaders).getQueryParameters(), properties);
            String typeOfResult = requestHeaders.getAcceptableMediaTypes().get(0).getSubtype();

            ActionReport actionReport = ResourceUtil.runCommand(commandName, properties, RestService.getHabitat(),typeOfResult);
            ActionReport.ExitCode exitCode = actionReport.getActionExitCode();
            ActionReportResult results = new ActionReportResult(commandName, actionReport, options());
            if (exitCode == ActionReport.ExitCode.SUCCESS) {
                results.setStatusCode(200); /*200 - ok*/
            } else {
                results.setStatusCode(400); /*400 - bad request*/
                results.setIsError(true);
                results.setErrorMessage(actionReport.getMessage());
            }

            return results;

        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}

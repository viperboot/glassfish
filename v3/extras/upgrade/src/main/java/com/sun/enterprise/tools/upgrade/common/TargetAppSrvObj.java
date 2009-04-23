/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * TargetAppSrvObj.java
 *
 * Created on November 29, 2007, 4:22 PM
 *
 */

package com.sun.enterprise.tools.upgrade.common;

import java.io.File;
import java.util.logging.*;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.*;
import com.sun.enterprise.tools.upgrade.common.CommonInfoModel;

/**
 *
 * @author rebeccas
 */
public class TargetAppSrvObj extends BaseDomainInfoObj{
	private String dtdFilename = null;
    private StringManager sm;
    static Logger _logger=LogService.getLogger(LogService.UPGRADE_LOGGER);

    //- Value indicates if an in-place upgrade of domains is supported by
    //- the traget appserver.  This value is specific to each product release
    //- and should be set accordingly V3 does not support in-place upgrade.
    private boolean isInPlaceUpgradeAllowed = false;
	
	/** Creates a new instance of TargetAppSrvObj */
	public TargetAppSrvObj() {
        sm = StringManager.getManager(TargetAppSrvObj.class);
    }
	
	public boolean isValidPath(String s) {
        boolean flag = false;
        File targetPathDir = new File(s);
        if (targetPathDir.exists()) {
            if (isInPlaceUpgradeAllowed()) {
                // check if this is an existing domain
                File domainXML = new File(s + "/" +
                        super.CONFIG_DOMAIN_XML_FILE);
                if (!domainXML.isFile() || !domainXML.exists()) {
                    flag = true;
                } else {
                     _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.target.dir_domain_exist",
                             targetPathDir.getAbsolutePath()));
                }
            } else {
                File tmpPath = new File(targetPathDir,
                        CommonInfoModel.getInstance().getSource().getDomainName());
                if (!tmpPath.exists()) {
                    flag = true;
                } else {
                    _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.target.dir_domain_exist",
                            tmpPath.getAbsolutePath()));
                }
            }
        } else {
            _logger.log(Level.INFO, sm.getString("enterprise.tools.upgrade.target.dir_does_not_exist",
                    targetPathDir.getAbsolutePath()));
        }
        return flag;
    }

	public void setInstallDir(String s){ 
		super.installDir = s;
		if (s != null){
			super.domainRoot = super.extractDomainRoot(s);
		}
		CommonInfoModel.getInstance().createUpgradeLogFile(installDir);
	}
	
	public String getDomainDir(){
		return getInstallDir() + "/" + super.domainName;
	}	
	
	public String getConfigXMLFile(){
		return getDomainDir() + "/" + super.CONFIG_DOMAIN_XML_FILE;
	}
	
	public String getVersionEdition(){
		if (super.versionEdition == null){
			VersionExtracter v = new VersionExtracter(super.domainRoot,
				CommonInfoModel.getInstance());
            super.version = UpgradeConstants.VERSION_3_0;
            super.edition = UpgradeConstants.ALL_PROFILE;
            super.versionEdition = v.formatVersionEditionStrings(
				super.version, super.edition);
		}
		return super.versionEdition;
	}
	
	
	//- target specific ---------------------
    public boolean isInPlaceUpgradeAllowed(){
        return isInPlaceUpgradeAllowed;
    }
}

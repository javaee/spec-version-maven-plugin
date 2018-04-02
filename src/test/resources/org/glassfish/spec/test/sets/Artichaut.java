/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.spec.test.sets;

import org.glassfish.spec.Artifact;
import org.glassfish.spec.test.TestSpec;

/**
 *
 * @author Romain Grecourt
 */
public class Artichaut extends TestSpec {
    public Artichaut() {
        super(
                new Artifact(
                "${artichaut.groupId}",
                "${artichaut.artifactId}",
                "${artichaut.mavenVersion}"),
                "${artichaut.specVersion}",
                "${artichaut.newSpecVersion}",
                "${artichaut.specImplVersion}",
                "${artichaut.implVersion}",
                "${artichaut.newImplVersion}",
                "${artichaut.specBuild}",
                "${artichaut.implBuild}",
                "${artichaut.apiPackage}",
                "${artichaut.implNamespace}",
                "${artichaut.jarType}",
                Boolean.valueOf("${artichaut.nonFinal}").booleanValue());
    }

    @Override
    public String getExpectedBundleVersion() {
        return "${artichaut.bundleVersion}";
    }

    @Override
    public String getExpectedBundleSpecVersion() {
        return "${artichaut.bundleSpecVersion}";
    }

    @Override
    public String getExpectedBundleSymbolicName() {
        return "${artichaut.bundleSymbolicName}";
    }

    @Override
    public String getExpectedJarExtensionName() {
        return "${artichaut.jarExtensionName}";
    }

    @Override
    public String getExpectedJarImplementationVersion() {
        return "${artichaut.jarImplementationVersion}";
    }

    @Override
    public String getExpectedJarSpecificationVersion() {
        return "${artichaut.jarSpecificationVersion}";
    }

    @Override
    public String getJarPath() {
       return "target/it/modules/${artichaut.artifactId}/target/${artichaut.artifactId}.jar";
    }
}

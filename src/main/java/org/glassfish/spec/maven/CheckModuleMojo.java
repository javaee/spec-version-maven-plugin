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

package org.glassfish.spec.maven;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.glassfish.spec.Artifact;
import org.glassfish.spec.Metadata;
import org.glassfish.spec.Spec;

/**
 *
 * Maven Goal to enforce spec rules and fail the build.
 * @author Romain Grecourt
 */
@Mojo(name = "check-module",
      requiresProject = true,
      defaultPhase = LifecyclePhase.PACKAGE)
public final class CheckModuleMojo extends AbstractMojo {

    /**
     * The maven project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Module to verify.
     */
    @Parameter(property = "module")
    private File module;

    /**
     * Ignore failures.
     */
    @Parameter(property = "ignoreErrors", defaultValue = "false")
    private boolean ignoreErrors;

    /**
     * Mode. Allowed values are "javaee", "jakarta"
     */
    @Parameter(property = "specMode", defaultValue = "javaee")
    private String specMode;

    /**
     * Spec.
     */
    @Parameter(property = "spec", required = true)
    private Spec spec;

    @Override
    @SuppressWarnings("checkstyle:LineLength")
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (module == null || !module.exists()) {
            module = project.getArtifact().getFile();
            if (module == null || !module.exists()) {
                getLog().error("There is no jar to verify, try using mvn package first.");
                throw new MojoFailureException("no jar to verify");
            }
        }
        try {
            if (spec == null) {
                spec = new Spec();
            }

            spec.setGroupIdPrefix(specMode.equals("jakarta") ? Spec.JAKARTA_GROUP_ID : Spec.JAVAX_GROUP_ID);
            spec.setArtifact(new Artifact(
                    project.getGroupId(),
                    project.getArtifactId(),
                    project.getVersion()));
            spec.setMetadata(Metadata.fromJar(new JarFile(module)));
            spec.verify();

            if (!spec.getErrors().isEmpty()) {
                System.out.println("");
                System.out.println(spec.getArtifact().toString());
                String specDesc = spec.toString();
                if (!specDesc.isEmpty()) {
                    System.out.println(spec.toString());
                }
                for (int i = 0; i < spec.getErrors().size(); i++) {
                    System.out.println(new StringBuilder()
                            .append('-')
                            .append(' ')
                            .append(spec.getErrors().get(i))
                            .toString());
                }
                System.out.println("");
                if (!ignoreErrors) {
                    throw new MojoFailureException("spec verification failed.");
                }
            }
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }
}

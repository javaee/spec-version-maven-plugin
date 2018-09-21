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
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.glassfish.spec.Artifact;
import org.glassfish.spec.Metadata;
import org.glassfish.spec.Spec;

/**
 *
 * Check a set of spec artifact in a staging directory.
 * @author Romain Grecourt
 */
@Mojo(name = "check-distribution",
      requiresProject = true,
      defaultPhase = LifecyclePhase.PACKAGE)
public final class CheckDistributionMojo extends AbstractMojo {

    /**
     * Include pattern.
     */
    @Parameter(property = "includes", defaultValue = "javax*.jar")
    private String includes;

    /**
     * Exclude pattern.
     */
    @Parameter(property = "excludes")
    private String excludes;

    /**
     * The directory containing the spec artifacts to process.
     */
    @Parameter(property = "dir", required = true)
    private File dir;

    /**
     * The specification configurations.
     */
    @Parameter(property = "specs", required = true)
    private List<Spec> specs;

    /**
     * Find or create the specification configuration for the given artifact.
     * @param file the artifact file to match
     * @return the spec configuration
     * @throws IOException if an error occurs while reading the JAR file entries
     */
    private Spec getSpec(final File file) throws IOException {
        JarFile jar = new JarFile(file);
        Artifact a = Artifact.fromJar(jar);
        for (Spec s : specs) {
            if (s.getArtifact().equals(a)) {
                s.setMetadata(Metadata.fromJar(jar));
                return s;
            }
        }
        Spec spec = new Spec();
        spec.setArtifact(a);
        spec.setMetadata(Metadata.fromJar(jar));
        return spec;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!dir.exists()) {
            String msg = String.format(
                    "directory (%s) does not exist",
                    dir.getAbsolutePath());
            getLog().error(msg);
            throw new MojoFailureException(msg);
        }

        List<File> jars = Collections.EMPTY_LIST;
        try {
            jars = FileUtils.getFiles(dir, includes, excludes);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }

        for (File jar : jars) {
            try {
                Spec spec = getSpec(jar);
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
                }
            } catch (IOException ex) {
                getLog().warn(ex.getMessage(), ex);
            }
        }
    }
}

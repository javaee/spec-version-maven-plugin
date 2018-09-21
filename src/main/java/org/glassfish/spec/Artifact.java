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

package org.glassfish.spec;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Represent the API JAR file as a Maven artifact.
 *
 * @author Romain Grecourt
 */
public final class Artifact {

    /**
     * Artifact groupId.
     */
    private String groupId;

    /**
     * Artifact artifactId.
     */
    private String artifactId;

    /**
     * Artifact version.
     */
    private ArtifactVersion version;

    /**
     * Artifact build number.
     */
    private String buildNumber;

    /**
     * Characters used to separate the build number within the version.
     */
    private static final String[] BUILD_NUMBER_SEPARATORS = new String[] {
        "m", "b"
    };

    /**
     * The Maven SNAPSHOT qualifier.
     */
    public static final String SNAPSHOT_QUALIFIER = "SNAPSHOT";

    /**
     * Strip the SNAPSHOT qualifier from a given qualifier.
     * @param qualifier the qualifier to process
     * @return a non SNAPSHOT qualifier
     */
    public static String stripSnapshotQualifier(final String qualifier) {
        if (qualifier != null) {
            if (qualifier.endsWith("-" + SNAPSHOT_QUALIFIER)) {
                return qualifier.replace("-" + SNAPSHOT_QUALIFIER, "");
            }
            return qualifier;
        }
        return null;
    }

    /**
     * Parse a version qualifier and extract the build number.
     * @param qualifier the qualifier to process
     * @return the build number, or {@code null} if none found
     */
    private static String getBuildNumber(final String qualifier) {
        String normalizedQualifier = stripSnapshotQualifier(qualifier);
        if (normalizedQualifier != null) {
            for (String c : BUILD_NUMBER_SEPARATORS) {
                if (normalizedQualifier.contains(c)) {
                    return normalizedQualifier.substring(
                            normalizedQualifier.lastIndexOf(c) + 1);
                }
            }
        }
        return null;
    }

    /**
     * Create a new {@link Artifact} instance.
     */
    public Artifact() {
    }

    /**
     * Create a new {@link Artifact} instance.
     * @param gId the artifact groupId
     * @param aId the artifact artifactId
     * @param v the artifact version
     */
    public Artifact(final String gId, final String aId, final String v) {
        this.groupId = gId;
        this.artifactId = aId;
        this.version = new DefaultArtifactVersion(v);
        this.buildNumber = getBuildNumber(version.getQualifier());
    }

    /**
     * Get the artifactId for this artifact.
     * @return the artifactId
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Get the groupId for this artifact.
     * @return the groupId
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Get the version for this artifact.
     * @return the version
     */
    public ArtifactVersion getVersion() {
        return version;
    }

    /**
     * Get the normalized release version for this artifact.
     * @return the version
     */
    public String getAbsoluteVersion() {
        return stripSnapshotQualifier(version.toString());
    }

    /**
     * Set the artifactId of this artifact.
     * @param aId the artifactId value to use
     */
    public void setArtifactId(final String aId) {
        this.artifactId = aId;
    }

    /**
     * Set the groupId of this artifact.
     * @param gId the artifactId value to use
     */
    public void setGroupId(final String gId) {
        this.groupId = gId;
    }

    /**
     * Set the version of this artifact.
     * @param v the artifactId value to use
     */
    public void setVersion(final String v) {
        this.version = new DefaultArtifactVersion(v);
        this.buildNumber = getBuildNumber(this.version.getQualifier());
    }

    /**
     * Get the build number of this artifact.
     * @return the build number
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * Get the {@link ZipEntry} for {@code pom.properties} in the given
     * JAR file.
     * @param jar the jar file to process
     * @return the {@link ZipEntry} if found, {@code null} otherwise
     */
    private static ZipEntry getPomPropertiesFile(final JarFile jar) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith("pom.properties")) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Create an {@link Artifact} instance from a given JAR file.
     * @param jar the jar file to process
     * @return the create {@link Artifact} instance
     * @throws IOException if an error occurs while reading JAR file entries
     */
    public static Artifact fromJar(final JarFile jar) throws IOException {
        ZipEntry entry = getPomPropertiesFile(jar);
        if (entry == null) {
            throw new RuntimeException(
                    "unable to find pom.properties "
                    + "files inside " + jar.getName());
        }
        InputStream is = jar.getInputStream(entry);
        Properties pomProps = new Properties();
        pomProps.load(is);

        return new Artifact(
                pomProps.getProperty("groupId"),
                pomProps.getProperty("artifactId"),
                pomProps.getProperty("version"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append(groupId);
        sb.append(':');
        sb.append(artifactId);
        sb.append(':');
        sb.append(version);
        sb.append(" ]");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Artifact other = (Artifact) obj;
        if (this.groupId == null && other.groupId != null) {
                return false;
        } else if (this.groupId != null
                && !this.groupId.equals(other.groupId)) {
            return false;
        }
        if (this.artifactId == null && other.artifactId != null) {
            return false;
        } else if (this.artifactId != null
                && !this.artifactId.equals(other.artifactId)) {
            return false;
        }
        if (this.version == null && other.version != null) {
            return false;
        } else if (this.version != null
                && !this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("checkstyle:MagicNumber")
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.groupId != null
                ? this.groupId.hashCode() : 0);
        hash = 71 * hash + (this.artifactId != null
                ? this.artifactId.hashCode() : 0);
        hash = 71 * hash + (this.version != null
                ? this.version.hashCode() : 0);
        hash = 71 * hash + (this.buildNumber != null
                ? this.buildNumber.hashCode() : 0);
        return hash;
    }
}

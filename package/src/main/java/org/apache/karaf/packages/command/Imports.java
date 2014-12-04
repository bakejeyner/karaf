/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.packages.command;

import java.util.SortedMap;

import org.apache.karaf.packages.core.PackageRequirement;
import org.apache.karaf.packages.core.PackageService;
import org.apache.karaf.packages.core.internal.filter.Expression;
import org.apache.karaf.packages.core.internal.filter.FilterParser;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.Row;
import org.apache.karaf.shell.support.table.ShellTable;
import org.osgi.framework.Bundle;

@Command(scope = "package", name = "imports", description = "Lists imported packages and the bundles that import them")
@Service
public class Imports implements Action {
    
    @Option(name = "--filter", description = "Only show package instead of full filter", required = false, multiValued = false)
    boolean showFilter;

    @Option(name = "--no-format", description = "Disable table rendered output", required = false, multiValued = false)
    boolean noFormat;
    
    @Option(name = "--package", description = "Only show the named package", required = false, multiValued = false)
    String packageName;

    @Reference
    private PackageService packageService;

    @Override
    public Object execute() throws Exception {
        SortedMap<String, PackageRequirement> imports = packageService.getImports();
        ShellTable table = new ShellTable();
        if (showFilter) {
            table.column("Filter");
        } else {
            table.column("Package");
            table.column("Version");
        }
        table.column("Optional");
        table.column("ID");
        table.column("Bundle Name");
        table.column("Resolveable");

        for (String filter : imports.keySet()) {
            PackageRequirement req = imports.get(filter);
            if (packageName == null || packageName.equals(req.getPackageName())) {
                Bundle bundle = req.getBundle();
                Row row = table.addRow();
                if (showFilter) {
                    row.addContent(filter);
                } else {
                    row.addContent(req.getPackageName(), req.getVersionRange());
                }
                row.addContent(getOptional(req), bundle.getBundleId(),
                               bundle.getSymbolicName(), req.isResolveable());
            }
        }
        table.print(System.out, !noFormat);
        return null;
    }

    private String getOptional(PackageRequirement req) {
        return req.isOptional() ? "optional" : "";
    }

}
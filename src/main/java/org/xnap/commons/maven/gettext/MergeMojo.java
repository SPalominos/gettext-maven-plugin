/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xnap.commons.maven.gettext;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Invokes the gettext:gettext goal and invokes msgmerge to update po files.
 *
 * @author Tammo van Lessen
 */
@Mojo(name = "merge", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
@Execute(goal = "gettext")
public class MergeMojo extends AbstractGettextMojo {
	
    /**
     * The msgmerge command.
     */
    @Parameter(defaultValue = "msgmerge", required = true)
    protected String msgmergeCmd;
    
    public void execute() throws MojoExecutionException
	{
		getLog().info("Invoking msgmerge for po files in '" + poDirectory.getAbsolutePath() + "'.");
		
		DirectoryScanner ds = new DirectoryScanner();
    	ds.setBasedir(poDirectory);
    	ds.setIncludes(new String[] {"**/*.po"});
    	ds.scan();
    	String[] files = ds.getIncludedFiles();
    	for (int i = 0; i < files.length; i++) {
    		getLog().info("Processing " + files[i]);
    		Commandline cl = new Commandline();
    		cl.setExecutable(msgmergeCmd);
        	cl.createArg().setValue("-q");
        	cl.createArg().setValue("--backup=numbered");
        	cl.createArg().setValue("-U");
        	cl.createArg().setFile(new File(poDirectory, files[i]));
        	cl.createArg().setValue(new File(poDirectory, keysFile).getAbsolutePath());
        	
        	getLog().debug("Executing: " + cl.toString());
    		StreamConsumer out = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.INFO);
    		StreamConsumer err = new LoggerStreamConsumer(getLog(), LoggerStreamConsumer.WARN);
        	try {
    			CommandLineUtils.executeCommandLine(cl, out, err);
    		} catch (CommandLineException e) {
    			getLog().error("Could not execute " + msgmergeCmd + ".", e);
    		}
    	}
    }
}

/*
 *  Copyright 2008 Maurice Zeijen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.milyn.smooks.mule;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.util.FileUtils;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class RouterFunctionalTest extends FunctionalTestCase {

	private final File routingTestDir = new File("target/routing-test");

	private final File test1File = new File(routingTestDir, "file1.dat");

	private final File[] test2Files = new File[] {
			new File(routingTestDir, "file2-0.dat"),
			new File(routingTestDir, "file2-1.dat"),
			new File(routingTestDir, "file2-2.dat"),
	};

	private final File testReplyFile = new File(routingTestDir, "fileReply.dat");

	@Override
	protected String getConfigResources() {
		return "router-mule-config.xml";
	}

	@Test
	public void testSmooks() throws Exception
    {
		InputStream in = getClass().getResourceAsStream("/router-input-message.xml");

        MuleClient client = new MuleClient();
        client.send("vm://messageInput", new DefaultMuleMessage(in));

        assertTrue("File '" + test1File + "' doesn't exist.", test1File.exists());

        for(int i = 0; i < test2Files.length; i++) {
        	assertTrue("File '" + test2Files[i] + "' doesn't exist.", test2Files[i].exists());
        }

        assertTrue("File '" + testReplyFile + "' doesn't exist.", testReplyFile.exists());

        String testReplyFileContent = IOUtils.toString(new FileInputStream(testReplyFile), "UTF-8");
        assertEquals("Reply value incorrect", "Hello World,testValue,test2Value,10,1215797456000,xmlTest1Value,xmlTest2Value,overwritten", testReplyFileContent);
    }


	/* (non-Javadoc)
	 * @see org.mule.tck.AbstractMuleTestCase#doSetUp()
	 */
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		Locale.setDefault(Locale.ENGLISH);

		FileUtils.deleteDirectory(routingTestDir);
	}

}

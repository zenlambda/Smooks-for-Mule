/**
 * Copyright (C) 2008 Maurice Zeijen <maurice@zeijen.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.milyn.smooks.mule;


import java.io.File;
import java.io.IOException;

import javax.xml.transform.dom.DOMResult;

import org.milyn.io.StreamUtils;
import org.mule.impl.RequestContext;
import org.mule.tck.AbstractMuleTestCase;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

/**
 * Unit test for {@link Transformer}
 * <p/>
 * The test in the class intentionally only test the configuration and <br>
 * execution of {@link Transformer} and not the actual tranformations<br>
 * that Smooks performs as these are covered in the Smooks project.
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class TransformerTest extends AbstractMuleTestCase
{
	private Transformer transformer;

	private UMOEventContext eventContext;

	private final String smooksConfigFile = "/transformer-smooks-config.xml";

	private final String smooksProfiledConfigFile = "/transformer-smooks-config-profiled.xml";

	public void testInitWithoutSmooksConfigFile() throws InitialisationException
	{
		boolean thrown = false;
		try {
			transformer.setConfigFile( null );
			transformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void testIllegalResultType()
	{
		boolean thrown = false;
		try {
			transformer.setConfigFile( smooksConfigFile );
			transformer.setResultType( "badResultType" );
			transformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void testJavaResultBeanId()
	{
		transformer.setConfigFile( smooksConfigFile );
		transformer.setResultType( "JAVA" );
		transformer.setJavaResultBeanId( "beanId" );
		try
		{
			transformer.initialise();
		}
		catch (InitialisationException e)
		{
			fail( "Should not have thrown A InitializationException");
		}
	}

	public void testResultClass()
	{
		transformer.setConfigFile( smooksConfigFile );
		transformer.setResultType( "RESULT" );
		transformer.setResultClass("javax.xml.transform.dom.DOMResult");
		try
		{
			transformer.initialise();
		}
		catch (InitialisationException e)
		{
			logger.error("Initialisation Exception", e);
			fail( "Should not have thrown A InitializationException");
		}
	}

	public void testResultFactoryClass()
	{
		transformer.setConfigFile( smooksConfigFile );
		transformer.setResultType( "RESULT" );
		transformer.setResultFactoryClass("test.DummyResultFactory");
		try
		{
			transformer.initialise();
		}
		catch (InitialisationException e)
		{
			logger.error("Initialisation Exception", e);

			fail( "Should not have thrown A InitializationException");
		}
	}

	public void testNoResultClassConfigurationOnRESULTResultType()
	{
		boolean thrown = false;
		try {
			transformer.setConfigFile( smooksConfigFile );
			transformer.setResultType( "RESULT" );
			transformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void testIllegalResultClassConfigurationOnRESULTResultType()
	{
		boolean thrown = false;
		try {
			transformer.setConfigFile( smooksConfigFile );
			transformer.setResultType( "RESULT" );
			transformer.setResultClass("test.DOESNOTEXIST");
			transformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}


	public void testIllegalResultFactoryClassConfigurationOnRESULTResultType()
	{
		boolean thrown = false;
		try {
			transformer.setConfigFile( smooksConfigFile );
			transformer.setResultType( "RESULT" );
			transformer.setResultFactoryClass("test.DOESNOTEXIST");
			transformer.initialise();
		} catch (InitialisationException e) {
			thrown = true;
		}
		assertTrue("expected InitialisationException to be thrown", thrown);
	}

	public void testDoTransformationExecContextAttrNotDefinend() throws Exception {
		testDoTransformation(null, null);
	}

	public void testDoTransformationWithoutExecContextAttr() throws Exception {
		testDoTransformation(false, null);
	}

	public void testDoTransformationWithExecContextAttr() throws Exception {
		testDoTransformation(true, null);
	}

	public void testDoTransformationWithExecContextAttrWithOwnAttrKey() throws Exception {
		testDoTransformation(true, "executionContextSmooks");
	}

	private void testDoTransformation(Boolean setExecuctionContextAsMessageKey, String executionContextMessagePropertyKey) throws Exception
	{
		transformer.setConfigFile( smooksConfigFile );
		transformer.setExcludeNonSerializables( false );
		if(setExecuctionContextAsMessageKey != null) {
			transformer.setExecutionContextAsMessageProperty(setExecuctionContextAsMessageKey);
		}
		if(executionContextMessagePropertyKey != null) {
			transformer.setExecutionContextMessagePropertyKey(executionContextMessagePropertyKey);
		} else {
			executionContextMessagePropertyKey = Constants.MESSAGE_PROPERTY_KEY_EXECUTION_CONTEXT;
		}

		transformer.initialise();

		byte[] inputMessage = readInputMessage();
		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );

		Object attributes = eventContext.getMessage().getProperty( executionContextMessagePropertyKey );

		if(setExecuctionContextAsMessageKey != null && setExecuctionContextAsMessageKey) {
			assertNotNull( attributes );
		} else {
			assertNull( attributes );
		}
	}


	public void testDoTransformationWithSmooksReportGeneration() throws TransformerException, InitialisationException
	{
		File reportFile = new File ( "target" + File.separator + "smooks-report.html" );
		transformer.setConfigFile( smooksConfigFile );
		transformer.setReportPath( reportFile.getAbsolutePath() );
		transformer.initialise();
		byte[] inputMessage = readInputMessage();
		try
		{
    		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
    		assertNotNull ( transformedObject );
			assertTrue( reportFile.exists() );
		}
		finally
		{
			if ( reportFile.exists() )
			{
				reportFile.delete();
			}
		}
	}

	public void testDoTransformationWithResultClassDefinend() throws TransformerException, InitialisationException
	{
		transformer.setConfigFile( smooksConfigFile );
		transformer.setResultType("RESULT");
		transformer.setResultClass("javax.xml.transform.dom.DOMResult");
		transformer.initialise();
		byte[] inputMessage = readInputMessage();

		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );
		assertTrue("transformed Object not a DOMResult", transformedObject instanceof DOMResult);
	}

	public void testDoTransformationWithResultFactoryClassDefinend() throws TransformerException, InitialisationException
	{
		transformer.setConfigFile( smooksConfigFile );
		transformer.setResultType( "RESULT" );
		transformer.setResultFactoryClass("test.DummyResultFactory");
		transformer.initialise();
		byte[] inputMessage = readInputMessage();

		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );
		assertTrue("transformed Object not a DOMResult", transformedObject instanceof DOMResult);
	}

	public void testDoTransformationWithProfileInConfig() throws TransformerException, InitialisationException
	{
		transformer.setConfigFile( smooksProfiledConfigFile );
		transformer.setProfile("profile1");
		transformer.setResultType("STRING");
		transformer.initialise();

		byte[] inputMessage = readInputMessage();

		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );
		assertTrue("transformed Object not a String", transformedObject instanceof String);
		assertTrue("result doesn't contain right xml", transformedObject.toString().contains("<yyy></yyy>"));
	}

	public void testDoTransformationWithProfileInMessage() throws TransformerException, InitialisationException
	{
		final String messagePropertyProfileKey = "smooksMessageProfile";

		transformer.setConfigFile( smooksProfiledConfigFile );
		transformer.setResultType("STRING");
		transformer.setProfileMessagePropertyKey(messagePropertyProfileKey);
		transformer.initialise();

		byte[] inputMessage = readInputMessage();

		eventContext.getMessage().setStringProperty(messagePropertyProfileKey, "profile2");

		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );
		assertTrue("transformed Object not a String", transformedObject instanceof String);
		assertTrue("result doesn't contain right xml", transformedObject.toString().contains("<zzz></zzz>"));
	}


	public void testDoTransformationWithProfileInConfigAndMessageMessage() throws TransformerException, InitialisationException
	{
		final String messagePropertyProfileKey = "smooksMessageProfile";

		transformer.setConfigFile( smooksProfiledConfigFile );
		transformer.setResultType("STRING");
		transformer.setProfile("profile1");
		transformer.setProfileMessagePropertyKey(messagePropertyProfileKey);
		transformer.initialise();

		byte[] inputMessage = readInputMessage();

		eventContext.getMessage().setStringProperty(messagePropertyProfileKey, "profile2");

		Object transformedObject = transformer.transform( inputMessage, "UTF-8", eventContext );
		assertNotNull ( transformedObject );
		assertTrue("transformed Object not a String", transformedObject instanceof String);
		assertTrue("result doesn't contain right xml", transformedObject.toString().contains("<zzz></zzz>"));
	}

	@Override
	protected void doSetUp() throws Exception
	{
    	transformer = new Transformer();
		RequestContext.setEvent( getTestEvent ( "Test!" ) );
		eventContext = RequestContext.getEventContext();
	}

	//	private
	private static byte[] readInputMessage()
	{
        try
        {
            return StreamUtils.readStream( TransformerTest.class.getResourceAsStream("/transformer-input-message.xml"));
        }
        catch (IOException e)
        {
        	e.printStackTrace();
            return "<no-message/>".getBytes();
        }
    }

}

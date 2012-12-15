package org.alveolo.butterfly.test.cocoon.email;

import java.util.HashMap;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.alveolo.butterfly.cocoon.EmptyGenerator;
import org.alveolo.butterfly.cocoon.HttpContextHelper;
import org.alveolo.butterfly.cocoon.JavaMailSerializer;
import org.alveolo.butterfly.cocoon.SaxonTransformer;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/mail-context.xml"})
public class MailSerializerTest {
	@Autowired
	private JavaMailSenderImpl sender;

	@Test
	public void simpleHtmlMessage() throws Exception {
		MimeMessage message = sender.createMimeMessage();

		Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
		pipeline.addComponent(new EmptyGenerator());
		pipeline.addComponent(new SaxonTransformer(null, getClass().getResource("/mail.xsl")));
		pipeline.addComponent(new JavaMailSerializer(message));

		// JavaMailSerializer does not write any bytes but fills passed MimeMessage instead
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		HttpContextHelper.storeModel(new HashMap<String, Object>(), parameters);

		pipeline.setup(new NullOutputStream(), parameters);

		pipeline.execute();

		message.setFrom(new InternetAddress("from@domain.com"));
		message.setRecipient(Message.RecipientType.TO, new InternetAddress("to@domain.com"));

		message.writeTo(System.out);

//		sender.send(message);
	}
}

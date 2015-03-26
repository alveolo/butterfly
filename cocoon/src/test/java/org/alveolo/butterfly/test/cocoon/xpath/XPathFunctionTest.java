package org.alveolo.butterfly.test.cocoon.xpath;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;

import org.apache.cocoon.pipeline.NonCachingPipeline;
import org.apache.cocoon.pipeline.Pipeline;
import org.apache.cocoon.sax.SAXPipelineComponent;
import org.apache.cocoon.sax.component.XMLGenerator;
import org.apache.cocoon.sax.component.XMLSerializer;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.alveolo.butterfly.cocoon.HttpContextHelper;
import org.alveolo.butterfly.cocoon.SaxonTransformer;
import org.alveolo.butterfly.saxon.xpath.functions.CoreConstants;
import org.alveolo.butterfly.saxon.xpath.functions.Marshall;
import org.alveolo.butterfly.saxon.xpath.functions.Message;


public class XPathFunctionTest {
	@Test
	public void transformBind() throws Exception {
		ApplicationContext rootContext = new ClassPathXmlApplicationContext("beans.xml");

		ServletContext servletContext = new MockServletContext();

		StaticWebApplicationContext webAppContext = new StaticWebApplicationContext();
		webAppContext.setParent(rootContext);
		webAppContext.setServletContext(servletContext);
		webAppContext.refresh();

		HttpServletRequest request = new MockHttpServletRequest();
		HttpServletResponse response = new MockHttpServletResponse();
		request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppContext);

		Map<String, Object> model = new HashMap<String, Object>();
		bindModelAttribute(model, "foo", new Foo(1, "admin"));

		// pipeline

		Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
		pipeline.addComponent(new XMLGenerator(getClass().getResource("/form.xml")));
		pipeline.addComponent(new SaxonTransformer(webAppContext, getClass().getResource("/layout.xsl")));
		pipeline.addComponent(new XMLSerializer());

		HashMap<String, Object> parameters = new HashMap<String, Object>();
		HttpContextHelper.storeServletContext(servletContext, parameters);
		HttpContextHelper.storeRequest(request, parameters);
		HttpContextHelper.storeResponse(response, parameters);
		HttpContextHelper.storeModel(model, parameters);

		pipeline.setup(System.out, parameters);

		pipeline.execute();
	}

	@Test
	public void transformI18N() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext("beans.xml");

		TransformerFactoryImpl tf = new TransformerFactoryImpl();
		Configuration conf = tf.getConfiguration();
		conf.registerExtensionFunction(new Message());
		conf.registerExtensionFunction(new Marshall());

		URL xml = getClass().getResource("/empty.xml");
		URL xsl = getClass().getResource("/i18n.xsl");

		final Transformer transformer = tf.newTransformer(new StreamSource(xsl.toExternalForm()));
		System.out.println(transformer.getOutputProperties());
		transformer.setParameter(CoreConstants.APPLICATION_CONTEXT_PARAM.getClarkName(), context);
		transformer.setParameter("bar", new Bar(UUID.randomUUID(), new Date()));
		transformer.transform(new StreamSource(xml.toExternalForm()), new StreamResult(System.out));
	}

	private void bindModelAttribute(Map<String, Object> model, String name, Object value) {
		model.put(name, value);

		String bindingResultKey = BindingResult.MODEL_KEY_PREFIX + name;
		WebDataBinder binder = new WebRequestDataBinder(value, name);
		model.put(bindingResultKey, binder.getBindingResult());
	}
}

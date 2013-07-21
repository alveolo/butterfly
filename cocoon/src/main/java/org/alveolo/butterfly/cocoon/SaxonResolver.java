package org.alveolo.butterfly.cocoon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;


class SaxonResolver implements URIResolver {
	private final HttpServletRequest request;
	private final HttpServletResponse response;

	SaxonResolver(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		try {
			if (href.startsWith("servlet:")) {
				RequestDispatcher dispatcher = request.getServletContext().getRequestDispatcher(href.substring(8));

				HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request);

				final ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);

				HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response) {
					ServletOutputStream out = new ServletOutputStream() {
						@Override public void write(int ch) throws IOException {
							buf.write(ch);
						}
						@Override public void write(byte[] b) throws IOException {
							buf.write(b);
						}
						@Override public void write(byte[] b, int off, int len) throws IOException {
							buf.write(b, off, len);
						}
					};
					@Override public ServletOutputStream getOutputStream() {
						return out;
					}
				};

				dispatcher.include(requestWrapper, responseWrapper);

				return new StreamSource(new ByteArrayInputStream(buf.toByteArray()));
			}
		} catch (ServletException ignore) {
			// TODO
		} catch (IOException ignore) {
			// TODO
		}

		return null;
	}
}

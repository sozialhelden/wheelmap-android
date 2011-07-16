package wheelmap.org.util;

import java.io.StringReader;
import java.io.StringWriter;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import wheelmap.org.WheelMapException;


public final class XmlSupport {

	public static <T> T serialize(final String xmlData, final Class<T> clazz) {
		Serializer serializer = new Persister();
		try {
			return serializer.read(clazz, new StringReader(xmlData), false);
		} catch (Exception e) {
			throw new WheelMapException(e);
		}
	}
	
	public static String deserialize(final Object instance) {
		Serializer serializer = new Persister();
		try {
			StringWriter stringWriter=new StringWriter();
			serializer.write(instance, stringWriter);
			return stringWriter.toString();
		} catch (Exception e) {
			throw new WheelMapException(e);
		}
	}
	
	private XmlSupport() {	
	}
	
}

package com.github.leeonky.dal.extensions.inspector;

import com.github.leeonky.util.Sneaky;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.Map;

class ObjectWriter {
    private final Document doc = Sneaky.get(DocumentBuilderFactory.newInstance()::newDocumentBuilder).newDocument();

    private String xmlString() {
        StringWriter writer = new StringWriter();
        Sneaky.run(() -> Sneaky.get(TransformerFactory.newInstance()::newTransformer)
                .transform(new DOMSource(doc), new StreamResult(writer)));
        return writer.getBuffer().toString();
    }

    private void appendEntry(Node parent, String name, Object data) {
        Element newNode = doc.createElement(name);
        parent.appendChild(newNode);
        appendValue(newNode, data);
    }

    @SuppressWarnings("unchecked")
    private void appendValue(Element parent, Object data) {
        if (data instanceof Map)
            for (Map.Entry<String, ?> entry : ((Map<String, ?>) data).entrySet())
                appendEntry(parent, entry.getKey(), entry.getValue());
        else if (data instanceof Iterable)
            for (Object e : ((Iterable<?>) data))
                appendEntry(parent, "__item", e);
        else
            parent.appendChild(doc.createTextNode(String.valueOf(data)));
    }

    public static String serialize(Object data) {
        ObjectWriter objectWriter = new ObjectWriter();
        objectWriter.appendEntry(objectWriter.doc, "object", data);
        return objectWriter.xmlString();
    }
}

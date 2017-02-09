package uwc.xmvc;

import uwc.util.trace.StackTraceAnchor;
import uwc.util.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.SAXParserFactory;

/**
 * 挡板数据服务.
 * @author steven
 * ==========================================================
 * <?xml version="1.0" encoding="UTF-8"?>
 * <mocklist version="1.0">
 * <service namespace="/logout">{"result":"success"}</service>
 * </mocklist>
 * ==========================================================
 * 其中key对应的是请求类型，string是该请求类型对应的返回的response（json类型）
 */
class MockService {
    private final static String ELEMENT_SERVICE = "service";
    private final static String ATTRIBUTE_NAME = "namespace";

    private HashMap<String, String> mMockForJumpTable = new HashMap<String, String>();


    protected String findMock(String service) {
        return mMockForJumpTable.get(service);
    }

    protected void refresh(InputStream mockRaw){
        try {
            mMockForJumpTable.clear();
            SAXParserFactory.newInstance()
                    .newSAXParser().parse(mockRaw, new MockHandler());
        } catch (Exception e) {
            Logger.toggle().eat(Logger.Level.debug, new StackTraceAnchor(e.getMessage()));
        }
    }

    private class MockHandler extends DefaultHandler {
        private String mService = null;

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if(!MockService.ELEMENT_SERVICE.equals(localName))
                return;
            mService = attributes.getValue(MockService.ATTRIBUTE_NAME);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if(!MockService.ELEMENT_SERVICE.equals(localName))
                return;
            mService = null;
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            if(null == mService)
                return;
            MockService.this.mMockForJumpTable.put(mService, new String(ch, start, length));
        }
    }
}

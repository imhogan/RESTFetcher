package au.com.thinkronicity.RestFetcher;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;

/**
 * A simple nammespace map for XML namespace prefixes.
 * 
 * @author Ian Hogan, Ian_MacDonald_Hogan@yahoo.com
 *
 */
public class MappedNamespaceContext
implements NamespaceContext {
    private final Map<String, String> PREF_MAP = new HashMap<String, String>();

    public MappedNamespaceContext(Map<String, String> prefMap) {
        this.PREF_MAP.putAll(prefMap);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return this.PREF_MAP.get(prefix);
    }

    @Override
    public String getPrefix(String arg0) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("rawtypes")
	@Override
    public Iterator getPrefixes(String arg0) {
        throw new UnsupportedOperationException();
    }
}

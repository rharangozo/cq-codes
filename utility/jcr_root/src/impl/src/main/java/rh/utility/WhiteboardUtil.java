package rh.utility;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class WhiteboardUtil {

	private BundleContext bc;
	private ServiceListener serviceListener;
	
	public static interface Callback<T> {

		void register(T service);

		void unregister(T service);
	}

	@SuppressWarnings("unchecked")
	public static <T> WhiteboardUtil create(BundleContext bc, final Callback<T> callback, String serviceName) {

		final WhiteboardUtil wu = new WhiteboardUtil();
		
		wu.bc = bc;		
		wu.serviceListener = new ServiceListener() {

			public void serviceChanged(ServiceEvent event) {
				ServiceReference sr = event.getServiceReference();
				switch (event.getType()) {
				case ServiceEvent.REGISTERED: {
					callback.register((T) wu.bc.getService(sr));
					break;
				}
				case ServiceEvent.UNREGISTERING: {
					callback.unregister((T) wu.bc.getService(sr));
					break;
				}
				}
			}
		};
		
		try {

			String filter = "(objectclass=" + serviceName + ")";

			bc.addServiceListener(wu.serviceListener, filter);

			ServiceReference[] srs = bc.getServiceReferences(null, filter);
			if (srs != null) {
				for (ServiceReference sr : srs) {
					callback.register((T) bc.getService(sr));
				}
			}

		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	public void close() {
		bc.removeServiceListener(serviceListener);
	}
}

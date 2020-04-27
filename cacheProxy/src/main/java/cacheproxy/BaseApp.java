package cacheproxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.common.config.Version;
import com.solacesystems.jcsmp.CapabilityType;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPRuntime;
import com.solacesystems.jcsmp.JCSMPSession;

public abstract class BaseApp {

	protected static final Logger logger = LoggerFactory.getLogger(BaseApp.class);
	protected JCSMPSession session = null;
	protected Conf conf = new Conf();

	public BaseApp() {
		Version v = JCSMPRuntime.onlyInstance().getVersion();
		logger.info("{} / JCSMP {}", getClass().getSimpleName(), v.getSwVersion());
		logger.info("===================================================");

	}

	protected JCSMPProperties buildJSCMPProperties() {
		JCSMPProperties properties = new JCSMPProperties();
		properties.setProperty(JCSMPProperties.HOST, conf.host);
		properties.setProperty(JCSMPProperties.VPN_NAME, conf.vpn);
		properties.setProperty(JCSMPProperties.USERNAME, conf.user);
		properties.setProperty(JCSMPProperties.PASSWORD, conf.pwd);

		return properties;
	}

	protected void printRouterInfo(JCSMPSession s) {
		final List<CapabilityType> routerversioncaps = new ArrayList<CapabilityType>() {
			private static final long serialVersionUID = 1L;
			{
				add(CapabilityType.PEER_PLATFORM);
				add(CapabilityType.PEER_SOFTWARE_DATE);
				add(CapabilityType.PEER_SOFTWARE_VERSION);
			}
		};

		try {
			String routerInfo = "Appliance information: ";
			Iterator<CapabilityType> it_routerinfo = routerversioncaps.iterator();
			while (it_routerinfo.hasNext()) {
				routerInfo += String.valueOf(s.getCapability(it_routerinfo.next()));
				if (it_routerinfo.hasNext())
					routerInfo += ", ";
			}
			System.out.println(routerInfo);

			System.out.println("Appliance capabilities: ");
			it_routerinfo = Arrays.asList(CapabilityType.values()).iterator();
			while (it_routerinfo.hasNext()) {
				CapabilityType c = it_routerinfo.next();
				if (routerversioncaps.contains(c))
					continue;
				System.out.printf("%-30s : %s%n", c, s.getCapability(c));
			}

		} catch (JCSMPException ex) {
			System.out.println("Error occurred printing appliance info: " + ex);
		}
	}
}
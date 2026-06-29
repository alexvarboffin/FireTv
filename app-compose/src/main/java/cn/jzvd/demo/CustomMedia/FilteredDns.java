package cn.jzvd.demo.CustomMedia;

import androidx.annotation.NonNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Dns;

/**
 * Skips {@code 0.0.0.0} DNS answers returned by ad-blocking/private DNS resolvers so OkHttp
 * can fall back to routable addresses (typically IPv6 for Pluto CDN hosts).
 */
public final class FilteredDns implements Dns {

    private final Dns delegate;

    public FilteredDns(Dns delegate) {
        this.delegate = delegate;
    }

    public FilteredDns() {
        this(Dns.SYSTEM);
    }

    @NonNull
    @Override
    public List<InetAddress> lookup(@NonNull String hostname) throws UnknownHostException {
        List<InetAddress> addresses = delegate.lookup(hostname);
        List<InetAddress> filtered = new ArrayList<>(addresses.size());
        for (InetAddress address : addresses) {
            if (address.isAnyLocalAddress()) {
                continue;
            }
            if ("0.0.0.0".equals(address.getHostAddress())) {
                continue;
            }
            filtered.add(address);
        }
        if (filtered.isEmpty()) {
            throw new UnknownHostException("No routable address for " + hostname);
        }
        return filtered;
    }
}

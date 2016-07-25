package com.kong.cloudstack.utils;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
/**
 * Created by kong on 2016/1/22.
 */
public class NetUtil {
    private static final Logger logger = LoggerFactory.getLogger(NetUtil.class);
    public static final String LOCALHOST = "127.0.0.1";
    public static final String ANYHOST = "0.0.0.0";
    private static final int RND_PORT_START = 30000;
    private static final int RND_PORT_RANGE = 10000;
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
    private static volatile InetAddress LOCAL_ADDRESS = null;

    public NetUtil() {
    }

    public static final String getIpByDomain(String domain) throws UnknownHostException {
        return InetAddress.getByName(domain).getHostAddress();
    }

    public static int getRandomPort() {
        return 30000 + RANDOM.nextInt(10000);
    }

    public static int getAvailablePort() {
        ServerSocket ss = null;

        int e1;
        try {
            ss = new ServerSocket();
            ss.bind((SocketAddress)null);
            int e = ss.getLocalPort();
            return e;
        } catch (IOException var12) {
            e1 = getRandomPort();
        } finally {
            if(ss != null) {
                try {
                    ss.close();
                } catch (IOException var11) {
                    ;
                }
            }

        }

        return e1;
    }

    public static int getAvailablePort(int port) {
        if(port <= 0) {
            return getAvailablePort();
        } else {
            for(int i = port; i < '\uffff'; ++i) {
                ServerSocket ss = null;

                try {
                    ss = new ServerSocket(i);
                    int e = i;
                    return e;
                } catch (IOException var13) {
                    ;
                } finally {
                    if(ss != null) {
                        try {
                            ss.close();
                        } catch (IOException var12) {
                            ;
                        }
                    }

                }
            }

            return port;
        }
    }

    public static boolean isInvalidPort(int port) {
        return port > 0 || port <= '\uffff';
    }

    public static boolean isValidAddress(String address) {
        return ADDRESS_PATTERN.matcher(address).matches();
    }

    public static boolean isLocalHost(String host) {
        return host != null && (LOCAL_IP_PATTERN.matcher(host).matches() || host.equalsIgnoreCase("localhost"));
    }

    public static boolean isAnyHost(String host) {
        return "0.0.0.0".equals(host);
    }

    public static boolean isInvalidLocalHost(String host) {
        return host == null || host.length() == 0 || host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0") || LOCAL_IP_PATTERN.matcher(host).matches();
    }

    public static boolean isValidLocalHost(String host) {
        return !isInvalidLocalHost(host);
    }

    public static InetSocketAddress getLocalSocketAddress(String host, int port) {
        return isInvalidLocalHost(host)?new InetSocketAddress(port):new InetSocketAddress(host, port);
    }

    private static boolean isValidAddress(InetAddress address) {
        if(address != null && !address.isLoopbackAddress()) {
            String name = address.getHostAddress();
            return name != null && !"0.0.0.0".equals(name) && !"127.0.0.1".equals(name) && IP_PATTERN.matcher(name).matches();
        } else {
            return false;
        }
    }

    public static String getLocalHost() {
        InetAddress address = getLocalAddress();
        return address == null?"127.0.0.1":address.getHostAddress();
    }

    public static List<String> getLocalIps() throws SocketException {
        ArrayList ips = Lists.newArrayList();
        ArrayList listAdr = Lists.newArrayList();
        Enumeration nifs = NetworkInterface.getNetworkInterfaces();
        if(nifs == null) {
            return ips;
        } else {
            while(nifs.hasMoreElements()) {
                NetworkInterface nif = (NetworkInterface)nifs.nextElement();
                Enumeration adrs = nif.getInetAddresses();

                while(adrs.hasMoreElements()) {
                    String ip = ((InetAddress)adrs.nextElement()).getHostAddress();
                    if(ip != null && !"0.0.0.0".equals(ip) && !"127.0.0.1".equals(ip) && IP_PATTERN.matcher(ip).matches()) {
                        ips.add(ip);
                    }
                }
            }

            return ips;
        }
    }

    public static InetAddress getLocalAddress() {
        if(LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        } else {
            InetAddress localAddress = getLocalAddress0();
            LOCAL_ADDRESS = localAddress;
            return localAddress;
        }
    }

    public static String getLogHost() {
        InetAddress address = LOCAL_ADDRESS;
        return address == null?"127.0.0.1":address.getHostAddress();
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;

        try {
            localAddress = InetAddress.getLocalHost();
            if(isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable var6) {
            logger.warn("Failed to retriving ip address, " + var6.getMessage(), var6);
        }

        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            if(e != null) {
                while(e.hasMoreElements()) {
                    try {
                        NetworkInterface e1 = (NetworkInterface)e.nextElement();
                        Enumeration addresses = e1.getInetAddresses();
                        if(addresses != null) {
                            while(addresses.hasMoreElements()) {
                                try {
                                    InetAddress e2 = (InetAddress)addresses.nextElement();
                                    if(isValidAddress(e2)) {
                                        return e2;
                                    }
                                } catch (Throwable var5) {
                                    logger.warn("Failed to retriving ip address, " + var5.getMessage(), var5);
                                }
                            }
                        }
                    } catch (Throwable var7) {
                        logger.warn("Failed to retriving ip address, " + var7.getMessage(), var7);
                    }
                }
            }
        } catch (Throwable var8) {
            logger.warn("Failed to retriving ip address, " + var8.getMessage(), var8);
        }

        logger.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }

    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException var2) {
            return hostName;
        }
    }

    public static String toAddressString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    public static InetSocketAddress toAddress(String address) {
        int i = address.indexOf(58);
        String host;
        int port;
        if(i > -1) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
            port = 0;
        }

        return new InetSocketAddress(host, port);
    }

    public static String toURL(String protocol, String host, int port, String path) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://");
        sb.append(host).append(':').append(port);
        if(path.charAt(0) != 47) {
            sb.append('/');
        }

        sb.append(path);
        return sb.toString();
    }
}
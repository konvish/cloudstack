package com.kong.monitor.biz;

/**
 * Created by kong on 2016/1/24.
 */
public class FormatTest {
    private StringBuffer eventMessage;
    private boolean quoteValues;
    private boolean useInternalDate;
    private static final String KVDELIM = "=";
    private static final String PAIRDELIM = " ";
    private static final char QUOTE = '\"';
    private static final String PREFIX_NAME = "name";
    private static final String PREFIX_EVENT_ID = "event_id";
    private static final String THROWABLE_CLASS = "throwable_class";
    private static final String THROWABLE_MESSAGE = "throwable_message";
    private static final String THROWABLE_STACKTRACE_ELEMENTS = "stacktrace_elements";
    public static String AC_MANAGEMENT_DEST_NT_DOMAIN = "dest_nt_domain";
    public static String AC_MANAGEMENT_SIGNATURE = "signature";
    public static String AC_MANAGEMENT_SRC_NT_DOMAIN = "src_nt_domain";
    public static String AUTH_ACTION = "action";
    public static String AUTH_APP = "app";
    public static String AUTH_DEST = "dest";
    public static String AUTH_SRC = "src";
    public static String AUTH_SRC_USER = "src_user";
    public static String AUTH_USER = "user";
    public static String CHANGE_ENDPOINT_PROTECTION_ACTION = "action";
    public static String CHANGE_ENDPOINT_PROTECTION_CHANGE_TYPE = "change_type";
    public static String CHANGE_ENDPOINT_PROTECTION_DEST = "dest";
    public static String CHANGE_ENDPOINT_PROTECTION_HASH = "hash";
    public static String CHANGE_ENDPOINT_PROTECTION_GID = "gid";
    public static String CHANGE_ENDPOINT_PROTECTION_ISDR = "isdr";
    public static String CHANGE_ENDPOINT_PROTECTION_MODE = "mode";
    public static String CHANGE_ENDPOINT_PROTECTION_MODTIME = "modtime";
    public static String CHANGE_ENDPOINT_PROTECTION_PATH = "path";
    public static String CHANGE_ENDPOINT_PROTECTION_SIZE = "size";
    public static String CHANGE_ENDPOINT_PROTECTION_UID = "uid";
    public static String CHANGE_NETWORK_PROTECTION_ACTION = "action";
    public static String CHANGE_NETWORK_PROTECTION_COMMAND = "command";
    public static String CHANGE_NETWORK_PROTECTION_DVC = "dvc";
    public static String CHANGE_NETWORK_PROTECTION_USER = "user";
    public static String COMMON_CATEGORY = "category";
    public static String COMMON_COUNT = "count";
    public static String COMMON_DESC = "desc";
    public static String COMMON_DHCP_POOL = "dhcp_pool";
    public static String COMMON_DURATION = "duration";
    public static String COMMON_DVC_HOST = "dvc_host";
    public static String COMMON_DVC_IP = "dvc_ip";
    public static String COMMON_DVC_IP6 = "dvc_ip6";
    public static String COMMON_DVC_LOCATION = "dvc_location";
    public static String COMMON_DVC_MAC = "dvc_mac";
    public static String COMMON_DVC_NT_DOMAIN = "dvc_nt_domain";
    public static String COMMON_DVC_NT_HOST = "dvc_nt_host";
    public static String COMMON_DVC_TIME = "dvc_time";
    public static String COMMON_END_TIME = "end_time";
    public static String COMMON_EVENT_ID = "event_id";
    public static String COMMON_LENGTH = "length";
    public static String COMMON_LOG_LEVEL = "log_level";
    public static String COMMON_NAME = "name";
    public static String COMMON_PID = "pid";
    public static String COMMON_PRIORITY = "priority";
    public static String COMMON_PRODUCT = "product";
    public static String COMMON_PRODUCT_VERSION = "product_version";
    public static String COMMON_REASON = "reason";
    public static String COMMON_RESULT = "result";
    public static String COMMON_SEVERITY = "severity";
    public static String COMMON_START_TIME = "start_time";
    public static String COMMON_TRANSACTION_ID = "transaction_id";
    public static String COMMON_URL = "url";
    public static String COMMON_VENDOR = "vendor";
    public static String DNS_DEST_DOMAIN = "dest_domain";
    public static String DNS_DEST_RECORD = "dest_record";
    public static String DNS_DEST_ZONE = "dest_zone";
    public static String DNS_RECORD_CLASS = "record_class";
    public static String DNS_RECORD_TYPE = "record_type";
    public static String DNS_SRC_DOMAIN = "src_domain";
    public static String DNS_SRC_RECORD = "src_record";
    public static String DNS_SRC_ZONE = "src_zone";
    public static String EMAIL_RECIPIENT = "recipient";
    public static String EMAIL_SENDER = "sender";
    public static String EMAIL_SUBJECT = "subject";
    public static String FILE_ACCESS_TIME = "file_access_time";
    public static String FILE_CREATE_TIME = "file_create_time";
    public static String FILE_HASH = "file_hash";
    public static String FILE_MODIFY_TIME = "file_modify_time";
    public static String FILE_NAME = "file_name";
    public static String FILE_PATH = "file_path";
    public static String FILE_PERMISSION = "file_permission";
    public static String FILE_SIZE = "file_size";
    public static String INTRUSION_DETECTION_CATEGORY = "category";
    public static String INTRUSION_DETECTION_DEST = "dest";
    public static String INTRUSION_DETECTION_DVC = "dvc";
    public static String INTRUSION_DETECTION_IDS_TYPE = "ids_type";
    public static String INTRUSION_DETECTION_PRODUCT = "product";
    public static String INTRUSION_DETECTION_SEVERITY = "severity";
    public static String INTRUSION_DETECTION_SIGNATURE = "signature";
    public static String INTRUSION_DETECTION_SRC = "src";
    public static String INTRUSION_DETECTION_USER = "user";
    public static String INTRUSION_DETECTION_VENDOR = "vendor";
    public static String MALWARE_ENDPOINT_PROTECTION_ACTION = "action";
    public static String MALWARE_ENDPOINT_PROTECTION_DEST_NT_DOMAIN = "dest_nt_domain";
    public static String MALWARE_ENDPOINT_PROTECTION_FILE_HASH = "file_hash";
    public static String MALWARE_ENDPOINT_PROTECTION_FILE_NAME = "file_name";
    public static String MALWARE_ENDPOINT_PROTECTION_FILE_PATH = "file_path";
    public static String MALWARE_ENDPOINT_PROTECTION_PRODUCT = "product";
    public static String MALWARE_ENDPOINT_PROTECTION_PRODUCT_VERSION = "product_version";
    public static String MALWARE_ENDPOINT_PROTECTION_SIGNATURE = "signature";
    public static String MALWARE_ENDPOINT_PROTECTION_SIGNATURE_VERSION = "signature_version";
    public static String MALWARE_ENDPOINT_PROTECTION_DEST = "dest";
    public static String MALWARE_ENDPOINT_PROTECTION_SRC_NT_DOMAIN = "src_nt_domain";
    public static String MALWARE_ENDPOINT_PROTECTION_USER = "user";
    public static String MALWARE_ENDPOINT_PROTECTION_VENDOR = "vendor";
    public static String MALWARE_NETWORK_PROTECTION_PRODUCT = "product";
    public static String MALWARE_NETWORK_PROTECTION_SEVERITY = "severity";
    public static String MALWARE_NETWORK_PROTECTION_VENDOR = "vendor";
    public static String NETWORK_TRAFFIC_ESS_ACTION = "action";
    public static String NETWORK_TRAFFIC_ESS_DEST_PORT = "dest_port";
    public static String NETWORK_TRAFFIC_ESS_PRODUCT = "product";
    public static String NETWORK_TRAFFIC_ESS_SRC_PORT = "src_port";
    public static String NETWORK_TRAFFIC_ESS_VENDOR = "vendor";
    public static String NETWORK_TRAFFIC_GENERIC_APP_LAYER = "app_layer";
    public static String NETWORK_TRAFFIC_GENERIC_BYTES_IN = "bytes_in";
    public static String NETWORK_TRAFFIC_GENERIC_BYTES_OUT = "bytes_out";
    public static String NETWORK_TRAFFIC_GENERIC_CHANNEL = "channel";
    public static String NETWORK_TRAFFIC_GENERIC_CVE = "cve";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_APP = "dest_app";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_CNC_CHANNEL = "dest_cnc_channel";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_CNC_NAME = "dest_cnc_name";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_CNC_PORT = "dest_cnc_port";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_COUNTRY = "dest_country";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_HOST = "dest_host";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_INT = "dest_int";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_IP = "dest_ip";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_IPV6 = "dest_ipv6";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_LAT = "dest_lat";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_LONG = "dest_long";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_MAC = "dest_mac";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_NT_DOMAIN = "dest_nt_domain";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_NT_HOST = "dest_nt_host";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_PORT = "dest_port";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_IP = "dest_translated_ip";
    public static String NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_PORT = "dest_translated_port";
    public static String NETWORK_TRAFFIC_GENERIC_IP_VERSION = "ip_version";
    public static String NETWORK_TRAFFIC_GENERIC_OUTBOUND_INTERFACE = "outbound_interface";
    public static String NETWORK_TRAFFIC_GENERIC_PACKETS_IN = "packets_in";
    public static String NETWORK_TRAFFIC_GENERIC_PACKETS_OUT = "packets_out";
    public static String NETWORK_TRAFFIC_GENERIC_PROTO = "proto";
    public static String NETWORK_TRAFFIC_GENERIC_SESSION_ID = "session_id";
    public static String NETWORK_TRAFFIC_GENERIC_SSID = "ssid";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_COUNTRY = "src_country";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_HOST = "src_host";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_INT = "src_int";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_IP = "src_ip";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_IPV6 = "src_ipv6";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_LAT = "src_lat";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_LONG = "src_long";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_MAC = "src_mac";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_NT_DOMAIN = "src_nt_domain";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_NT_HOST = "src_nt_host";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_PORT = "src_port";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_IP = "src_translated_ip";
    public static String NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_PORT = "src_translated_port";
    public static String NETWORK_TRAFFIC_GENERIC_SYSLOG_ID = "syslog_id";
    public static String NETWORK_TRAFFIC_GENERIC_SYSLOG_PRIORITY = "syslog_priority";
    public static String NETWORK_TRAFFIC_GENERIC_TCP_FLAG = "tcp_flag";
    public static String NETWORK_TRAFFIC_GENERIC_TOS = "tos";
    public static String NETWORK_TRAFFIC_GENERIC_TRANSPORT = "transport";
    public static String NETWORK_TRAFFIC_GENERIC_TTL = "ttl";
    public static String NETWORK_TRAFFIC_GENERIC_VLAN_ID = "vlan_id";
    public static String NETWORK_TRAFFIC_GENERIC_VLAN_NAME = "vlan_name";
    public static String PACKET_FILTERING_ACTION = "action";
    public static String PACKET_FILTERING_DEST_PORT = "dest_port";
    public static String PACKET_FILTERING_DIRECTION = "direction";
    public static String PACKET_FILTERING_DVC = "dvc";
    public static String PACKET_FILTERING_RULE = "rule";
    public static String PACKET_FILTERING_SVC_PORT = "svc_port";
    public static String PROXY_ACTION = "action";
    public static String PROXY_DEST = "dest";
    public static String PROXY_HTTP_CONTENT_TYPE = "http_content_type";
    public static String PROXY_HTTP_METHOD = "http_method";
    public static String PROXY_HTTP_REFER = "http_refer";
    public static String PROXY_HTTP_RESPONSE = "http_response";
    public static String PROXY_HTTP_USER_AGENT = "http_user_agent";
    public static String PROXY_PRODUCT = "product";
    public static String PROXY_SRC = "src";
    public static String PROXY_STATUS = "status";
    public static String PROXY_USER = "user";
    public static String PROXY_URL = "url";
    public static String PROXY_VENDOR = "vendor";
    public static String SYSTEM_CENTER_APP = "app";
    public static String SYSTEM_CENTER_FREEMBYTES = "FreeMBytes";
    public static String SYSTEM_CENTER_KERNEL_RELEASE = "kernel_release";
    public static String SYSTEM_CENTER_LABEL = "label";
    public static String SYSTEM_CENTER_MOUNT = "mount";
    public static String SYSTEM_CENTER_OS = "os";
    public static String SYSTEM_CENTER_PERCENTPROCESSORTIME = "PercentProcessorTime";
    public static String SYSTEM_CENTER_SETLOCALDEFS = "setlocaldefs";
    public static String SYSTEM_CENTER_SELINUX = "selinux";
    public static String SYSTEM_CENTER_SELINUXTYPE = "selinuxtype";
    public static String SYSTEM_CENTER_SHELL = "shell";
    public static String SYSTEM_CENTER_SRC_PORT = "src_port";
    public static String SYSTEM_CENTER_SSHD_PROTOCOL = "sshd_protocol";
    public static String SYSTEM_CENTER_STARTMODE = "Startmode";
    public static String SYSTEM_CENTER_SYSTEMUPTIME = "SystemUptime";
    public static String SYSTEM_CENTER_TOTALMBYTES = "TotalMBytes";
    public static String SYSTEM_CENTER_USEDMBYTES = "UsedMBytes";
    public static String SYSTEM_CENTER_USER = "user";
    public static String SYSTEM_CENTER_UPDATES = "updates";
    public static String TRAFFIC_DEST = "dest";
    public static String TRAFFIC_DVC = "dvc";
    public static String TRAFFIC_SRC = "src";
    public static String UPDATE_PACKAGE = "package";
    public static String USER_INFO_UPDATES_AFFECTED_USER = "affected_user";
    public static String USER_INFO_UPDATES_AFFECTED_USER_GROUP = "affected_user_group";
    public static String USER_INFO_UPDATES_AFFECTED_USER_GROUP_ID = "affected_user_group_id";
    public static String USER_INFO_UPDATES_AFFECTED_USER_ID = "affected_user_id";
    public static String USER_INFO_UPDATES_AFFECTED_USER_PRIVILEGE = "affected_user_privilege";
    public static String USER_INFO_UPDATES_USER = "user";
    public static String USER_INFO_UPDATES_USER_GROUP = "user_group";
    public static String USER_INFO_UPDATES_USER_GROUP_ID = "user_group_id";
    public static String USER_INFO_UPDATES_USER_ID = "user_id";
    public static String USER_INFO_UPDATES_USER_PRIVILEGE = "user_privilege";
    public static String USER_INFO_UPDATES_USER_SUBJECT = "user_subject";
    public static String USER_INFO_UPDATES_USER_SUBJECT_ID = "user_subject_id";
    public static String USER_INFO_UPDATES_USER_SUBJECT_PRIVILEGE = "user_subject_privilege";
    public static String VULNERABILITY_CATEGORY = "category";
    public static String VULNERABILITY_DEST = "dest";
    public static String VULNERABILITY_OS = "os";
    public static String VULNERABILITY_SEVERITY = "severity";
    public static String VULNERABILITY_SIGNATURE = "signature";
    public static String WINDOWS_ADMIN_OBJECT_NAME = "object_name";
    public static String WINDOWS_ADMIN_OBJECT_TYPE = "object_type";
    public static String WINDOWS_ADMIN_OBJECT_HANDLE = "object_handle";

    public static void main(String[] args) {
        FormatTest event = new FormatTest("Failed Login", "sshd:failure");
        event.setAuthApp("jane");
        event.setAuthUser("jane");
        event.addPair("somefieldname", "foobar");
        event.addPair("somefieldname2", "foobar2");
        event.addPair("somefieldname3", "foobar3");
        event.addPair("somefieldname4", "foobar4");
        System.out.println(event.toString());
    }

    public FormatTest(String eventName, String eventID, boolean useInternalDate, boolean quoteValues) {
        this.quoteValues = true;
        this.useInternalDate = true;
        this.eventMessage = new StringBuffer();
        this.quoteValues = quoteValues;
        this.useInternalDate = useInternalDate;
        this.addPair("name", eventName);
        this.addPair("event_id", eventID);
    }

    public FormatTest(String eventName, String eventID) {
        this(eventName, eventID, true, true);
    }

    public FormatTest() {
        this.quoteValues = true;
        this.useInternalDate = true;
        this.eventMessage = new StringBuffer();
    }

    public FormatTest clone() {
        FormatTest clone = new FormatTest();
        clone.quoteValues = this.quoteValues;
        clone.useInternalDate = this.useInternalDate;
        clone.eventMessage.append(this.eventMessage);
        return clone;
    }

    public void addPair(String key, char value) {
        this.addPair(key, String.valueOf(value));
    }

    public void addPair(String key, boolean value) {
        this.addPair(key, String.valueOf(value));
    }

    public void addPair(String key, double value) {
        this.addPair(key, String.valueOf(value));
    }

    public void addPair(String key, long value) {
        this.addPair(key, String.valueOf(value));
    }

    public void addPair(String key, int value) {
        this.addPair(key, String.valueOf(value));
    }

    public void addPair(String key, Object value) {
        this.addPair(key, value.toString());
    }

    public void addThrowable(Throwable throwable) {
        this.addThrowableObject(throwable, -1);
    }

    public void addThrowable(Throwable throwable, int stackTraceDepth) {
        this.addThrowableObject(throwable, stackTraceDepth);
    }

    private void addThrowableObject(Throwable throwable, int stackTraceDepth) {
        this.addPair("throwable_class", throwable.getClass().getCanonicalName());
        this.addPair("throwable_message", throwable.getMessage());
        StackTraceElement[] elements = throwable.getStackTrace();
        StringBuffer sb = new StringBuffer();
        int depth = 0;
        StackTraceElement[] arr$ = elements;
        int len$ = elements.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            StackTraceElement element = arr$[i$];
            ++depth;
            if(stackTraceDepth != -1 && stackTraceDepth < depth) {
                break;
            }

            sb.append(element.toString()).append(",");
        }

        this.addPair("stacktrace_elements", sb.toString());
    }

    public void addPair(String key, String value) {
        if(this.quoteValues) {
            this.eventMessage.append(key).append("=").append('\"').append(value).append('\"').append(" ");
        } else {
            this.eventMessage.append(key).append("=").append(value).append(" ");
        }

    }

    public String toString() {
        String event = "";
        if(this.useInternalDate) {
            StringBuffer clonedMessage = new StringBuffer();
            clonedMessage.append(System.currentTimeMillis()).append(" ").append(this.eventMessage);
            event = clonedMessage.toString();
        } else {
            event = this.eventMessage.toString();
        }

        return event.substring(0, event.length() - " ".length());
    }

    public void setAcManagementDestNtDomain(String acManagementDestNtDomain) {
        this.addPair(AC_MANAGEMENT_DEST_NT_DOMAIN, acManagementDestNtDomain);
    }

    public void setAcManagementSignature(String acManagementSignature) {
        this.addPair(AC_MANAGEMENT_SIGNATURE, acManagementSignature);
    }

    public void setAcManagementSrcNtDomain(String acManagementSrcNtDomain) {
        this.addPair(AC_MANAGEMENT_SRC_NT_DOMAIN, acManagementSrcNtDomain);
    }

    public void setAuthAction(String authAction) {
        this.addPair(AUTH_ACTION, authAction);
    }

    public void setAuthApp(String authApp) {
        this.addPair(AUTH_APP, authApp);
    }

    public void setAuthDest(String authDest) {
        this.addPair(AUTH_DEST, authDest);
    }

    public void setAuthSrc(String authSrc) {
        this.addPair(AUTH_SRC, authSrc);
    }

    public void setAuthSrcUser(String authSrcUser) {
        this.addPair(AUTH_SRC_USER, authSrcUser);
    }

    public void setAuthUser(String authUser) {
        this.addPair(AUTH_USER, authUser);
    }

    public void setChangeEndpointProtectionAction(String changeEndpointProtectionAction) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_ACTION, changeEndpointProtectionAction);
    }

    public void setChangeEndpointProtectionChangeType(String changeEndpointProtectionChangeType) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_CHANGE_TYPE, changeEndpointProtectionChangeType);
    }

    public void setChangeEndpointProtectionDest(String changeEndpointProtectionDest) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_DEST, changeEndpointProtectionDest);
    }

    public void setChangeEndpointProtectionHash(String changeEndpointProtectionHash) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_HASH, changeEndpointProtectionHash);
    }

    public void setChangeEndpointProtectionGid(long changeEndpointProtectionGid) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_GID, changeEndpointProtectionGid);
    }

    public void setChangeEndpointProtectionIsdr(boolean changeEndpointProtectionIsdr) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_ISDR, changeEndpointProtectionIsdr);
    }

    public void setChangeEndpointProtectionMode(long changeEndpointProtectionMode) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_MODE, changeEndpointProtectionMode);
    }

    public void setChangeEndpointProtectionModtime(String changeEndpointProtectionModtime) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_MODTIME, changeEndpointProtectionModtime);
    }

    public void setChangeEndpointProtectionPath(String changeEndpointProtectionPath) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_PATH, changeEndpointProtectionPath);
    }

    public void setChangeEndpointProtectionSize(long changeEndpointProtectionSize) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_SIZE, changeEndpointProtectionSize);
    }

    public void setChangeEndpointProtectionUid(long changeEndpointProtectionUid) {
        this.addPair(CHANGE_ENDPOINT_PROTECTION_UID, changeEndpointProtectionUid);
    }

    public void setChangeNetworkProtectionAction(String changeNetworkProtectionAction) {
        this.addPair(CHANGE_NETWORK_PROTECTION_ACTION, changeNetworkProtectionAction);
    }

    public void setChangeNetworkProtectionCommand(String changeNetworkProtectionCommand) {
        this.addPair(CHANGE_NETWORK_PROTECTION_COMMAND, changeNetworkProtectionCommand);
    }

    public void setChangeNetworkProtectionDvc(String changeNetworkProtectionDvc) {
        this.addPair(CHANGE_NETWORK_PROTECTION_DVC, changeNetworkProtectionDvc);
    }

    public void setChangeNetworkProtectionUser(String changeNetworkProtectionUser) {
        this.addPair(CHANGE_NETWORK_PROTECTION_USER, changeNetworkProtectionUser);
    }

    public void setCommonCategory(String commonCategory) {
        this.addPair(COMMON_CATEGORY, commonCategory);
    }

    public void setCommonCount(String commonCount) {
        this.addPair(COMMON_COUNT, commonCount);
    }

    public void setCommonDesc(String commonDesc) {
        this.addPair(COMMON_DESC, commonDesc);
    }

    public void setCommonDhcpPool(String commonDhcpPool) {
        this.addPair(COMMON_DHCP_POOL, commonDhcpPool);
    }

    public void setCommonDuration(long commonDuration) {
        this.addPair(COMMON_DURATION, commonDuration);
    }

    public void setCommonDvcHost(String commonDvcHost) {
        this.addPair(COMMON_DVC_HOST, commonDvcHost);
    }

    public void setCommonDvcIp(String commonDvcIp) {
        this.addPair(COMMON_DVC_IP, commonDvcIp);
    }

    public void setCommonDvcIp6(String commonDvcIp6) {
        this.addPair(COMMON_DVC_IP6, commonDvcIp6);
    }

    public void setCommonDvcLocation(String commonDvcLocation) {
        this.addPair(COMMON_DVC_LOCATION, commonDvcLocation);
    }

    public void setCommonDvcMac(String commonDvcMac) {
        this.addPair(COMMON_DVC_MAC, commonDvcMac);
    }

    public void setCommonDvcNtDomain(String commonDvcNtDomain) {
        this.addPair(COMMON_DVC_NT_DOMAIN, commonDvcNtDomain);
    }

    public void setCommonDvcNtHost(String commonDvcNtHost) {
        this.addPair(COMMON_DVC_NT_HOST, commonDvcNtHost);
    }

    public void setCommonDvcTime(long commonDvcTime) {
        this.addPair(COMMON_DVC_TIME, commonDvcTime);
    }

    public void setCommonEndTime(long commonEndTime) {
        this.addPair(COMMON_END_TIME, commonEndTime);
    }

    public void setCommonEventId(long commonEventId) {
        this.addPair(COMMON_EVENT_ID, commonEventId);
    }

    public void setCommonLength(long commonLength) {
        this.addPair(COMMON_LENGTH, commonLength);
    }

    public void setCommonLogLevel(String commonLogLevel) {
        this.addPair(COMMON_LOG_LEVEL, commonLogLevel);
    }

    public void setCommonName(String commonName) {
        this.addPair(COMMON_NAME, commonName);
    }

    public void setCommonPid(long commonPid) {
        this.addPair(COMMON_PID, commonPid);
    }

    public void setCommonPriority(long commonPriority) {
        this.addPair(COMMON_PRIORITY, commonPriority);
    }

    public void setCommonProduct(String commonProduct) {
        this.addPair(COMMON_PRODUCT, commonProduct);
    }

    public void setCommonProductVersion(long commonProductVersion) {
        this.addPair(COMMON_PRODUCT_VERSION, commonProductVersion);
    }

    public void setCommonReason(String commonReason) {
        this.addPair(COMMON_REASON, commonReason);
    }

    public void setCommonResult(String commonResult) {
        this.addPair(COMMON_RESULT, commonResult);
    }

    public void setCommonSeverity(String commonSeverity) {
        this.addPair(COMMON_SEVERITY, commonSeverity);
    }

    public void setCommonStartTime(long commonStartTime) {
        this.addPair(COMMON_START_TIME, commonStartTime);
    }

    public void setCommonTransactionId(String commonTransactionId) {
        this.addPair(COMMON_TRANSACTION_ID, commonTransactionId);
    }

    public void setCommonUrl(String commonUrl) {
        this.addPair(COMMON_URL, commonUrl);
    }

    public void setCommonVendor(String commonVendor) {
        this.addPair(COMMON_VENDOR, commonVendor);
    }

    public void setDnsDestDomain(String dnsDestDomain) {
        this.addPair(DNS_DEST_DOMAIN, dnsDestDomain);
    }

    public void setDnsDestRecord(String dnsDestRecord) {
        this.addPair(DNS_DEST_RECORD, dnsDestRecord);
    }

    public void setDnsDestZone(String dnsDestZone) {
        this.addPair(DNS_DEST_ZONE, dnsDestZone);
    }

    public void setDnsRecordClass(String dnsRecordClass) {
        this.addPair(DNS_RECORD_CLASS, dnsRecordClass);
    }

    public void setDnsRecordType(String dnsRecordType) {
        this.addPair(DNS_RECORD_TYPE, dnsRecordType);
    }

    public void setDnsSrcDomain(String dnsSrcDomain) {
        this.addPair(DNS_SRC_DOMAIN, dnsSrcDomain);
    }

    public void setDnsSrcRecord(String dnsSrcRecord) {
        this.addPair(DNS_SRC_RECORD, dnsSrcRecord);
    }

    public void setDnsSrcZone(String dnsSrcZone) {
        this.addPair(DNS_SRC_ZONE, dnsSrcZone);
    }

    public void setEmailRecipient(String emailRecipient) {
        this.addPair(EMAIL_RECIPIENT, emailRecipient);
    }

    public void setEmailSender(String emailSender) {
        this.addPair(EMAIL_SENDER, emailSender);
    }

    public void setEmailSubject(String emailSubject) {
        this.addPair(EMAIL_SUBJECT, emailSubject);
    }

    public void setFileAccessTime(long fileAccessTime) {
        this.addPair(FILE_ACCESS_TIME, fileAccessTime);
    }

    public void setFileCreateTime(long fileCreateTime) {
        this.addPair(FILE_CREATE_TIME, fileCreateTime);
    }

    public void setFileHash(String fileHash) {
        this.addPair(FILE_HASH, fileHash);
    }

    public void setFileModifyTime(long fileModifyTime) {
        this.addPair(FILE_MODIFY_TIME, fileModifyTime);
    }

    public void setFileName(String fileName) {
        this.addPair(FILE_NAME, fileName);
    }

    public void setFilePath(String filePath) {
        this.addPair(FILE_PATH, filePath);
    }

    public void setFilePermission(String filePermission) {
        this.addPair(FILE_PERMISSION, filePermission);
    }

    public void setFileSize(long fileSize) {
        this.addPair(FILE_SIZE, fileSize);
    }

    public void setIntrusionDetectionCategory(String intrusionDetectionCategory) {
        this.addPair(INTRUSION_DETECTION_CATEGORY, intrusionDetectionCategory);
    }

    public void setIntrusionDetectionDest(String intrusionDetectionDest) {
        this.addPair(INTRUSION_DETECTION_DEST, intrusionDetectionDest);
    }

    public void setIntrusionDetectionDvc(String intrusionDetectionDvc) {
        this.addPair(INTRUSION_DETECTION_DVC, intrusionDetectionDvc);
    }

    public void setIntrusionDetectionIdsType(String intrusionDetectionIdsType) {
        this.addPair(INTRUSION_DETECTION_IDS_TYPE, intrusionDetectionIdsType);
    }

    public void setIntrusionDetectionProduct(String intrusionDetectionProduct) {
        this.addPair(INTRUSION_DETECTION_PRODUCT, intrusionDetectionProduct);
    }

    public void setIntrusionDetectionSeverity(String intrusionDetectionSeverity) {
        this.addPair(INTRUSION_DETECTION_SEVERITY, intrusionDetectionSeverity);
    }

    public void setIntrusionDetectionSignature(String intrusionDetectionSignature) {
        this.addPair(INTRUSION_DETECTION_SIGNATURE, intrusionDetectionSignature);
    }

    public void setIntrusionDetectionSrc(String intrusionDetectionSrc) {
        this.addPair(INTRUSION_DETECTION_SRC, intrusionDetectionSrc);
    }

    public void setIntrusionDetectionUser(String intrusionDetectionUser) {
        this.addPair(INTRUSION_DETECTION_USER, intrusionDetectionUser);
    }

    public void setIntrusionDetectionVendor(String intrusionDetectionVendor) {
        this.addPair(INTRUSION_DETECTION_VENDOR, intrusionDetectionVendor);
    }

    public void setMalwareEndpointProtectionAction(String malwareEndpointProtectionAction) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_ACTION, malwareEndpointProtectionAction);
    }

    public void setMalwareEndpointProtectionDestNtDomain(String malwareEndpointProtectionDestNtDomain) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_DEST_NT_DOMAIN, malwareEndpointProtectionDestNtDomain);
    }

    public void setMalwareEndpointProtectionFileHash(String malwareEndpointProtectionFileHash) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_FILE_HASH, malwareEndpointProtectionFileHash);
    }

    public void setMalwareEndpointProtectionFileName(String malwareEndpointProtectionFileName) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_FILE_NAME, malwareEndpointProtectionFileName);
    }

    public void setMalwareEndpointProtectionFilePath(String malwareEndpointProtectionFilePath) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_FILE_PATH, malwareEndpointProtectionFilePath);
    }

    public void setMalwareEndpointProtectionProduct(String malwareEndpointProtectionProduct) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_PRODUCT, malwareEndpointProtectionProduct);
    }

    public void setMalwareEndpointProtectionProductVersion(String malwareEndpointProtectionProductVersion) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_PRODUCT_VERSION, malwareEndpointProtectionProductVersion);
    }

    public void setMalwareEndpointProtectionSignature(String malwareEndpointProtectionSignature) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_SIGNATURE, malwareEndpointProtectionSignature);
    }

    public void setMalwareEndpointProtectionSignatureVersion(String malwareEndpointProtectionSignatureVersion) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_SIGNATURE_VERSION, malwareEndpointProtectionSignatureVersion);
    }

    public void setMalwareEndpointProtectionDest(String malwareEndpointProtectionDest) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_DEST, malwareEndpointProtectionDest);
    }

    public void setMalwareEndpointProtectionSrcNtDomain(String malwareEndpointProtectionSrcNtDomain) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_SRC_NT_DOMAIN, malwareEndpointProtectionSrcNtDomain);
    }

    public void setMalwareEndpointProtectionUser(String malwareEndpointProtectionUser) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_USER, malwareEndpointProtectionUser);
    }

    public void setMalwareEndpointProtectionVendor(String malwareEndpointProtectionVendor) {
        this.addPair(MALWARE_ENDPOINT_PROTECTION_VENDOR, malwareEndpointProtectionVendor);
    }

    public void setMalwareNetworkProtectionProduct(String malwareNetworkProtectionProduct) {
        this.addPair(MALWARE_NETWORK_PROTECTION_PRODUCT, malwareNetworkProtectionProduct);
    }

    public void setMalwareNetworkProtectionSeverity(String malwareNetworkProtectionSeverity) {
        this.addPair(MALWARE_NETWORK_PROTECTION_SEVERITY, malwareNetworkProtectionSeverity);
    }

    public void setMalwareNetworkProtectionVendor(String malwareNetworkProtectionVendor) {
        this.addPair(MALWARE_NETWORK_PROTECTION_VENDOR, malwareNetworkProtectionVendor);
    }

    public void setNetworkTrafficEssAction(String networkTrafficEssAction) {
        this.addPair(NETWORK_TRAFFIC_ESS_ACTION, networkTrafficEssAction);
    }

    public void setNetworkTrafficEssDestPort(int networkTrafficEssDestPort) {
        this.addPair(NETWORK_TRAFFIC_ESS_DEST_PORT, networkTrafficEssDestPort);
    }

    public void setNetworkTrafficEssProduct(String networkTrafficEssProduct) {
        this.addPair(NETWORK_TRAFFIC_ESS_PRODUCT, networkTrafficEssProduct);
    }

    public void setNetworkTrafficEssSrcPort(int networkTrafficEssSrcPort) {
        this.addPair(NETWORK_TRAFFIC_ESS_SRC_PORT, networkTrafficEssSrcPort);
    }

    public void setNetworkTrafficEssVendor(String networkTrafficEssVendor) {
        this.addPair(NETWORK_TRAFFIC_ESS_VENDOR, networkTrafficEssVendor);
    }

    public void setNetworkTrafficGenericAppLayer(String networkTrafficGenericAppLayer) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_APP_LAYER, networkTrafficGenericAppLayer);
    }

    public void setNetworkTrafficGenericBytesIn(long networkTrafficGenericBytesIn) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_BYTES_IN, networkTrafficGenericBytesIn);
    }

    public void setNetworkTrafficGenericBytesOut(long networkTrafficGenericBytesOut) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_BYTES_OUT, networkTrafficGenericBytesOut);
    }

    public void setNetworkTrafficGenericChannel(String networkTrafficGenericChannel) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_CHANNEL, networkTrafficGenericChannel);
    }

    public void setNetworkTrafficGenericCve(String networkTrafficGenericCve) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_CVE, networkTrafficGenericCve);
    }

    public void setNetworkTrafficGenericDestApp(String networkTrafficGenericDestApp) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_APP, networkTrafficGenericDestApp);
    }

    public void setNetworkTrafficGenericDestCncChannel(String networkTrafficGenericDestCncChannel) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_CNC_CHANNEL, networkTrafficGenericDestCncChannel);
    }

    public void setNetworkTrafficGenericDestCncName(String networkTrafficGenericDestCncName) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_CNC_NAME, networkTrafficGenericDestCncName);
    }

    public void setNetworkTrafficGenericDestCncPort(String networkTrafficGenericDestCncPort) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_CNC_PORT, networkTrafficGenericDestCncPort);
    }

    public void setNetworkTrafficGenericDestCountry(String networkTrafficGenericDestCountry) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_COUNTRY, networkTrafficGenericDestCountry);
    }

    public void setNetworkTrafficGenericDestHost(String networkTrafficGenericDestHost) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_HOST, networkTrafficGenericDestHost);
    }

    public void setNetworkTrafficGenericDestInt(String networkTrafficGenericDestInt) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_INT, networkTrafficGenericDestInt);
    }

    public void setNetworkTrafficGenericDestIp(String networkTrafficGenericDestIp) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_IP, networkTrafficGenericDestIp);
    }

    public void setNetworkTrafficGenericDestIpv6(String networkTrafficGenericDestIpv6) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_IPV6, networkTrafficGenericDestIpv6);
    }

    public void setNetworkTrafficGenericDestLat(int networkTrafficGenericDestLat) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_LAT, networkTrafficGenericDestLat);
    }

    public void setNetworkTrafficGenericDestLong(int networkTrafficGenericDestLong) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_LONG, networkTrafficGenericDestLong);
    }

    public void setNetworkTrafficGenericDestMac(String networkTrafficGenericDestMac) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_MAC, networkTrafficGenericDestMac);
    }

    public void setNetworkTrafficGenericDestNtDomain(String networkTrafficGenericDestNtDomain) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_NT_DOMAIN, networkTrafficGenericDestNtDomain);
    }

    public void setNetworkTrafficGenericDestNtHost(String networkTrafficGenericDestNtHost) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_NT_HOST, networkTrafficGenericDestNtHost);
    }

    public void setNetworkTrafficGenericDestPort(int networkTrafficGenericDestPort) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_PORT, networkTrafficGenericDestPort);
    }

    public void setNetworkTrafficGenericDestTranslatedIp(String networkTrafficGenericDestTranslatedIp) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_IP, networkTrafficGenericDestTranslatedIp);
    }

    public void setNetworkTrafficGenericDestTranslatedPort(int networkTrafficGenericDestTranslatedPort) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_DEST_TRANSLATED_PORT, networkTrafficGenericDestTranslatedPort);
    }

    public void setNetworkTrafficGenericIpVersion(int networkTrafficGenericIpVersion) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_IP_VERSION, networkTrafficGenericIpVersion);
    }

    public void setNetworkTrafficGenericOutboundInterface(String networkTrafficGenericOutboundInterface) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_OUTBOUND_INTERFACE, networkTrafficGenericOutboundInterface);
    }

    public void setNetworkTrafficGenericPacketsIn(long networkTrafficGenericPacketsIn) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_PACKETS_IN, networkTrafficGenericPacketsIn);
    }

    public void setNetworkTrafficGenericPacketsOut(long networkTrafficGenericPacketsOut) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_PACKETS_OUT, networkTrafficGenericPacketsOut);
    }

    public void setNetworkTrafficGenericProto(String networkTrafficGenericProto) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_PROTO, networkTrafficGenericProto);
    }

    public void setNetworkTrafficGenericSessionId(String networkTrafficGenericSessionId) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SESSION_ID, networkTrafficGenericSessionId);
    }

    public void setNetworkTrafficGenericSsid(String networkTrafficGenericSsid) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SSID, networkTrafficGenericSsid);
    }

    public void setNetworkTrafficGenericSrcCountry(String networkTrafficGenericSrcCountry) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_COUNTRY, networkTrafficGenericSrcCountry);
    }

    public void setNetworkTrafficGenericSrcHost(String networkTrafficGenericSrcHost) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_HOST, networkTrafficGenericSrcHost);
    }

    public void setNetworkTrafficGenericSrcInt(String networkTrafficGenericSrcInt) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_INT, networkTrafficGenericSrcInt);
    }

    public void setNetworkTrafficGenericSrcIp(String networkTrafficGenericSrcIp) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_IP, networkTrafficGenericSrcIp);
    }

    public void setNetworkTrafficGenericSrcIpv6(String networkTrafficGenericSrcIpv6) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_IPV6, networkTrafficGenericSrcIpv6);
    }

    public void setNetworkTrafficGenericSrcLat(int networkTrafficGenericSrcLat) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_LAT, networkTrafficGenericSrcLat);
    }

    public void setNetworkTrafficGenericSrcLong(int networkTrafficGenericSrcLong) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_LONG, networkTrafficGenericSrcLong);
    }

    public void setNetworkTrafficGenericSrcMac(String networkTrafficGenericSrcMac) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_MAC, networkTrafficGenericSrcMac);
    }

    public void setNetworkTrafficGenericSrcNtDomain(String networkTrafficGenericSrcNtDomain) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_NT_DOMAIN, networkTrafficGenericSrcNtDomain);
    }

    public void setNetworkTrafficGenericSrcNtHost(String networkTrafficGenericSrcNtHost) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_NT_HOST, networkTrafficGenericSrcNtHost);
    }

    public void setNetworkTrafficGenericSrcPort(int networkTrafficGenericSrcPort) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_PORT, networkTrafficGenericSrcPort);
    }

    public void setNetworkTrafficGenericSrcTranslatedIp(String networkTrafficGenericSrcTranslatedIp) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_IP, networkTrafficGenericSrcTranslatedIp);
    }

    public void setNetworkTrafficGenericSrcTranslatedPort(int networkTrafficGenericSrcTranslatedPort) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SRC_TRANSLATED_PORT, networkTrafficGenericSrcTranslatedPort);
    }

    public void setNetworkTrafficGenericSyslogId(String networkTrafficGenericSyslogId) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SYSLOG_ID, networkTrafficGenericSyslogId);
    }

    public void setNetworkTrafficGenericSyslogPriority(String networkTrafficGenericSyslogPriority) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_SYSLOG_PRIORITY, networkTrafficGenericSyslogPriority);
    }

    public void setNetworkTrafficGenericTcpFlag(String networkTrafficGenericTcpFlag) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_TCP_FLAG, networkTrafficGenericTcpFlag);
    }

    public void setNetworkTrafficGenericTos(String networkTrafficGenericTos) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_TOS, networkTrafficGenericTos);
    }

    public void setNetworkTrafficGenericTransport(String networkTrafficGenericTransport) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_TRANSPORT, networkTrafficGenericTransport);
    }

    public void setNetworkTrafficGenericTtl(int networkTrafficGenericTtl) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_TTL, networkTrafficGenericTtl);
    }

    public void setNetworkTrafficGenericVlanId(long networkTrafficGenericVlanId) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_VLAN_ID, networkTrafficGenericVlanId);
    }

    public void setNetworkTrafficGenericVlanName(String networkTrafficGenericVlanName) {
        this.addPair(NETWORK_TRAFFIC_GENERIC_VLAN_NAME, networkTrafficGenericVlanName);
    }

    public void setPacketFilteringAction(String packetFilteringAction) {
        this.addPair(PACKET_FILTERING_ACTION, packetFilteringAction);
    }

    public void setPacketFilteringDestPort(int packetFilteringDestPort) {
        this.addPair(PACKET_FILTERING_DEST_PORT, packetFilteringDestPort);
    }

    public void setPacketFilteringDirection(String packetFilteringDirection) {
        this.addPair(PACKET_FILTERING_DIRECTION, packetFilteringDirection);
    }

    public void setPacketFilteringDvc(String packetFilteringDvc) {
        this.addPair(PACKET_FILTERING_DVC, packetFilteringDvc);
    }

    public void setPacketFilteringRule(String packetFilteringRule) {
        this.addPair(PACKET_FILTERING_RULE, packetFilteringRule);
    }

    public void setPacketFilteringSvcPort(int packetFilteringSvcPort) {
        this.addPair(PACKET_FILTERING_SVC_PORT, packetFilteringSvcPort);
    }

    public void setProxyAction(String proxyAction) {
        this.addPair(PROXY_ACTION, proxyAction);
    }

    public void setProxyDest(String proxyDest) {
        this.addPair(PROXY_DEST, proxyDest);
    }

    public void setProxyHttpContentType(String proxyHttpContentType) {
        this.addPair(PROXY_HTTP_CONTENT_TYPE, proxyHttpContentType);
    }

    public void setProxyHttpMethod(String proxyHttpMethod) {
        this.addPair(PROXY_HTTP_METHOD, proxyHttpMethod);
    }

    public void setProxyHttpRefer(String proxyHttpRefer) {
        this.addPair(PROXY_HTTP_REFER, proxyHttpRefer);
    }

    public void setProxyHttpResponse(int proxyHttpResponse) {
        this.addPair(PROXY_HTTP_RESPONSE, proxyHttpResponse);
    }

    public void setProxyHttpUserAgent(String proxyHttpUserAgent) {
        this.addPair(PROXY_HTTP_USER_AGENT, proxyHttpUserAgent);
    }

    public void setProxyProduct(String proxyProduct) {
        this.addPair(PROXY_PRODUCT, proxyProduct);
    }

    public void setProxySrc(String proxySrc) {
        this.addPair(PROXY_SRC, proxySrc);
    }

    public void setProxyStatus(int proxyStatus) {
        this.addPair(PROXY_STATUS, proxyStatus);
    }

    public void setProxyUser(String proxyUser) {
        this.addPair(PROXY_USER, proxyUser);
    }

    public void setProxyUrl(String proxyUrl) {
        this.addPair(PROXY_URL, proxyUrl);
    }

    public void setProxyVendor(String proxyVendor) {
        this.addPair(PROXY_VENDOR, proxyVendor);
    }

    public void setSystemCenterApp(String systemCenterApp) {
        this.addPair(SYSTEM_CENTER_APP, systemCenterApp);
    }

    public void setSystemCenterFreembytes(long systemCenterFreembytes) {
        this.addPair(SYSTEM_CENTER_FREEMBYTES, systemCenterFreembytes);
    }

    public void setSystemCenterKernelRelease(String systemCenterKernelRelease) {
        this.addPair(SYSTEM_CENTER_KERNEL_RELEASE, systemCenterKernelRelease);
    }

    public void setSystemCenterLabel(String systemCenterLabel) {
        this.addPair(SYSTEM_CENTER_LABEL, systemCenterLabel);
    }

    public void setSystemCenterMount(String systemCenterMount) {
        this.addPair(SYSTEM_CENTER_MOUNT, systemCenterMount);
    }

    public void setSystemCenterOs(String systemCenterOs) {
        this.addPair(SYSTEM_CENTER_OS, systemCenterOs);
    }

    public void setSystemCenterPercentprocessortime(int systemCenterPercentprocessortime) {
        this.addPair(SYSTEM_CENTER_PERCENTPROCESSORTIME, systemCenterPercentprocessortime);
    }

    public void setSystemCenterSetlocaldefs(int systemCenterSetlocaldefs) {
        this.addPair(SYSTEM_CENTER_SETLOCALDEFS, systemCenterSetlocaldefs);
    }

    public void setSystemCenterSelinux(String systemCenterSelinux) {
        this.addPair(SYSTEM_CENTER_SELINUX, systemCenterSelinux);
    }

    public void setSystemCenterSelinuxtype(String systemCenterSelinuxtype) {
        this.addPair(SYSTEM_CENTER_SELINUXTYPE, systemCenterSelinuxtype);
    }

    public void setSystemCenterShell(String systemCenterShell) {
        this.addPair(SYSTEM_CENTER_SHELL, systemCenterShell);
    }

    public void setSystemCenterSrcPort(int systemCenterSrcPort) {
        this.addPair(SYSTEM_CENTER_SRC_PORT, systemCenterSrcPort);
    }

    public void setSystemCenterSshdProtocol(String systemCenterSshdProtocol) {
        this.addPair(SYSTEM_CENTER_SSHD_PROTOCOL, systemCenterSshdProtocol);
    }

    public void setSystemCenterStartmode(String systemCenterStartmode) {
        this.addPair(SYSTEM_CENTER_STARTMODE, systemCenterStartmode);
    }

    public void setSystemCenterSystemuptime(long systemCenterSystemuptime) {
        this.addPair(SYSTEM_CENTER_SYSTEMUPTIME, systemCenterSystemuptime);
    }

    public void setSystemCenterTotalmbytes(long systemCenterTotalmbytes) {
        this.addPair(SYSTEM_CENTER_TOTALMBYTES, systemCenterTotalmbytes);
    }

    public void setSystemCenterUsedmbytes(long systemCenterUsedmbytes) {
        this.addPair(SYSTEM_CENTER_USEDMBYTES, systemCenterUsedmbytes);
    }

    public void setSystemCenterUser(String systemCenterUser) {
        this.addPair(SYSTEM_CENTER_USER, systemCenterUser);
    }

    public void setSystemCenterUpdates(long systemCenterUpdates) {
        this.addPair(SYSTEM_CENTER_UPDATES, systemCenterUpdates);
    }

    public void setTrafficDest(String trafficDest) {
        this.addPair(TRAFFIC_DEST, trafficDest);
    }

    public void setTrafficDvc(String trafficDvc) {
        this.addPair(TRAFFIC_DVC, trafficDvc);
    }

    public void setTrafficSrc(String trafficSrc) {
        this.addPair(TRAFFIC_SRC, trafficSrc);
    }

    public void setUpdatePackage(String updatePackage) {
        this.addPair(UPDATE_PACKAGE, updatePackage);
    }

    public void setUserInfoUpdatesAffectedUser(String userInfoUpdatesAffectedUser) {
        this.addPair(USER_INFO_UPDATES_AFFECTED_USER, userInfoUpdatesAffectedUser);
    }

    public void setUserInfoUpdatesAffectedUserGroup(String userInfoUpdatesAffectedUserGroup) {
        this.addPair(USER_INFO_UPDATES_AFFECTED_USER_GROUP, userInfoUpdatesAffectedUserGroup);
    }

    public void setUserInfoUpdatesAffectedUserGroupId(int userInfoUpdatesAffectedUserGroupId) {
        this.addPair(USER_INFO_UPDATES_AFFECTED_USER_GROUP_ID, userInfoUpdatesAffectedUserGroupId);
    }

    public void setUserInfoUpdatesAffectedUserId(int userInfoUpdatesAffectedUserId) {
        this.addPair(USER_INFO_UPDATES_AFFECTED_USER_ID, userInfoUpdatesAffectedUserId);
    }

    public void setUserInfoUpdatesAffectedUserPrivilege(String userInfoUpdatesAffectedUserPrivilege) {
        this.addPair(USER_INFO_UPDATES_AFFECTED_USER_PRIVILEGE, userInfoUpdatesAffectedUserPrivilege);
    }

    public void setUserInfoUpdatesUser(String userInfoUpdatesUser) {
        this.addPair(USER_INFO_UPDATES_USER, userInfoUpdatesUser);
    }

    public void setUserInfoUpdatesUserGroup(String userInfoUpdatesUserGroup) {
        this.addPair(USER_INFO_UPDATES_USER_GROUP, userInfoUpdatesUserGroup);
    }

    public void setUserInfoUpdatesUserGroupId(int userInfoUpdatesUserGroupId) {
        this.addPair(USER_INFO_UPDATES_USER_GROUP_ID, userInfoUpdatesUserGroupId);
    }

    public void setUserInfoUpdatesUserId(int userInfoUpdatesUserId) {
        this.addPair(USER_INFO_UPDATES_USER_ID, userInfoUpdatesUserId);
    }

    public void setUserInfoUpdatesUserPrivilege(String userInfoUpdatesUserPrivilege) {
        this.addPair(USER_INFO_UPDATES_USER_PRIVILEGE, userInfoUpdatesUserPrivilege);
    }

    public void setUserInfoUpdatesUserSubject(String userInfoUpdatesUserSubject) {
        this.addPair(USER_INFO_UPDATES_USER_SUBJECT, userInfoUpdatesUserSubject);
    }

    public void setUserInfoUpdatesUserSubjectId(int userInfoUpdatesUserSubjectId) {
        this.addPair(USER_INFO_UPDATES_USER_SUBJECT_ID, userInfoUpdatesUserSubjectId);
    }

    public void setUserInfoUpdatesUserSubjectPrivilege(String userInfoUpdatesUserSubjectPrivilege) {
        this.addPair(USER_INFO_UPDATES_USER_SUBJECT_PRIVILEGE, userInfoUpdatesUserSubjectPrivilege);
    }

    public void setVulnerabilityCategory(String vulnerabilityCategory) {
        this.addPair(VULNERABILITY_CATEGORY, vulnerabilityCategory);
    }

    public void setVulnerabilityDest(String vulnerabilityDest) {
        this.addPair(VULNERABILITY_DEST, vulnerabilityDest);
    }

    public void setVulnerabilityOs(String vulnerabilityOs) {
        this.addPair(VULNERABILITY_OS, vulnerabilityOs);
    }

    public void setVulnerabilitySeverity(String vulnerabilitySeverity) {
        this.addPair(VULNERABILITY_SEVERITY, vulnerabilitySeverity);
    }

    public void setVulnerabilitySignature(String vulnerabilitySignature) {
        this.addPair(VULNERABILITY_SIGNATURE, vulnerabilitySignature);
    }

    public void setWindowsAdminObjectName(String windowsAdminObjectName) {
        this.addPair(WINDOWS_ADMIN_OBJECT_NAME, windowsAdminObjectName);
    }

    public void setWindowsAdminObjectType(String windowsAdminObjectType) {
        this.addPair(WINDOWS_ADMIN_OBJECT_TYPE, windowsAdminObjectType);
    }

    public void setWindowsAdminObjectHandle(String windowsAdminObjectHandle) {
        this.addPair(WINDOWS_ADMIN_OBJECT_HANDLE, windowsAdminObjectHandle);
    }
}
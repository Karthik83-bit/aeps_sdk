package fingerprintmodel.wiseasyfpmodel;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import fingerprintmodel.uid.Data;
import fingerprintmodel.uid.Skey;

@Root(name = "PidData")
public class AratekPidData {

    @Element(name = "Resp", required = false)
    public AratekResp resp;

    @Element(name = "DeviceInfo", required = false)
    public AratekDeviceInfo deviceInfo;

    @Element(name = "Skey", required = false)
    public Skey skey;

    @Element(name = "Hmac", required = false)
    public String hmac;

    @Element(name = "Data", required = false)
    public Data data;

    public AratekPidData() {
    }
}

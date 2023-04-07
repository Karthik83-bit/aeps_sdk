package fingerprintmodel.wiseasyfpmodel;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "DeviceInfo")
public class AratekDeviceInfo {

    @Attribute(name = "dpId")
    public String dpld;

    @Attribute(name = "rdsId")
    public  String rdsld;

    @Attribute(name = "rdsVer")
    public  String rdsVer;

    @Attribute(name = "dc")
    public  String dc;

    @Attribute(name = "mi")
    public  String mi;

    @Attribute(name = "mc")
    public String mc;

  @Element(name = "additional_info")
    public Aratek_additional_info add_info;

    public AratekDeviceInfo() {
    }
}

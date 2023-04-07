package fingerprintmodel.wiseasyfpmodel;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Resp")
public class AratekResp {

    @Attribute(name = "errCode", required = false)
    public String errCode;

    @Attribute(name = "errInfo", required = false)
    public String errInfo;

    @Attribute(name = "fType", required = false)
    public String fType;

    @Attribute(name = "fCount", required = false)
    public String fCount;

    @Attribute(name = "format", required = false)
    public String format;

    @Attribute(name = "nmPoints", required = false)
    public String nmPoints;

    @Attribute(name = "qScore", required = false)
    public String qScore;

    public AratekResp() {
    }
}

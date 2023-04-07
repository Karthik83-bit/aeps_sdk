package fingerprintmodel.wiseasyfpmodel;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "additional_info")
public class Aratek_additional_info {

    @Element(name = "Param")
    public fingerprintmodel.Param Param;
}

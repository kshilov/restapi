package com.heymoose.resource.xml;

import com.heymoose.infrastructure.util.db.SqlLoader;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.Map;

public class XmlAffiliateBasic {
    @XmlAttribute
    public Long id;
    @XmlElement
    public String email;

    public XmlAffiliateBasic(Map<String, Object> action) {
      this.id = SqlLoader.extractLong(action.get("affiliate_id"));
      this.email = SqlLoader.extractString(action.get("affiliate_email"));
    }

    protected XmlAffiliateBasic() { }
}

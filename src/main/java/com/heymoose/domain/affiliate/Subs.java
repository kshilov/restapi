package com.heymoose.domain.affiliate;

import com.sun.jersey.api.client.WebResource;
import javax.annotation.Nullable;

/**
 * @author Serg Prasolov ci.serg@gmail.com
 * @since 6/5/12 6:37 PM
 */
public class Subs {
    private String sourceId;
    private String subId;
    private String subId1;
    private String subId2;
    private String subId3;
    private String subId4;

    public static Subs empty() {
        return new Subs(null, null, null, null, null, null);
    }

    public Subs(@Nullable String sourceId,
                @Nullable String subId, @Nullable String subId1, @Nullable String subId2,
                @Nullable String subId3, @Nullable String subId4) {
        this.sourceId = sourceId;
        this.subId = subId;
        this.subId1 = subId1;
        this.subId2 = subId2;
        this.subId3 = subId3;
        this.subId4 = subId4;
    }

    public String sourceId() {
        return sourceId;
    }

    public String subId() {
        return subId;
    }

    public String subId1() {
        return subId1;
    }

    public String subId2() {
        return subId2;
    }

    public String subId3() {
        return subId3;
    }

    public String subId4() {
        return subId4;
    }

    public WebResource addToQuery(WebResource wr) {
        if (sourceId != null) wr = wr.queryParam("source_id", sourceId);
        if (subId != null) wr = wr.queryParam("sub_id", subId);
        if (subId1 != null) wr = wr.queryParam("sub_id1", subId1);
        if (subId2 != null) wr = wr.queryParam("sub_id2", subId2);
        if (subId3 != null) wr = wr.queryParam("sub_id3", subId3);
        if (subId4 != null) wr = wr.queryParam("sub_id4", subId4);
        return wr;
    }
}

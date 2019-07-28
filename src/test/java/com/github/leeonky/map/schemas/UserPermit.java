package com.github.leeonky.map.schemas;

import com.github.leeonky.map.*;

import java.util.List;

@Permit(target = User.class, action = Create.class)
public class UserPermit {
    public String name;
    public AddressPermit address;
    public List<UserPermit> partners;
    public List<List<UserPermit>> neighbors;

    @PermitAction(Create.class)
    public List<IdPermit> ids;

    @SubPermitProperty("type")
    public abstract static class IdPermit {
        public String type;
    }

    @Permit(target = Void.class, action = Create.class)
    @SubPermitPropertyStringValue("PASSPORT")
    public static class PassportPermit extends IdPermit {
        public String name;
    }

    @Permit(target = Void.class, action = Create.class)
    @SubPermitPropertyStringValue("IDENTITY")
    public static class IdentityPermit extends IdPermit {
        public String number;
    }
}

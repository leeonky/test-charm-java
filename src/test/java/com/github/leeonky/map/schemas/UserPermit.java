package com.github.leeonky.map.schemas;

import com.github.leeonky.map.*;

import java.util.List;

@Permit(target = User.class, action = Create.class)
public class UserPermit {
    public String name;
    public AddressPermit address;
    public List<UserPermit> partners;
    public List<List<UserPermit>> neighbors;

    @ToProperty("name")
    public String nickName;

    @ToProperty("id.type")
    public String idType;

    @PermitAction(Create.class)
    public List<IdPermit> ids;

    @PermitAction(Create.class)
    public Object error1;

    @PermitAction(Create.class)
    public List error2;

    @PolymorphicPermitIdentity("type")
    public abstract static class IdPermit {
        public String type;
    }

    @Permit(target = Void.class, action = Create.class)
    @PolymorphicPermitIdentityString("PASSPORT")
    public static class PassportPermit extends IdPermit {
        public String name;
    }

    @Permit(target = Void.class, action = Create.class)
    @PolymorphicPermitIdentityString("IDENTITY")
    public static class IdentityPermit extends IdPermit {
        public String number;
    }

    @Permit(target = Void.class, action = Create.class, scope = NewScope.class)
    @PolymorphicPermitIdentityString("PASSPORT")
    public static class NewScopePassportPermit extends IdPermit {
        public int age;
    }
}

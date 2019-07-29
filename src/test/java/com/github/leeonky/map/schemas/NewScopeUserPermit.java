package com.github.leeonky.map.schemas;

import com.github.leeonky.map.Create;
import com.github.leeonky.map.Permit;
import com.github.leeonky.map.PermitAction;

import java.util.List;

@Permit(target = User.class, action = Create.class, scope = NewScope.class)
public class NewScopeUserPermit {
    public int age;

    @PermitAction(Create.class)
    public List<UserPermit.IdPermit> ids;
}

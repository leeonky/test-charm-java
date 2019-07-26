package com.github.leeonky.map.schemas;

import com.github.leeonky.map.Create;

import java.util.List;

@Permit(target = User.class, action = Create.class)
public class UserPermit {
    public String name;
    public AddressPermit address;
    public List<UserPermit> partners;
}

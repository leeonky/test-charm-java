package com.github.leeonky.map.schemas;

import com.github.leeonky.map.Create;

@Permit(target = User.class, action = Create.class)
public class UserPermit {
    public String name;
}

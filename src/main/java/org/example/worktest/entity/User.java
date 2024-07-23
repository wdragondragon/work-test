package org.example.worktest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.worktest.utils.jackson.CustomerJacksonSerializerEnum;
import org.example.worktest.utils.jackson.ExtStringHandler;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String username;

    @ExtStringHandler(type = CustomerJacksonSerializerEnum.BASE64, serializer = false)
    private String password;
}

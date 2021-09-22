package cn.edu.zjut.cart.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginInfoDTO {

    private Long userId;

    private String visitorId;

    /**
     * 是否初次访问
     */
    private Boolean firstVisit = false;
}

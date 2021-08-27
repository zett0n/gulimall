package cn.edu.zjut.common.validation;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
    private Set<Integer> set = new HashSet<>();

    // 将 vals 指定的值存入集合用于后续判断参数是否在 vals 中
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            this.set.add(val);
        }
    }

    // value 为需要校验的值
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return this.set.contains(value);
    }
}

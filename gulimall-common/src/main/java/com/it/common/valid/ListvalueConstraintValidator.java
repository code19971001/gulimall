package com.it.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * 自定义校验器
 *
 * @author : code1997
 * @date : 2021/5/3 22:20
 */
public class ListvalueConstraintValidator implements ConstraintValidator<ListValue, Integer> {

    private Set<Integer> dataSet = new HashSet<>();

    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            dataSet.add(val);
        }
    }

    /**
     * value：是需要被校验的值
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {

        return dataSet.contains(value);
    }
}

package cn.bug.chatgpt.data.domain.openai.annotation;

import cn.bug.chatgpt.data.domain.openai.service.rule.factory.DefaultLogicFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})//作用域：类、接口、枚举
@Retention(RetentionPolicy.RUNTIME)//注解的生命周期
public @interface LogicStrategy {

    DefaultLogicFactory.LogicModel logicMode();//属性 logicMode，类型是 DefaultLogicFactory.LogicModel。当使用这个注解时，必须提供 logicMode 的值

}

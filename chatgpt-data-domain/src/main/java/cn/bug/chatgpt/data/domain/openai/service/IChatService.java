package cn.bug.chatgpt.data.domain.openai.service;

import cn.bug.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 20:53
 */
public interface IChatService {

    ResponseBodyEmitter completions(ChatProcessAggregate chatProcess,HttpServletResponse response);

}

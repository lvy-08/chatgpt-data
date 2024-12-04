package cn.bug.chatgpt.data.domain.openai.service;

import cn.bug.chatgpt.data.domain.openai.model.aggregates.ChatProcessAggregate;
import cn.bug.chatgpt.data.types.common.Constants;
import cn.bug.chatgpt.data.types.exception.ChatGPTException;
import cn.bug.chatgpt.session.OpenAiSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description
 * @create 2023-07-22 21:12
 */
@Slf4j
public abstract class AbstractChatService implements IChatService {

    @Resource//自动注入名称为openAiSession的bean
    protected OpenAiSession openAiSession;

    @Override
    public ResponseBodyEmitter completions(ChatProcessAggregate chatProcess, HttpServletResponse response) {
        // 1. 校验权限
        if (!"b8b6".equals(chatProcess.getToken())) {
            throw new ChatGPTException(Constants.ResponseCode.TOKEN_ERROR.getCode(), Constants.ResponseCode.TOKEN_ERROR.getInfo());
        }

        // 2. 请求应答
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(3 * 60 * 1000L);//返回类主要用于在 HTTP 响应中以异步的方式分块（chunked）发送数据，而不是一次性发送完整的响应
        emitter.onCompletion(() -> {//onCompletion()：响应完成后触发,执行清理资源或后续逻辑
            log.info("流式问答请求完成，使用模型：{}", chatProcess.getModel());
        });

        emitter.onError(throwable -> log.error("流式问答请求疫情，使用模型：{}", chatProcess.getModel(), throwable));//该方法用于注册一个错误回调函数，如果当ResponseBodyEmitter发送数据时出现错误，会被触发

        // 3. 应答处理
        try {
            this.doMessageResponse(chatProcess, emitter,response);
        } catch (Exception e) {
            throw new ChatGPTException(Constants.ResponseCode.UN_ERROR.getCode(), Constants.ResponseCode.UN_ERROR.getInfo());
        }

        // 4. 返回结果
        return emitter;
    }

    protected abstract void doMessageResponse(ChatProcessAggregate chatProcess, ResponseBodyEmitter responseBodyEmitter,HttpServletResponse response) throws JsonProcessingException;

}
